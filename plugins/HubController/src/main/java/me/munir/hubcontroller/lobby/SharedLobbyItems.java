package me.munir.hubcontroller.lobby;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class SharedLobbyItems {
   private final NamespacedKey teamSelectorKey;
   private final NamespacedKey kitSelectorKey;
   private final NamespacedKey loadoutSelectorKey;

   public SharedLobbyItems(JavaPlugin plugin) {
      this.teamSelectorKey = new NamespacedKey(plugin, "shared_team_selector");
      this.kitSelectorKey = new NamespacedKey(plugin, "shared_kit_selector");
      this.loadoutSelectorKey = new NamespacedKey(plugin, "shared_loadout_selector");
   }

   public void applyToPlayer(Player player) {
      player.getInventory().setItem(0, this.createTeamSelector());
      player.getInventory().setItem(4, this.createKitSelector());
      player.getInventory().setItem(8, this.createLoadoutSelector());
   }

   public boolean isTeamSelector(ItemStack itemStack) {
      return this.hasMarker(itemStack, this.teamSelectorKey);
   }

   public boolean isKitSelector(ItemStack itemStack) {
      return this.hasMarker(itemStack, this.kitSelectorKey);
   }

   public boolean isLoadoutSelector(ItemStack itemStack) {
      return this.hasMarker(itemStack, this.loadoutSelectorKey);
   }

   private ItemStack createTeamSelector() {
      return this.createItem(Material.BLUE_WOOL, "Select Team", this.teamSelectorKey);
   }

   private ItemStack createKitSelector() {
      return this.createItem(Material.CHEST, "Select Kit", this.kitSelectorKey);
   }

   private ItemStack createLoadoutSelector() {
      return this.createItem(Material.BOW, "Select Loadout", this.loadoutSelectorKey);
   }

   private boolean hasMarker(ItemStack itemStack, NamespacedKey key) {
      if (itemStack == null || !itemStack.hasItemMeta()) {
         return false;
      }

      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta == null) {
         return false;
      }

      Byte marker = itemMeta.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
      return marker != null && marker == 1;
   }

   private ItemStack createItem(Material material, String displayName, NamespacedKey key) {
      ItemStack itemStack = new ItemStack(material);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.displayName(Component.text(displayName));
         itemMeta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte)1);
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }
}
