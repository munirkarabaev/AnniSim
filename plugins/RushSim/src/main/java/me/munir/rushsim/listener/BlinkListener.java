package me.munir.rushsim.listener;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.game.RoundItems;
import me.munir.rushsim.kit.Kit;
import me.munir.rushsim.kit.KitManager;
import net.kyori.adventure.text.Component;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class BlinkListener implements Listener {
   private static final double MIN_DASH_DISTANCE = 5.0D;
   private static final double MAX_DASH_DISTANCE = 20.0D;
   private static final BlockFace[] SEARCH_DIRECTIONS;
   private final JavaPlugin plugin;
   private final GameManager gameManager;
   private final RoundItems roundItems;
   private final KitManager kitManager;
   private final Map<UUID, Long> cooldowns = new HashMap();
   private final Map<UUID, Location> previewLocations = new HashMap();

   public BlinkListener(JavaPlugin plugin, GameManager gameManager, RoundItems roundItems, KitManager kitManager) {
      this.plugin = plugin;
      this.gameManager = gameManager;
      this.roundItems = roundItems;
      this.kitManager = kitManager;
      plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateBlinkPreviewState, 1L, 2L);
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         Action action = event.getAction();
         if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (this.shouldUseBlink(player, event.getItem())) {
               event.setCancelled(true);
               BlinkListener.DashTarget dashTarget = this.resolveDashTarget(player);
               if (dashTarget == null) {
                  this.hidePreview(player);
                  player.sendMessage("You cannot blink there.");
               } else {
                  long remainingSeconds = this.getRemainingCooldownSeconds(player);
                  if (remainingSeconds > 0L) {
                     player.sendMessage("Blink is on cooldown for " + remainingSeconds + " more seconds.");
                  } else {
                     player.teleport(dashTarget.getLandingLocation());
                     player.getWorld().playSound(dashTarget.getLandingLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1.0F, 0.8F);
                     player.getWorld().playSound(dashTarget.getLandingLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1.0F, 1.1F);
                     player.getWorld().spawnParticle(Particle.PORTAL, dashTarget.getLandingLocation().clone().add(0.0D, 1.0D, 0.0D), 56, 0.45D, 0.75D, 0.45D, 0.06D);
                     player.getWorld().spawnParticle(Particle.WHITE_ASH, dashTarget.getLandingLocation().clone().add(0.0D, 1.0D, 0.0D), 40, 0.35D, 0.65D, 0.35D, 0.015D);
                     long cooldownMillis = (long)Math.ceil(dashTarget.getDistance() * 1000.0D);
                     this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMillis);
                     this.showPreview(player, dashTarget);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      this.cooldowns.remove(playerId);
      this.previewLocations.remove(playerId);
   }

   private void updateBlinkPreviewState() {
      Iterator var1;
      Player player;
      if (this.gameManager.isInGame()) {
         var1 = this.plugin.getServer().getOnlinePlayers().iterator();

         while(var1.hasNext()) {
            player = (Player)var1.next();
            if (!this.shouldShowBlinkActionBar(player)) {
               this.hidePreview(player);
               this.clearActionBar(player);
            } else {
               this.showCooldownActionBar(player);
               if (!this.shouldShowBlinkPreview(player)) {
                  this.hidePreview(player);
               } else {
                  BlinkListener.DashTarget dashTarget = this.resolveDashTarget(player);
                  if (dashTarget == null) {
                     this.hidePreview(player);
                  } else {
                     this.showPreview(player, dashTarget);
                  }
               }
            }
         }

      } else {
         this.clearAllCooldowns();
         var1 = this.plugin.getServer().getOnlinePlayers().iterator();

         while(var1.hasNext()) {
            player = (Player)var1.next();
            this.hidePreview(player);
            this.clearActionBar(player);
         }

      }
   }

   private boolean shouldShowBlinkActionBar(Player player) {
      if (!this.gameManager.isInGame()) {
         return false;
      } else {
         return this.kitManager.getKit(player) != Kit.DASHER ? false : this.roundItems.isBlinkItem(player.getInventory().getItemInMainHand());
      }
   }

   private boolean shouldShowBlinkPreview(Player player) {
      return this.shouldShowBlinkActionBar(player) && player.isSneaking();
   }

   private boolean shouldUseBlink(Player player, ItemStack itemStack) {
      if (!this.gameManager.isInGame()) {
         return false;
      } else if (!player.isSneaking()) {
         return false;
      } else {
         return this.kitManager.getKit(player) != Kit.DASHER ? false : this.roundItems.isBlinkItem(itemStack);
      }
   }

   private BlinkListener.DashTarget resolveDashTarget(Player player) {
      RayTraceResult rayTraceResult = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 20.0D, FluidCollisionMode.NEVER, true);
      if (rayTraceResult != null && rayTraceResult.getHitBlock() != null) {
         Block hitBlock = rayTraceResult.getHitBlock();
         Vector hitPosition = rayTraceResult.getHitPosition();
         BlinkListener.DashTarget directDashTarget = this.createDashTarget(player, hitBlock, true);
         return directDashTarget != null ? directDashTarget : (BlinkListener.DashTarget)this.getCandidateBlocks(hitBlock).stream().filter((candidateBlock) -> {
            return !this.isSameBlock(candidateBlock, hitBlock);
         }).map((candidateBlock) -> {
            return this.createDashTarget(player, candidateBlock, false);
         }).filter((dashTarget) -> {
            return dashTarget != null;
         }).min(Comparator.comparingDouble((dashTarget) -> {
            return this.getTargetPriorityScore(dashTarget, hitPosition);
         })).orElse((BlinkListener.DashTarget)null);
      } else {
         return null;
      }
   }

   private Set<Block> getCandidateBlocks(Block targetBlock) {
      Set<Block> candidateBlocks = new LinkedHashSet();
      candidateBlocks.add(targetBlock);
      BlockFace[] var3 = SEARCH_DIRECTIONS;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         BlockFace blockFace = var3[var5];
         candidateBlocks.add(targetBlock.getRelative(blockFace));
      }

      return candidateBlocks;
   }

   private double getTargetPriorityScore(BlinkListener.DashTarget dashTarget, Vector hitPosition) {
      Location previewLocation = dashTarget.getPreviewBlockLocation();
      double centerX = previewLocation.getX() + 0.5D;
      double centerY = previewLocation.getY() + 0.5D;
      double centerZ = previewLocation.getZ() + 0.5D;
      double horizontalDistance = this.horizontalDistanceSquared(centerX, centerZ, hitPosition.getX(), hitPosition.getZ());
      double verticalDistance = Math.abs(centerY - hitPosition.getY());
      return horizontalDistance * 4.0D + verticalDistance;
   }

   private double horizontalDistanceSquared(double firstX, double firstZ, double secondX, double secondZ) {
      double deltaX = firstX - secondX;
      double deltaZ = firstZ - secondZ;
      return deltaX * deltaX + deltaZ * deltaZ;
   }

   private BlinkListener.DashTarget createDashTarget(Player player, Block lookedAtBlock, boolean allowTargetColumnPass) {
      Block standableBlock = this.resolveStandableBlock(lookedAtBlock);
      if (standableBlock == null) {
         return null;
      } else {
         Location landingLocation = this.getLandingLocation(player, standableBlock);
         if (landingLocation == null) {
            return null;
         } else {
            double distance = player.getLocation().distance(landingLocation);
            if (!(distance < 5.0D) && !(distance > 20.0D)) {
               return !this.isPathClear(player, lookedAtBlock, standableBlock, allowTargetColumnPass) ? null : new BlinkListener.DashTarget(standableBlock.getLocation(), landingLocation, distance);
            } else {
               return null;
            }
         }
      }
   }

   private Block resolveStandableBlock(Block sourceBlock) {
      if (sourceBlock.isLiquid()) {
         return null;
      } else if (this.isPassableTarget(sourceBlock)) {
         return this.findStandableBlockBelow(sourceBlock.getRelative(BlockFace.DOWN));
      } else if (this.isValidStandableBlock(sourceBlock)) {
         return sourceBlock;
      } else {
         Block standableAbove = this.findStandableBlockAbove(sourceBlock.getRelative(BlockFace.UP));
         return standableAbove != null ? standableAbove : this.findStandableBlockBelow(sourceBlock.getRelative(BlockFace.DOWN));
      }
   }

   private Block findStandableBlockAbove(Block startBlock) {
      Block candidateBlock = startBlock;

      for(int height = 0; height < 3; ++height) {
         if (candidateBlock.isLiquid()) {
            return null;
         }

         if (this.isValidStandableBlock(candidateBlock)) {
            return candidateBlock;
         }

         candidateBlock = candidateBlock.getRelative(BlockFace.UP);
      }

      return null;
   }

   private Block findStandableBlockBelow(Block startBlock) {
      Block candidateBlock = startBlock;

      for(int depth = 0; depth < 3; ++depth) {
         if (candidateBlock.isLiquid()) {
            return null;
         }

         if (this.isValidStandableBlock(candidateBlock)) {
            return candidateBlock;
         }

         candidateBlock = candidateBlock.getRelative(BlockFace.DOWN);
      }

      return null;
   }

   private boolean isValidStandableBlock(Block block) {
      if (!block.getType().isSolid()) {
         return false;
      } else if (!block.isLiquid() && !this.isBlockedLandingSurface(block.getType())) {
         Block feetBlock = block.getRelative(BlockFace.UP);
         Block headBlock = block.getRelative(BlockFace.UP, 2);
         if (feetBlock.isPassable() && headBlock.isPassable()) {
            return !feetBlock.isLiquid() && !headBlock.isLiquid();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isPassableTarget(Block block) {
      return block.isPassable() || block.isLiquid();
   }

   private Location getLandingLocation(Player player, Block standableBlock) {
      return new Location(player.getWorld(), (double)standableBlock.getX() + 0.5D, (double)standableBlock.getY() + 1.0D, (double)standableBlock.getZ() + 0.5D, player.getLocation().getYaw(), player.getLocation().getPitch());
   }

   private boolean isPathClear(Player player, Block lookedAtBlock, Block standableBlock, boolean allowTargetColumnPass) {
      Location eyeLocation = player.getEyeLocation();
      Location targetCenter = lookedAtBlock.getLocation().add(0.5D, 0.5D, 0.5D);
      RayTraceResult rayTraceResult = player.getWorld().rayTraceBlocks(eyeLocation, targetCenter.toVector().subtract(eyeLocation.toVector()).normalize(), eyeLocation.distance(targetCenter), FluidCollisionMode.NEVER, true);
      if (rayTraceResult != null) {
         Block hitBlock = rayTraceResult.getHitBlock();
         if (hitBlock != null && !this.isSameBlock(hitBlock, lookedAtBlock) && !this.isSameBlock(hitBlock, standableBlock)) {
            return false;
         }
      }

      Location landingLocation = this.getLandingLocation(player, standableBlock);
      Location bodyCheckStart = player.getLocation().clone().add(0.0D, 1.0D, 0.0D);
      RayTraceResult landingTrace = player.getWorld().rayTraceBlocks(bodyCheckStart, landingLocation.toVector().subtract(bodyCheckStart.toVector()).normalize(), bodyCheckStart.distance(landingLocation), FluidCollisionMode.NEVER, true);
      if (landingTrace == null) {
         return true;
      } else {
         Block hitBlock = landingTrace.getHitBlock();
         if (hitBlock == null) {
            return false;
         } else if (this.isSameBlock(hitBlock, standableBlock)) {
            return true;
         } else if (!allowTargetColumnPass) {
            return false;
         } else {
            return this.isSameBlock(hitBlock, lookedAtBlock) || this.isSameBlock(hitBlock, lookedAtBlock.getRelative(BlockFace.UP)) || this.isSameBlock(hitBlock, lookedAtBlock.getRelative(BlockFace.UP, 2));
         }
      }
   }

   private boolean isBlockedLandingSurface(Material material) {
      String materialName = material.name();
      return materialName.contains("GLASS") || material == Material.BRICKS || material == Material.BRICK_STAIRS || material == Material.BRICK_SLAB || material == Material.IRON_BARS || material == Material.NETHER_BRICK_FENCE || materialName.endsWith("_FENCE");
   }

   private void showPreview(Player player, BlinkListener.DashTarget dashTarget) {
      Location previewLocation = dashTarget.getPreviewBlockLocation();
      Location previousPreview = (Location)this.previewLocations.get(player.getUniqueId());
      if (previousPreview != null && !this.isSameBlock(previousPreview.getBlock(), previewLocation.getBlock())) {
         this.restorePreviewBlock(player, previousPreview);
      }

      player.sendBlockChange(previewLocation, this.getPreviewMaterial(dashTarget.getDistance()).createBlockData());
      this.previewLocations.put(player.getUniqueId(), previewLocation);
   }

   private void hidePreview(Player player) {
      Location previewLocation = (Location)this.previewLocations.remove(player.getUniqueId());
      if (previewLocation != null) {
         this.restorePreviewBlock(player, previewLocation);
      }

   }

   private void clearActionBar(Player player) {
      player.sendActionBar(Component.empty());
   }

   private void restorePreviewBlock(Player player, Location previewLocation) {
      BlockData originalBlockData = previewLocation.getBlock().getBlockData();
      player.sendBlockChange(previewLocation, originalBlockData);
   }

   private void showCooldownActionBar(Player player) {
      long remainingSeconds = this.getRemainingCooldownSeconds(player);
      if (remainingSeconds > 0L) {
         player.sendActionBar(Component.text("Dash - " + remainingSeconds + " seconds"));
      } else {
         player.sendActionBar(Component.text("Dash - Ready"));
      }
   }

   private long getRemainingCooldownSeconds(Player player) {
      long cooldownEnd = (Long)this.cooldowns.getOrDefault(player.getUniqueId(), 0L);
      long remainingMillis = cooldownEnd - System.currentTimeMillis();
      return remainingMillis <= 0L ? 0L : (long)Math.ceil((double)remainingMillis / 1000.0D);
   }

   private void clearAllCooldowns() {
      this.cooldowns.clear();
   }

   private Material getPreviewMaterial(double distance) {
      if (distance <= 10.0D) {
         return Material.EMERALD_BLOCK;
      } else {
         return distance <= 15.0D ? Material.GOLD_BLOCK : Material.DIAMOND_BLOCK;
      }
   }

   private boolean isSameBlock(Block firstBlock, Block secondBlock) {
      return firstBlock.getWorld().getUID().equals(secondBlock.getWorld().getUID()) && firstBlock.getX() == secondBlock.getX() && firstBlock.getY() == secondBlock.getY() && firstBlock.getZ() == secondBlock.getZ();
   }

   static {
      SEARCH_DIRECTIONS = new BlockFace[]{BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};
   }

   private static class DashTarget {
      private final Location previewBlockLocation;
      private final Location landingLocation;
      private final double distance;

      private DashTarget(Location previewBlockLocation, Location landingLocation, double distance) {
         this.previewBlockLocation = previewBlockLocation;
         this.landingLocation = landingLocation;
         this.distance = distance;
      }

      public Location getPreviewBlockLocation() {
         return this.previewBlockLocation;
      }

      public Location getLandingLocation() {
         return this.landingLocation;
      }

      public double getDistance() {
         return this.distance;
      }
   }
}
