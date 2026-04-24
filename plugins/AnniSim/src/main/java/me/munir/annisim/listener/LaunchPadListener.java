package me.munir.annisim.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class LaunchPadListener implements Listener {
   private static final long FALL_PROTECTION_MILLIS = 5000L;
   private static final double HORIZONTAL_POWER = 2.484D;
   private static final double BASE_VERTICAL_BOOST = 0.162D;
   private static final double SPRINT_BONUS_CAP = 0.54D;
   private static final double JUMP_VELOCITY_MULTIPLIER = 1.17D;
   private static final double JUMP_VERTICAL_BOOST = 0.2106D;
   private final JavaPlugin plugin;
   private final Map<UUID, Boolean> playersOnLaunchPad = new HashMap();
   private final Map<UUID, Long> fallProtectionEndTimes = new HashMap();

   public LaunchPadListener(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      if (event.getTo() != null) {
         if (!this.isStationary(event)) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            boolean isOnLaunchPad = this.isStandingOnLaunchPad(player);
            boolean wasOnLaunchPad = this.playersOnLaunchPad.getOrDefault(playerId, false);
            this.playersOnLaunchPad.put(playerId, isOnLaunchPad);
            if (isOnLaunchPad && !wasOnLaunchPad) {
               long now = System.currentTimeMillis();
               this.fallProtectionEndTimes.put(playerId, now + 5000L);
               this.launchPlayer(player);
            }
         }
      }
   }

   @EventHandler
   public void onEntityDamage(EntityDamageEvent event) {
      Entity var3 = event.getEntity();
      if (var3 instanceof Player) {
         Player player = (Player)var3;
         if (event.getCause() == DamageCause.FALL) {
            Long protectionEndTime = (Long)this.fallProtectionEndTimes.get(player.getUniqueId());
            if (protectionEndTime != null) {
               if (protectionEndTime <= System.currentTimeMillis()) {
                  this.fallProtectionEndTimes.remove(player.getUniqueId());
               } else {
                  event.setCancelled(true);
                  this.fallProtectionEndTimes.remove(player.getUniqueId());
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      this.playersOnLaunchPad.remove(playerId);
      this.fallProtectionEndTimes.remove(playerId);
   }

   private boolean isStationary(PlayerMoveEvent event) {
      return event.getFrom().getX() == event.getTo().getX() && event.getFrom().getY() == event.getTo().getY() && event.getFrom().getZ() == event.getTo().getZ();
   }

   private boolean isStandingOnLaunchPad(Player player) {
      BoundingBox boundingBox = player.getBoundingBox();
      double sampleY = boundingBox.getMinY() - 0.05D;
      double minX = boundingBox.getMinX() + 0.05D;
      double maxX = boundingBox.getMaxX() - 0.05D;
      double minZ = boundingBox.getMinZ() + 0.05D;
      double maxZ = boundingBox.getMaxZ() - 0.05D;
      double centerX = (minX + maxX) / 2.0D;
      double centerZ = (minZ + maxZ) / 2.0D;
      return this.isLaunchPadBlock(player, minX, sampleY, minZ) || this.isLaunchPadBlock(player, minX, sampleY, maxZ) || this.isLaunchPadBlock(player, maxX, sampleY, minZ) || this.isLaunchPadBlock(player, maxX, sampleY, maxZ) || this.isLaunchPadBlock(player, centerX, sampleY, centerZ);
   }

   private boolean isLaunchPadBlock(Player player, double x, double y, double z) {
      Block plateBlock = player.getWorld().getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
      if (plateBlock.getType() != Material.STONE_PRESSURE_PLATE) {
         return false;
      }

      return plateBlock.getRelative(0, -1, 0).getType() == Material.IRON_BLOCK;
   }

   private void launchPlayer(Player player) {
      Vector forward = player.getLocation().getDirection().setY(0).normalize().multiply(2.484D);
      Vector currentVelocity = player.getVelocity().clone();
      Vector horizontalVelocity = currentVelocity.clone().setY(0);
      if (horizontalVelocity.lengthSquared() > 0.0D) {
         forward.add(horizontalVelocity.normalize().multiply(Math.min(horizontalVelocity.length(), 0.54D)));
      }

      boolean jumpedIntoPad = currentVelocity.getY() > 0.08D;
      if (jumpedIntoPad) {
         forward.multiply(1.17D);
      }

      forward.setY(jumpedIntoPad ? 0.2106D : 0.162D);
      player.setVelocity(forward);
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0F, 0.9F);
      player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0.0D, 0.1D, 0.0D), 20, 0.25D, 0.05D, 0.25D, 0.05D);
   }
}
