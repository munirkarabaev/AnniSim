package me.munir.annisim.listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import me.munir.annisim.kit.SuccubusManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public class SuccubusListener implements Listener {
   private final JavaPlugin plugin;
   private final SuccubusManager succubusManager;
   private final Set<UUID> playersWithIndicator = new HashSet();

   public SuccubusListener(JavaPlugin plugin, SuccubusManager succubusManager) {
      this.plugin = plugin;
      this.succubusManager = succubusManager;
      plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateActionBarIndicator, 1L, 2L);
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         Action action = event.getAction();
         if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (this.succubusManager.canUseItem(player, event.getItem())) {
               event.setCancelled(true);
               Player target = this.succubusManager.resolveTarget(player);
               if (target == null) {
                  player.sendActionBar(Component.text("No enemy target in melee range."));
                  player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8F, 0.8F);
               } else {
                  long remainingCooldownSeconds = this.succubusManager.getRemainingCooldownSeconds(player);
                  if (remainingCooldownSeconds > 0L) {
                     player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8F, 0.6F);
                  } else {
                     double threshold = this.succubusManager.getExecuteThreshold(target);
                     this.succubusManager.startCooldown(player);
                     if (target.getHealth() <= threshold) {
                        double drainedHealth = target.getHealth();
                        this.showLifeDrainEffect(player, target);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.2F);
                        target.playSound(target.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0F, 1.0F);
                        this.healPlayer(player, drainedHealth);
                        target.setHealth(0.0D);
                        player.sendActionBar(Component.text("Executed " + target.getName() + "."));
                     } else {
                        double backfireDamage = this.succubusManager.getBackfireDamage(target);
                        double updatedHealth = Math.max(0.0D, player.getHealth() - backfireDamage);
                        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0.0D, 1.0D, 0.0D), 28, 0.4D, 0.6D, 0.4D, 0.02D);
                        player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, player.getLocation().add(0.0D, 1.0D, 0.0D), 18, 0.3D, 0.5D, 0.3D, 0.1D);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 0.8F);
                        player.setHealth(updatedHealth);
                        String var10001 = this.formatHealth(backfireDamage);
                        player.sendActionBar(Component.text("Backfire: took " + var10001 + " damage."));
                        player.sendMessage(Component.text("Life drain failed.", NamedTextColor.RED));
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.succubusManager.clearPlayer(event.getPlayer());
      this.playersWithIndicator.remove(event.getPlayer().getUniqueId());
   }

   private void updateActionBarIndicator() {
      Iterator var1;
      Player player;
      if (this.succubusManager.isInGame()) {
         var1 = this.plugin.getServer().getOnlinePlayers().iterator();

         while(var1.hasNext()) {
            player = (Player)var1.next();
            if (!this.succubusManager.canUseItem(player, player.getInventory().getItemInMainHand())) {
               if (this.playersWithIndicator.remove(player.getUniqueId())) {
                  this.clearIndicator(player);
               }
            } else {
               this.playersWithIndicator.add(player.getUniqueId());
               long remainingCooldownSeconds = this.succubusManager.getRemainingCooldownSeconds(player);
               if (remainingCooldownSeconds > 0L) {
                  player.sendActionBar(Component.text("Succubus cooldown: " + remainingCooldownSeconds + "s"));
               } else {
                  player.sendActionBar(Component.text("Succubus ready"));
               }
            }
         }

      } else {
         this.succubusManager.clearAllCooldowns();
         var1 = this.plugin.getServer().getOnlinePlayers().iterator();

         while(var1.hasNext()) {
            player = (Player)var1.next();
            this.clearIndicator(player);
         }

         this.playersWithIndicator.clear();
      }
   }

   private void showLifeDrainEffect(Player player, Player target) {
      target.getWorld().spawnParticle(Particle.WHITE_SMOKE, target.getLocation().add(0.0D, 1.0D, 0.0D), 70, 0.45D, 0.8D, 0.45D, 0.02D);
      target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0.0D, 1.0D, 0.0D), 50, 0.35D, 0.7D, 0.35D, 0.02D);
      target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0.0D, 1.0D, 0.0D), 25, 0.25D, 0.5D, 0.25D, 0.05D);
      double stepX = (player.getLocation().getX() - target.getLocation().getX()) / 12.0D;
      double stepY = (player.getLocation().getY() - target.getLocation().getY()) / 12.0D;
      double stepZ = (player.getLocation().getZ() - target.getLocation().getZ()) / 12.0D;

      for(int index = 0; index <= 12; ++index) {
         target.getWorld().spawnParticle(Particle.WHITE_SMOKE, target.getLocation().add(stepX * (double)index, 1.0D + stepY * (double)index, stepZ * (double)index), 6, 0.08D, 0.08D, 0.08D, 0.0D);
      }

   }

   private String formatHealth(double health) {
      return Math.abs(health - Math.rint(health)) < 0.001D ? Integer.toString((int)Math.rint(health)) : String.format("%.1f", health);
   }

   private void healPlayer(Player player, double amount) {
      if (amount > 0.0D) {
         double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH) == null ? 20.0D : player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
         player.setHealth(Math.min(maxHealth, player.getHealth() + amount));
      }
   }

   private void clearIndicator(Player player) {
      player.sendActionBar(Component.empty());
   }
}
