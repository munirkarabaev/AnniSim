package me.munir.rushsim.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.kit.Kit;
import me.munir.rushsim.kit.KitManager;
import me.munir.rushsim.team.TeamManager;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class AcrobatListener implements Listener {
   private static final long COOLDOWN_TICKS = 200L;
   private static final int MIN_FOOD_LEVEL = 7;
   private static final double DOUBLE_JUMP_VERTICAL_VELOCITY = 0.92D;
   private static final double HORIZONTAL_BOOST = 0.55D;
   private static final double MAX_HORIZONTAL_SPEED = 1.2D;
   private final JavaPlugin plugin;
   private final GameManager gameManager;
   private final KitManager kitManager;
   private final TeamManager teamManager;
   private final Map<UUID, BukkitTask> cooldownTasks = new HashMap();
   private final Map<UUID, Long> cooldownEndTicks = new HashMap();

   public AcrobatListener(JavaPlugin plugin, GameManager gameManager, KitManager kitManager, TeamManager teamManager) {
      this.plugin = plugin;
      this.gameManager = gameManager;
      this.kitManager = kitManager;
      this.teamManager = teamManager;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
      Player player = event.getPlayer();
      if (this.isEligibleAcrobat(player) && player.getGameMode() == GameMode.SURVIVAL) {
         event.setCancelled(true);
         player.setFlying(false);
         player.setAllowFlight(false);
         Vector velocity = player.getVelocity().clone();
         Vector horizontalVelocity = velocity.clone().setY(0.0D);
         Vector forwardBoost = player.getLocation().getDirection().setY(0.0D);
         if (forwardBoost.lengthSquared() > 0.0D) {
            horizontalVelocity.add(forwardBoost.normalize().multiply(0.55D));
         }

         if (horizontalVelocity.lengthSquared() > 1.44D) {
            horizontalVelocity.normalize().multiply(1.2D);
         }

         velocity.setX(horizontalVelocity.getX());
         velocity.setZ(horizontalVelocity.getZ());
         velocity.setY(Math.max(velocity.getY(), 0.92D));
         player.setVelocity(velocity);
         player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0F, 1.0F);
         this.startCooldown(player);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      if (event.getTo() != null) {
         Player player = event.getPlayer();
         if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) {
            return;
         }

         if (!this.isEligibleAcrobat(player)) {
            this.disableFlight(player);
         } else if (player.isOnGround() && !this.isOnCooldown(player)) {
            player.setAllowFlight(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityDamage(EntityDamageEvent event) {
      Entity var3 = event.getEntity();
      if (var3 instanceof Player) {
         Player player = (Player)var3;
         if (event.getCause() == EntityDamageEvent.DamageCause.FALL && this.isEligibleAcrobat(player)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onFoodLevelChange(FoodLevelChangeEvent event) {
      Entity var3 = event.getEntity();
      if (var3 instanceof Player) {
         Player player = (Player)var3;
         if (this.isEligibleAcrobat(player) && event.getFoodLevel() < 7) {
            event.setCancelled(true);
            if (player.getFoodLevel() < 7) {
               player.setFoodLevel(7);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityExhaustion(EntityExhaustionEvent event) {
      Entity var3 = event.getEntity();
      if (var3 instanceof Player) {
         Player player = (Player)var3;
         if (this.isEligibleAcrobat(player) && player.getFoodLevel() <= 7) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.clearPlayer(event.getPlayer());
   }

   private boolean isEligibleAcrobat(Player player) {
      return this.gameManager.isInGame() && this.teamManager.isActivePlayer(player) && this.kitManager.getKit(player) == Kit.ACROBAT;
   }

   private boolean isOnCooldown(Player player) {
      Long cooldownEndTick = (Long)this.cooldownEndTicks.get(player.getUniqueId());
      if (cooldownEndTick == null) {
         return false;
      } else if (cooldownEndTick <= this.plugin.getServer().getCurrentTick()) {
         this.cooldownEndTicks.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   private void startCooldown(Player player) {
      this.clearCooldownTask(player.getUniqueId());
      long cooldownEndTick = this.plugin.getServer().getCurrentTick() + 200L;
      this.cooldownEndTicks.put(player.getUniqueId(), cooldownEndTick);
      BukkitTask cooldownTask = this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
         this.cooldownTasks.remove(player.getUniqueId());
         this.cooldownEndTicks.remove(player.getUniqueId());
         if (player.isOnline() && this.isEligibleAcrobat(player)) {
            if (player.isOnGround()) {
               player.setAllowFlight(true);
            }

            player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.9F, 1.2F);
         }
      }, 200L);
      this.cooldownTasks.put(player.getUniqueId(), cooldownTask);
   }

   private void disableFlight(Player player) {
      player.setAllowFlight(false);
      if (player.isFlying()) {
         player.setFlying(false);
      }
   }

   private void clearPlayer(Player player) {
      UUID playerId = player.getUniqueId();
      this.clearCooldownTask(playerId);
      this.cooldownEndTicks.remove(playerId);
      this.disableFlight(player);
      if (player.getFoodLevel() < 7) {
         player.setFoodLevel(7);
      }
   }

   private void clearCooldownTask(UUID playerId) {
      BukkitTask cooldownTask = (BukkitTask)this.cooldownTasks.remove(playerId);
      if (cooldownTask != null) {
         cooldownTask.cancel();
      }
   }
}
