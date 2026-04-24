package me.munir.annisim.lobby;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class LobbyItems {
   public static final int TEAM_SELECTOR_SLOT = 0;
   public static final int KIT_SELECTOR_SLOT = 4;
   public static final int LOADOUT_SELECTOR_SLOT = 8;
   public static final String TEAM_SELECTOR_NAME = "Select Team";
   public static final String KIT_SELECTOR_NAME = "Select Kit";
   public static final String LOADOUT_SELECTOR_NAME = "Select Loadout";
   private final NamespacedKey teamSelectorKey;
   private final NamespacedKey kitSelectorKey;
   private final NamespacedKey loadoutSelectorKey;

   public LobbyItems(JavaPlugin plugin) {
      this.teamSelectorKey = new NamespacedKey(plugin, "team_selector");
      this.kitSelectorKey = new NamespacedKey(plugin, "kit_selector");
      this.loadoutSelectorKey = new NamespacedKey(plugin, "loadout_selector");
   }

   public ItemStack createTeamSelector() {
      return this.createNamedItem(Material.BLUE_WOOL, "Select Team", this.teamSelectorKey);
   }

   public ItemStack createKitSelector() {
      return this.createNamedItem(Material.CHEST, "Select Kit", this.kitSelectorKey);
   }

   public ItemStack createLoadoutSelector() {
      return this.createNamedItem(Material.BOW, "Select Loadout", this.loadoutSelectorKey);
   }

   public boolean isLobbyItem(ItemStack itemStack) {
      return this.isTeamSelector(itemStack) || this.isKitSelector(itemStack) || this.isLoadoutSelector(itemStack);
   }

   public boolean isTeamSelector(ItemStack itemStack) {
      return this.hasLobbyKey(itemStack, this.teamSelectorKey);
   }

   public boolean isKitSelector(ItemStack itemStack) {
      return this.hasLobbyKey(itemStack, this.kitSelectorKey);
   }

   public boolean isLoadoutSelector(ItemStack itemStack) {
      return this.hasLobbyKey(itemStack, this.loadoutSelectorKey);
   }

   private boolean hasLobbyKey(ItemStack itemStack, NamespacedKey key) {
      if (itemStack == null) {
         return false;
      } else if (!itemStack.hasItemMeta()) {
         return false;
      } else {
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta != null && itemMeta.hasDisplayName()) {
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            Byte marker = (Byte)dataContainer.get(key, PersistentDataType.BYTE);
            return marker != null && marker == 1;
         } else {
            return false;
         }
      }
   }

   private ItemStack createNamedItem(Material material, String displayName, NamespacedKey key) {
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
