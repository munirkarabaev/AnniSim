package me.munir.rushsim.game;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class RoundItems {
   public static final String BLINK_ITEM_NAME = "Blink";
   public static final String SUCCUBUS_ITEM_NAME = "Succubus";
   public static final String KIT_ITEM_NAME = "KIT ITEM";
   private final NamespacedKey blinkItemKey;
   private final NamespacedKey succubusItemKey;

   public RoundItems(JavaPlugin plugin) {
      this.blinkItemKey = new NamespacedKey(plugin, "blink_item");
      this.succubusItemKey = new NamespacedKey(plugin, "succubus_item");
   }

   public ItemStack createBlinkItem() {
      ItemStack itemStack = new ItemStack(Material.PURPLE_DYE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.displayName(Component.text("Blink"));
         itemMeta.getPersistentDataContainer().set(this.blinkItemKey, PersistentDataType.BYTE, (byte)1);
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }

   public ItemStack createSuccubusItem() {
      ItemStack itemStack = new ItemStack(Material.RED_DYE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.displayName(Component.text("Succubus"));
         itemMeta.getPersistentDataContainer().set(this.succubusItemKey, PersistentDataType.BYTE, (byte)1);
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }

   public ItemStack createKitPlaceholderItem() {
      ItemStack itemStack = new ItemStack(Material.BEDROCK);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.displayName(Component.text("KIT ITEM"));
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }

   public boolean isKitPlaceholderItem(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() == Material.BEDROCK && itemStack.hasItemMeta()) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         return itemMeta != null && itemMeta.hasDisplayName() && "KIT ITEM".equals(itemMeta.getDisplayName());
      } else {
         return false;
      }
   }

   public boolean isBlinkItem(ItemStack itemStack) {
      if (itemStack != null && itemStack.hasItemMeta()) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta == null) {
            return false;
         } else {
            Byte marker = (Byte)itemMeta.getPersistentDataContainer().get(this.blinkItemKey, PersistentDataType.BYTE);
            return marker != null && marker == 1;
         }
      } else {
         return false;
      }
   }

   public boolean isSuccubusItem(ItemStack itemStack) {
      if (itemStack != null && itemStack.hasItemMeta()) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta == null) {
            return false;
         } else {
            Byte marker = (Byte)itemMeta.getPersistentDataContainer().get(this.succubusItemKey, PersistentDataType.BYTE);
            return marker != null && marker == 1;
         }
      } else {
         return false;
      }
   }
}
