package me.munir.rushsim.kit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.game.RoundItems;
import me.munir.rushsim.team.Team;
import me.munir.rushsim.team.TeamManager;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class SuccubusManager {
   public static final int COOLDOWN_SECONDS = 60;
   private static final long COOLDOWN_MILLIS = 60000L;
   private static final double EXECUTE_THRESHOLD_RATIO = 0.3D;
   private static final double DEFAULT_MELEE_RANGE = 3.0D;
   private static final double HEALTH_DISPLAY_RANGE = 13.0D;
   private static final double TARGETING_RANGE_BUFFER = 0.25D;
   private static final double TARGETING_HITBOX_EXPANSION = 0.2D;
   private final GameManager gameManager;
   private final TeamManager teamManager;
   private final KitManager kitManager;
   private final RoundItems roundItems;
   private final Map<UUID, Long> cooldownEndTimes = new HashMap();

   public SuccubusManager(GameManager gameManager, TeamManager teamManager, KitManager kitManager, RoundItems roundItems) {
      this.gameManager = gameManager;
      this.teamManager = teamManager;
      this.kitManager = kitManager;
      this.roundItems = roundItems;
   }

   public boolean canUseItem(Player player, ItemStack itemStack) {
      if (!this.gameManager.isInGame()) {
         return false;
      } else if (!this.teamManager.isActivePlayer(player)) {
         return false;
      } else {
         return this.kitManager.getKit(player) != Kit.SUCCUBUS ? false : this.roundItems.isSuccubusItem(itemStack);
      }
   }

   public boolean isSuccubusItem(ItemStack itemStack) {
      return this.roundItems.isSuccubusItem(itemStack);
   }

   public boolean isInGame() {
      return this.gameManager.isInGame();
   }

   public Player getDisplayTarget(Player player) {
      return !this.canUseItem(player, player.getInventory().getItemInMainHand()) ? null : this.resolveTarget(player);
   }

   public boolean shouldShowEnemyHealth(Player player) {
      if (!this.gameManager.isInGame()) {
         return false;
      } else {
         Team team = this.teamManager.getTeam(player);
         if (team == Team.SPECTATOR) {
            return true;
         } else if (!this.teamManager.isActivePlayer(player)) {
            return false;
         } else {
            return this.kitManager.getKit(player) == Kit.SUCCUBUS;
         }
      }
   }

   public boolean shouldShowEnemyHealth(Player viewer, Player observed) {
      if (!this.shouldShowEnemyHealth(viewer)) {
         return false;
      } else {
         Team viewerTeam = this.teamManager.getTeam(viewer);
         Team observedTeam = this.teamManager.getTeam(observed);
         if (viewerTeam == Team.SPECTATOR) {
            return !viewer.getUniqueId().equals(observed.getUniqueId()) && viewer.getWorld().getUID().equals(observed.getWorld().getUID());
         } else if (!this.teamManager.isActivePlayer(observed)) {
            return false;
         } else if (viewerTeam != null && observedTeam != null && viewerTeam != observedTeam) {
            return viewer.getWorld().getUID().equals(observed.getWorld().getUID()) && viewer.getLocation().distanceSquared(observed.getLocation()) <= 169.0D;
         } else {
            return false;
         }
      }
   }

   public Player resolveTarget(Player player) {
      double meleeRange = this.getMeleeRange(player);
      Player crosshairTarget = this.findCrosshairTarget(player, meleeRange);
      if (crosshairTarget != null) {
         return crosshairTarget;
      }

      Location eyeLocation = player.getEyeLocation();
      Vector eyePosition = eyeLocation.toVector();
      Vector direction = eyeLocation.getDirection().normalize();
      RayTraceResult rayTraceResult = player.getWorld().rayTrace(eyeLocation, direction, meleeRange + 0.25D, FluidCollisionMode.NEVER, true, 0.2D, (entity) -> {
         return this.isValidTarget(player, entity);
      });
      if (rayTraceResult != null) {
         Entity var9 = rayTraceResult.getHitEntity();
         if (var9 instanceof Player) {
            Player target = (Player)var9;
            return target;
         }
      }

      return this.findFallbackTarget(player, eyeLocation, eyePosition, direction, meleeRange);
   }

   public long getRemainingCooldownSeconds(Player player) {
      long remainingMillis = this.getRemainingCooldownMillis(player);
      return remainingMillis <= 0L ? 0L : (remainingMillis + 999L) / 1000L;
   }

   public void startCooldown(Player player) {
      this.cooldownEndTimes.put(player.getUniqueId(), System.currentTimeMillis() + 60000L);
   }

   public void clearPlayer(Player player) {
      this.cooldownEndTimes.remove(player.getUniqueId());
   }

   public void clearAllCooldowns() {
      this.cooldownEndTimes.clear();
   }

   public double getExecuteThreshold(Player target) {
      return this.getMaxHealth(target) * 0.3D;
   }

   public double getBackfireDamage(Player target) {
      return Math.max(0.0D, target.getHealth() - this.getExecuteThreshold(target));
   }

   public int getDisplayedHealth(Player target) {
      return (int)Math.ceil(target.getHealth());
   }

   private long getRemainingCooldownMillis(Player player) {
      Long cooldownEndTime = (Long)this.cooldownEndTimes.get(player.getUniqueId());
      if (cooldownEndTime == null) {
         return 0L;
      } else {
         long remainingMillis = cooldownEndTime - System.currentTimeMillis();
         if (remainingMillis <= 0L) {
            this.cooldownEndTimes.remove(player.getUniqueId());
            return 0L;
         } else {
            return remainingMillis;
         }
      }
   }

   private boolean isValidTarget(Player player, Entity entity) {
      if (!(entity instanceof Player)) {
         return false;
      } else {
         Player target = (Player)entity;
         if (player.getUniqueId().equals(target.getUniqueId())) {
            return false;
         } else if (!this.teamManager.isActivePlayer(target)) {
            return false;
         } else {
            Team playerTeam = this.teamManager.getTeam(player);
            Team targetTeam = this.teamManager.getTeam(target);
            return playerTeam != null && targetTeam != null && playerTeam != targetTeam;
         }
      }
   }

   private double getMaxHealth(Player target) {
      return target.getAttribute(Attribute.GENERIC_MAX_HEALTH) == null ? 20.0D : target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
   }

   private double getMeleeRange(Player player) {
      return player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE) == null ? 3.0D : player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).getValue();
   }

   private Player findCrosshairTarget(Player player, double meleeRange) {
      Entity targetEntity = player.getTargetEntity((int)Math.ceil(meleeRange + 0.25D));
      if (targetEntity instanceof Player target && this.isValidTarget(player, target) && this.isWithinRange(player, target, meleeRange + 0.25D)) {
         return target;
      } else {
         return null;
      }
   }

   private Player findFallbackTarget(Player player, Location eyeLocation, Vector eyePosition, Vector direction, double meleeRange) {
      Player bestTarget = null;
      double bestScore = Double.MAX_VALUE;
      Iterator var10 = player.getNearbyEntities(meleeRange + 0.25D, meleeRange + 0.25D, meleeRange + 0.25D).iterator();

      while(var10.hasNext()) {
         Entity entity = (Entity)var10.next();
         if (this.isValidTarget(player, entity)) {
            Player target = (Player)entity;
            BoundingBox boundingBox = target.getBoundingBox().expand(0.2D);
            RayTraceResult hitboxTrace = boundingBox.rayTrace(eyePosition, direction, meleeRange + 0.25D);
            if (hitboxTrace != null && hitboxTrace.getHitPosition() != null) {
               double hitDistance = hitboxTrace.getHitPosition().distance(eyePosition);
               if (!this.isBlockedByWorld(player, eyeLocation, direction, hitDistance) && hitDistance < bestScore) {
                  bestScore = hitDistance;
                  bestTarget = target;
               }
            }
         }
      }

      return bestTarget;
   }

   private boolean isBlockedByWorld(Player player, Location eyeLocation, Vector direction, double hitDistance) {
      RayTraceResult blockTrace = player.getWorld().rayTraceBlocks(eyeLocation, direction, hitDistance, FluidCollisionMode.NEVER, true);
      return blockTrace != null && blockTrace.getHitBlock() != null;
   }

   private boolean isWithinRange(Player player, Player target, double maxRange) {
      Vector eyePosition = player.getEyeLocation().toVector();
      BoundingBox boundingBox = target.getBoundingBox().expand(0.2D);
      Vector closestPoint = new Vector(this.clamp(eyePosition.getX(), boundingBox.getMinX(), boundingBox.getMaxX()), this.clamp(eyePosition.getY(), boundingBox.getMinY(), boundingBox.getMaxY()), this.clamp(eyePosition.getZ(), boundingBox.getMinZ(), boundingBox.getMaxZ()));
      return eyePosition.distanceSquared(closestPoint) <= maxRange * maxRange;
   }

   private double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }
}
