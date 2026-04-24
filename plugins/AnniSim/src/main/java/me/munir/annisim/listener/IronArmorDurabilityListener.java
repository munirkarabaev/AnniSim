package me.munir.annisim.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class IronArmorDurabilityListener implements Listener {
   private static final int DURABILITY_MULTIPLIER = 3;
   private final NamespacedKey pendingDamageKey;

   public IronArmorDurabilityListener(JavaPlugin plugin) {
      this.pendingDamageKey = new NamespacedKey(plugin, "iron_armor_pending_damage");
   }

   @EventHandler
   public void onPlayerItemDamage(PlayerItemDamageEvent event) {
      ItemStack itemStack = event.getItem();
      if (this.isIronArmor(itemStack.getType())) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta != null) {
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            int pendingDamage = (Integer)dataContainer.getOrDefault(this.pendingDamageKey, PersistentDataType.INTEGER, 0);
            int totalDamage = pendingDamage + event.getDamage();
            int appliedDamage = totalDamage / 3;
            int remainingDamage = totalDamage % 3;
            if (remainingDamage == 0) {
               dataContainer.remove(this.pendingDamageKey);
            } else {
               dataContainer.set(this.pendingDamageKey, PersistentDataType.INTEGER, remainingDamage);
            }

            itemStack.setItemMeta(itemMeta);
            if (appliedDamage <= 0) {
               event.setCancelled(true);
            } else {
               event.setDamage(appliedDamage);
            }
         }
      }
   }

   private boolean isIronArmor(Material material) {
      return material == Material.IRON_HELMET || material == Material.IRON_CHESTPLATE || material == Material.IRON_LEGGINGS || material == Material.IRON_BOOTS;
   }
}
