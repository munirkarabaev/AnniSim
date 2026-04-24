package me.munir.annisim.listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class GodAppleConsumeListener implements Listener {
   private static final int GOD_APPLE_REGEN_DURATION_TICKS = 600;
   private static final int GOD_APPLE_REGEN_AMPLIFIER = 2;
   private static final int GOD_APPLE_ABSORPTION_DURATION_TICKS = 4800;
   private static final int GOD_APPLE_ABSORPTION_AMPLIFIER = 3;
   private final JavaPlugin plugin;
   private final Map<UUID, GodAppleConsumeListener.SuspendedPotionEffect> suspendedRegenerationEffects = new HashMap();
   private final Map<UUID, Long> godAppleRegenerationEndTimes = new HashMap();
   private final Map<UUID, BukkitTask> regenerationResumeTasks = new HashMap();
   private final Set<UUID> internalEffectUpdates = new HashSet();

   public GodAppleConsumeListener(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
      if (event.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE) {
         Player player = event.getPlayer();
         UUID playerId = player.getUniqueId();
         PotionEffect currentRegeneration = player.getPotionEffect(PotionEffectType.REGENERATION);
         if (currentRegeneration != null && currentRegeneration.getAmplifier() < 2) {
            this.storeSuspendedRegeneration(playerId, currentRegeneration);
         }

         this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
               this.godAppleRegenerationEndTimes.put(playerId, System.currentTimeMillis() + 30000L);
               this.scheduleRegenerationResume(player);
               this.runInternalEffectUpdate(player, () -> {
                  player.removePotionEffect(PotionEffectType.REGENERATION);
                  player.removePotionEffect(PotionEffectType.ABSORPTION);
                  player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 2), true);
                  player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 4800, 3), true);
               });
            }
         }, 2L);
      }
   }

   @EventHandler
   public void onPotionEffectChange(EntityPotionEffectEvent event) {
      Entity var3 = event.getEntity();
      if (var3 instanceof Player) {
         Player player = (Player)var3;
         if (event.getModifiedType() == PotionEffectType.REGENERATION) {
            UUID playerId = player.getUniqueId();
            if (!this.internalEffectUpdates.contains(playerId) && this.hasActiveGodAppleRegeneration(playerId)) {
               PotionEffect newEffect = event.getNewEffect();
               if (newEffect != null && newEffect.getAmplifier() < 2) {
                  this.storeSuspendedRegeneration(playerId, newEffect);
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      this.clearTrackedRegeneration(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.clearTrackedRegeneration(event.getPlayer().getUniqueId());
   }

   private void scheduleRegenerationResume(Player player) {
      UUID playerId = player.getUniqueId();
      BukkitTask existingTask = (BukkitTask)this.regenerationResumeTasks.remove(playerId);
      if (existingTask != null) {
         existingTask.cancel();
      }

      BukkitTask task = this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
         this.regenerationResumeTasks.remove(playerId);
         this.godAppleRegenerationEndTimes.remove(playerId);
         if (!player.isOnline()) {
            this.suspendedRegenerationEffects.remove(playerId);
         } else {
            GodAppleConsumeListener.SuspendedPotionEffect suspendedEffect = (GodAppleConsumeListener.SuspendedPotionEffect)this.suspendedRegenerationEffects.remove(playerId);
            if (suspendedEffect != null) {
               int remainingTicks = suspendedEffect.getRemainingTicks();
               if (remainingTicks > 0) {
                  PotionEffect currentRegeneration = player.getPotionEffect(PotionEffectType.REGENERATION);
                  if (currentRegeneration == null || currentRegeneration.getAmplifier() < suspendedEffect.amplifier()) {
                     this.runInternalEffectUpdate(player, () -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, remainingTicks, suspendedEffect.amplifier(), suspendedEffect.ambient(), suspendedEffect.particles(), suspendedEffect.icon()), true);
                     });
                  }
               }
            }
         }
      }, 600L);
      this.regenerationResumeTasks.put(playerId, task);
   }

   private void storeSuspendedRegeneration(UUID playerId, PotionEffect potionEffect) {
      GodAppleConsumeListener.SuspendedPotionEffect newEffect = GodAppleConsumeListener.SuspendedPotionEffect.fromPotionEffect(potionEffect);
      GodAppleConsumeListener.SuspendedPotionEffect existingEffect = (GodAppleConsumeListener.SuspendedPotionEffect)this.suspendedRegenerationEffects.get(playerId);
      if (existingEffect == null || this.shouldReplace(existingEffect, newEffect)) {
         this.suspendedRegenerationEffects.put(playerId, newEffect);
      }

   }

   private boolean shouldReplace(GodAppleConsumeListener.SuspendedPotionEffect existingEffect, GodAppleConsumeListener.SuspendedPotionEffect newEffect) {
      if (newEffect.amplifier() != existingEffect.amplifier()) {
         return newEffect.amplifier() > existingEffect.amplifier();
      } else {
         return newEffect.expiresAtMillis() > existingEffect.expiresAtMillis();
      }
   }

   private boolean hasActiveGodAppleRegeneration(UUID playerId) {
      Long endTime = (Long)this.godAppleRegenerationEndTimes.get(playerId);
      return endTime != null && endTime > System.currentTimeMillis();
   }

   private void clearTrackedRegeneration(UUID playerId) {
      this.suspendedRegenerationEffects.remove(playerId);
      this.godAppleRegenerationEndTimes.remove(playerId);
      BukkitTask existingTask = (BukkitTask)this.regenerationResumeTasks.remove(playerId);
      if (existingTask != null) {
         existingTask.cancel();
      }

      this.internalEffectUpdates.remove(playerId);
   }

   private void runInternalEffectUpdate(Player player, Runnable action) {
      UUID playerId = player.getUniqueId();
      this.internalEffectUpdates.add(playerId);

      try {
         action.run();
      } finally {
         this.internalEffectUpdates.remove(playerId);
      }

   }

   private static record SuspendedPotionEffect(long expiresAtMillis, int amplifier, boolean ambient, boolean particles, boolean icon) {
      private SuspendedPotionEffect(long expiresAtMillis, int amplifier, boolean ambient, boolean particles, boolean icon) {
         this.expiresAtMillis = expiresAtMillis;
         this.amplifier = amplifier;
         this.ambient = ambient;
         this.particles = particles;
         this.icon = icon;
      }

      private static GodAppleConsumeListener.SuspendedPotionEffect fromPotionEffect(PotionEffect potionEffect) {
         return new GodAppleConsumeListener.SuspendedPotionEffect(System.currentTimeMillis() + (long)potionEffect.getDuration() * 50L, potionEffect.getAmplifier(), potionEffect.isAmbient(), potionEffect.hasParticles(), potionEffect.hasIcon());
      }

      private int getRemainingTicks() {
         long remainingMillis = this.expiresAtMillis - System.currentTimeMillis();
         return remainingMillis <= 0L ? 0 : (int)Math.max(1L, (remainingMillis + 49L) / 50L);
      }

      public long expiresAtMillis() {
         return this.expiresAtMillis;
      }

      public int amplifier() {
         return this.amplifier;
      }

      public boolean ambient() {
         return this.ambient;
      }

      public boolean particles() {
         return this.particles;
      }

      public boolean icon() {
         return this.icon;
      }
   }
}
