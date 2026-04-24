package me.munir.rushsim.loadout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.munir.rushsim.game.RoundItems;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class LoadoutManager {
   private final RoundItems roundItems;
   private final Map<UUID, LoadoutType> playerLoadouts = new HashMap<>();

   public LoadoutManager(RoundItems roundItems) {
      this.roundItems = roundItems;
   }

   public LoadoutType getSelectedLoadout(Player player) {
      return this.playerLoadouts.getOrDefault(player.getUniqueId(), LoadoutType.DEFAULT);
   }

   public void setSelectedLoadout(Player player, LoadoutType loadoutType) {
      this.playerLoadouts.put(player.getUniqueId(), loadoutType == null ? LoadoutType.DEFAULT : loadoutType);
   }

   public Inventory createSelectionMenu() {
      Inventory inventory = org.bukkit.Bukkit.createInventory((InventoryHolder)null, 9, "Select Loadout");
      ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.displayName(net.kyori.adventure.text.Component.text(LoadoutType.DEFAULT.getDisplayName()));
         itemStack.setItemMeta(itemMeta);
      }

      inventory.setItem(4, itemStack);
      return inventory;
   }

   public void applyLoadout(Player player) {
      PlayerInventory inventory = player.getInventory();
      inventory.clear();
      inventory.setHelmet(this.createArmor(Material.DIAMOND_HELMET));
      inventory.setChestplate(this.createArmor(Material.DIAMOND_CHESTPLATE));
      inventory.setLeggings(this.createArmor(Material.DIAMOND_LEGGINGS));
      inventory.setBoots(this.createArmor(Material.DIAMOND_BOOTS));
      inventory.addItem(this.createSword());
      this.addKitItem(player, inventory);
      inventory.addItem(this.createTool(Material.DIAMOND_PICKAXE, Enchantment.EFFICIENCY, 3));
      inventory.addItem(new ItemStack(Material.DIAMOND_AXE));
      inventory.addItem(new ItemStack(Material.DIAMOND_SHOVEL));
      inventory.addItem(new ItemStack(Material.COOKED_BEEF, 32));
      inventory.addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
   }

   private void addKitItem(Player player, PlayerInventory inventory) {
      // Kit lookup happens in RushSimPlugin before application. This manager only owns item creation.
   }

   public ItemStack getKitItem(String kitName) {
      if ("DASHER".equalsIgnoreCase(kitName)) {
         return this.roundItems.createBlinkItem();
      }

      if ("SUCCUBUS".equalsIgnoreCase(kitName)) {
         return this.roundItems.createSuccubusItem();
      }

      return null;
   }

   private ItemStack createArmor(Material material) {
      ItemStack itemStack = new ItemStack(material);
      itemStack.addUnsafeEnchantment(Enchantment.PROTECTION, 2);
      return itemStack;
   }

   private ItemStack createSword() {
      return this.createTool(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 4);
   }

   private ItemStack createTool(Material material, Enchantment enchantment, int level) {
      ItemStack itemStack = new ItemStack(material);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.addEnchant(enchantment, level, true);
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }
}
