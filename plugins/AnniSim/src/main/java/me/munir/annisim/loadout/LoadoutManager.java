package me.munir.annisim.loadout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import me.munir.annisim.game.GameManager;
import me.munir.annisim.game.RoundItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

public class LoadoutManager {
   private static final int LAYOUT_SIZE = 27;
   private static final int EDITOR_SIZE = 36;
   private static final int HOTBAR_SIZE = 9;
   private static final int INVENTORY_SIZE = 18;
   private static final int DECORATION_ROW_START = 27;
   private static final int OFFHAND_EDITOR_SLOT = 35;
   private static final String EDITOR_FILLER_NAME = " ";
   private static final String OFFHAND_SLOT_NAME = "Offhand";
   private final JavaPlugin plugin;
   private final RoundItems roundItems;
   private final File playerDataFolder;
   private final Map<UUID, LoadoutType> playerLoadouts = new HashMap();
   private final Map<UUID, Map<LoadoutType, List<String>>> hotbarLayouts = new HashMap();
   private final Map<UUID, Map<LoadoutType, List<String>>> inventoryLayouts = new HashMap();
   private final Map<UUID, Map<LoadoutType, String>> offhandLayouts = new HashMap();
   private final Map<UUID, LoadoutType> roundLoadoutOverrides = new HashMap();
   private final Set<UUID> loadedPlayers = new HashSet();
   private final Set<LoadoutType> bannedLoadouts = new HashSet();

   public LoadoutManager(JavaPlugin plugin, RoundItems roundItems) {
      this.plugin = plugin;
      this.roundItems = roundItems;
      this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
      if (!this.playerDataFolder.exists()) {
         this.playerDataFolder.mkdirs();
      }

      this.loadBannedLoadouts();
   }

   public LoadoutType getSelectedLoadout(Player player) {
      UUID playerId = player.getUniqueId();
      this.ensureLoaded(playerId);
      LoadoutType selectedLoadout = (LoadoutType)this.roundLoadoutOverrides.get(playerId);
      if (selectedLoadout == null) {
         selectedLoadout = (LoadoutType)this.playerLoadouts.getOrDefault(playerId, LoadoutType.DEFAULT);
      }

      return this.isLoadoutBanned(selectedLoadout) ? LoadoutType.DEFAULT : selectedLoadout;
   }

   public void setSelectedLoadout(Player player, LoadoutType loadoutType) {
      if (loadoutType == null || this.isLoadoutBanned(loadoutType)) {
         loadoutType = LoadoutType.DEFAULT;
      }

      UUID playerId = player.getUniqueId();
      this.ensureLoaded(playerId);
      this.playerLoadouts.put(playerId, loadoutType);
      this.savePlayerData(playerId);
   }

   public boolean isLoadoutBanned(LoadoutType loadoutType) {
      return loadoutType != null && this.bannedLoadouts.contains(loadoutType);
   }

   public List<LoadoutType> getSelectableLoadouts() {
      return Arrays.stream(LoadoutType.values()).filter((loadoutType) -> {
         return !this.isLoadoutBanned(loadoutType);
      }).toList();
   }

   public boolean banLoadout(LoadoutType loadoutType, GameManager gameManager) {
      if (loadoutType == null || loadoutType == LoadoutType.DEFAULT) {
         return false;
      } else if (!this.bannedLoadouts.add(loadoutType)) {
         return false;
      } else {
         this.saveBannedLoadouts();
         this.resetSelectedLoadout(loadoutType, gameManager);
         return true;
      }
   }

   public boolean unbanLoadout(LoadoutType loadoutType) {
      if (loadoutType == null) {
         return false;
      } else if (!this.bannedLoadouts.remove(loadoutType)) {
         return false;
      } else {
         this.saveBannedLoadouts();
         return true;
      }
   }

   public void setRoundLoadout(Player player, LoadoutType loadoutType) {
      UUID playerId = player.getUniqueId();
      if (loadoutType == null || this.isLoadoutBanned(loadoutType)) {
         this.roundLoadoutOverrides.remove(playerId);
      } else {
         this.roundLoadoutOverrides.put(playerId, loadoutType);
      }
   }

   public void clearRoundLoadouts() {
      this.roundLoadoutOverrides.clear();
   }

   public Inventory createLayoutEditorInventory(Player player, LoadoutType loadoutType, List<ItemStack> baseItems) {
      this.ensureLoaded(player.getUniqueId());
      Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 36, this.getLayoutEditorTitle(loadoutType));
      List<ItemStack> layoutItems = this.replaceKitItemsWithPlaceholder(this.getLoadoutItems(loadoutType, baseItems));
      LayoutArrangement arrangement = this.arrangeItems(player.getUniqueId(), loadoutType, layoutItems);

      for(int slot = 0; slot < arrangement.mainInventoryItems().size(); ++slot) {
         ItemStack itemStack = (ItemStack)arrangement.mainInventoryItems().get(slot);
         if (itemStack != null) {
            inventory.setItem(slot, itemStack);
         }
      }

      this.refreshLayoutEditorDecorations(inventory);
      this.setEditorSlotItem(inventory, 35, arrangement.offhandItem());
      return inventory;
   }

   public void saveLayout(Player player, LoadoutType loadoutType, Inventory inventory) {
      UUID playerId = player.getUniqueId();
      this.ensureLoaded(playerId);
      ((Map)this.hotbarLayouts.computeIfAbsent(playerId, (ignored) -> {
         return new EnumMap(LoadoutType.class);
      })).put(loadoutType, this.serializeSlots(inventory, 0, 9));
      ((Map)this.inventoryLayouts.computeIfAbsent(playerId, (ignored) -> {
         return new EnumMap(LoadoutType.class);
      })).put(loadoutType, this.serializeSlots(inventory, 9, 27));
      ((Map)this.offhandLayouts.computeIfAbsent(playerId, (ignored) -> {
         return new EnumMap(LoadoutType.class);
      })).put(loadoutType, this.serializeEditorSlot(inventory, 35));
      this.savePlayerData(playerId);
   }

   public void applyLoadout(Player player, PlayerInventory inventory, List<ItemStack> baseItems) {
      LoadoutType selectedLoadout = this.getSelectedLoadout(player);
      this.applyArmorLoadout(inventory, selectedLoadout);
      List<ItemStack> layoutItems = this.getLoadoutItems(selectedLoadout, baseItems);
      LayoutArrangement arrangement = this.arrangeItems(player.getUniqueId(), selectedLoadout, layoutItems);

      for(int slot = 0; slot < arrangement.mainInventoryItems().size(); ++slot) {
         inventory.setItem(slot, (ItemStack)arrangement.mainInventoryItems().get(slot));
      }

      inventory.setItemInOffHand(arrangement.offhandItem());
   }

   public String getLayoutEditorTitle(LoadoutType loadoutType) {
      return "Edit Loadout: " + loadoutType.getDisplayName();
   }

   public LoadoutType parseLayoutEditorTitle(String title) {
      String prefix = "Edit Loadout: ";
      if (!title.startsWith(prefix)) {
         return null;
      } else {
         String loadoutName = title.substring(prefix.length());
         LoadoutType[] var4 = LoadoutType.values();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            LoadoutType loadoutType = var4[var6];
            if (loadoutType.getDisplayName().equalsIgnoreCase(loadoutName)) {
               return loadoutType;
            }
         }

         return null;
      }
   }

   private List<ItemStack> getLoadoutItems(LoadoutType loadoutType, List<ItemStack> baseItems) {
      List<ItemStack> items = this.cloneItems(baseItems);
      this.removeItems(items, Material.BOW, Material.ARROW);
      switch(loadoutType) {
      case WARRIOR:
         this.replaceSword(items, this.createSword(Material.DIAMOND_SWORD, 4, 0, 0));
         break;
      case TANK:
         this.replaceSword(items, this.createSword(Material.IRON_SWORD, 3, 0, 0));
         break;
      case POWERPAD:
         items.add(new ItemStack(Material.IRON_BLOCK, 4));
         items.add(new ItemStack(Material.STONE_PRESSURE_PLATE, 4));
         break;
      case STRIDER:
         this.replaceSword(items, this.createSword(Material.IRON_SWORD, 3, 0, 0));
         break;
      case ARCHER:
         this.replaceSword(items, this.createSword(Material.IRON_SWORD, 3, 0, 0));
         items.add(this.createBow(4, 1));
         items.add(new ItemStack(Material.ARROW, 64));
         items.add(new ItemStack(Material.ARROW, 64));
         break;
      case FIRE:
         this.replaceSword(items, this.createSword(Material.IRON_SWORD, 3, 2, 0));
         break;
      case FORTUNE:
         this.replaceSword(items, this.createSword(Material.IRON_SWORD, 3, 0, 2));
         this.replaceTool(items, Material.IRON_PICKAXE, this.createPickaxe(4, 3));
         break;
      case GOD_APPLE:
         items.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
      case DEFAULT:
      }

      return items;
   }

   private void replaceSword(List<ItemStack> items, ItemStack replacementSword) {
      for(int index = 0; index < items.size(); ++index) {
         ItemStack itemStack = (ItemStack)items.get(index);
         if (itemStack != null && itemStack.getType() == Material.IRON_SWORD) {
            items.set(index, replacementSword);
            return;
         }
      }

      items.add(0, replacementSword);
   }

   private void removeItems(List<ItemStack> items, Material... materials) {
      Set<Material> materialSet = Set.of(materials);
      items.removeIf((itemStack) -> {
         return itemStack != null && materialSet.contains(itemStack.getType());
      });
   }

   private void replaceTool(List<ItemStack> items, Material material, ItemStack replacementTool) {
      for(int index = 0; index < items.size(); ++index) {
         ItemStack itemStack = (ItemStack)items.get(index);
         if (itemStack != null && itemStack.getType() == material) {
            items.set(index, replacementTool);
            return;
         }
      }

      items.add(replacementTool);
   }

   public void refreshLayoutEditorDecorations(Inventory inventory) {
      for(int slot = 27; slot < 35; ++slot) {
         inventory.setItem(slot, this.createEditorFillerItem());
      }

      if (this.getEditorSlotItem(inventory, 35) == null) {
         inventory.setItem(35, this.createOffhandPlaceholderItem());
      }
   }

   public boolean isLockedEditorSlot(int slot) {
      return slot >= 27 && slot < 35;
   }

   public boolean isEditorSlot(int slot) {
      return slot >= 0 && slot < 27 || slot == 35;
   }

   public boolean isOffhandEditorSlot(int slot) {
      return slot == 35;
   }

   public ItemStack getEditorSlotItem(Inventory inventory, int slot) {
      ItemStack itemStack = inventory.getItem(slot);
      return this.isEditorPlaceholderItem(itemStack) ? null : itemStack;
   }

   public void setEditorSlotItem(Inventory inventory, int slot, ItemStack itemStack) {
      if (slot == 35) {
         inventory.setItem(slot, itemStack == null ? this.createOffhandPlaceholderItem() : itemStack);
      } else {
         inventory.setItem(slot, itemStack);
      }
   }

   private LayoutArrangement arrangeItems(UUID playerId, LoadoutType loadoutType, List<ItemStack> items) {
      List<ItemStack> remainingItems = this.cloneItems(items);
      ItemStack offhandItem = null;
      String offhandLayout = this.getSavedOffhandLayout(playerId, loadoutType);
      if (offhandLayout != null && !offhandLayout.isBlank()) {
         int offhandIndex = this.findMatchingItemIndex(remainingItems, offhandLayout);
         if (offhandIndex != -1) {
            offhandItem = (ItemStack)remainingItems.remove(offhandIndex);
         }
      }

      return new LayoutArrangement(this.arrangeMainInventory(playerId, loadoutType, remainingItems), offhandItem);
   }

   private List<ItemStack> arrangeMainInventory(UUID playerId, LoadoutType loadoutType, List<ItemStack> items) {
      List<String> desiredLayout = this.getSavedLayout(playerId, loadoutType);
      if (desiredLayout == null) {
         return this.createDefaultArrangement(items);
      } else {
         List<ItemStack> arrangedItems = new ArrayList(27);

         for(int index = 0; index < 27; ++index) {
            arrangedItems.add((ItemStack)null);
         }

         List<ItemStack> remainingItems = this.cloneItems(items);

         int remainingIndex;
         for(remainingIndex = 0; remainingIndex < Math.min(27, desiredLayout.size()); ++remainingIndex) {
            String layoutKey = (String)desiredLayout.get(remainingIndex);
            if (layoutKey != null && !layoutKey.isBlank()) {
               int itemIndex = this.findMatchingItemIndex(remainingItems, layoutKey);
               if (itemIndex != -1) {
                  arrangedItems.set(remainingIndex, (ItemStack)remainingItems.remove(itemIndex));
               }
            }
         }

         remainingIndex = 0;

         for(int slot = 0; slot < arrangedItems.size() && remainingIndex < remainingItems.size(); ++slot) {
            if (arrangedItems.get(slot) == null) {
               arrangedItems.set(slot, (ItemStack)remainingItems.get(remainingIndex));
               ++remainingIndex;
            }
         }

         return arrangedItems;
      }
   }

   private List<ItemStack> createDefaultArrangement(List<ItemStack> items) {
      List<ItemStack> arrangedItems = new ArrayList(27);

      for(int slot = 0; slot < 27; ++slot) {
         if (slot < items.size()) {
            arrangedItems.add((ItemStack)items.get(slot));
         } else {
            arrangedItems.add((ItemStack)null);
         }
      }

      return arrangedItems;
   }

   private int findMatchingItemIndex(List<ItemStack> items, String layoutKey) {
      for(int index = 0; index < items.size(); ++index) {
         ItemStack itemStack = (ItemStack)items.get(index);
         if (itemStack != null && layoutKey.equals(this.getLayoutKey(itemStack))) {
            return index;
         }
      }

      return -1;
   }

   private List<String> getSavedLayout(UUID playerId, LoadoutType loadoutType) {
      List<String> hotbar = (List)((Map)this.hotbarLayouts.computeIfAbsent(playerId, (ignored) -> {
         return new EnumMap(LoadoutType.class);
      })).get(loadoutType);
      List<String> inventory = (List)((Map)this.inventoryLayouts.computeIfAbsent(playerId, (ignored) -> {
         return new EnumMap(LoadoutType.class);
      })).get(loadoutType);
      if (hotbar == null && inventory == null) {
         return null;
      } else {
         List<String> combined = new ArrayList(27);
         combined.addAll(this.normalizeLayout(hotbar, 9));
         combined.addAll(this.normalizeLayout(inventory, 18));
         return combined;
      }
   }

   private String getSavedOffhandLayout(UUID playerId, LoadoutType loadoutType) {
      return (String)((Map)this.offhandLayouts.computeIfAbsent(playerId, (ignored) -> {
         return new EnumMap(LoadoutType.class);
      })).get(loadoutType);
   }

   private List<String> normalizeLayout(List<String> layout, int expectedSize) {
      List<String> normalized = new ArrayList(expectedSize);

      for(int index = 0; index < expectedSize; ++index) {
         if (layout != null && index < layout.size()) {
            normalized.add((String)layout.get(index));
         } else {
            normalized.add("");
         }
      }

      return normalized;
   }

   private List<String> serializeSlots(Inventory inventory, int startSlot, int endSlot) {
      List<String> keys = new ArrayList();

      for(int slot = startSlot; slot < endSlot; ++slot) {
         ItemStack itemStack = this.getEditorSlotItem(inventory, slot);
         keys.add(itemStack == null ? "" : this.getLayoutKey(itemStack));
      }

      return keys;
   }

   private String serializeEditorSlot(Inventory inventory, int slot) {
      ItemStack itemStack = this.getEditorSlotItem(inventory, slot);
      return itemStack == null ? "" : this.getLayoutKey(itemStack);
   }

   private String getLayoutKey(ItemStack itemStack) {
      if (this.roundItems.isBlinkItem(itemStack)) {
         return "KIT_ITEM";
      } else if (this.roundItems.isSuccubusItem(itemStack)) {
         return "KIT_ITEM";
      } else if (this.roundItems.isKitPlaceholderItem(itemStack)) {
         return "KIT_ITEM";
      } else {
         if (itemStack.getType() == Material.POTION) {
            PotionMeta potionMeta = (PotionMeta)itemStack.getItemMeta();
            if (potionMeta != null && !potionMeta.getCustomEffects().isEmpty()) {
               PotionEffect potionEffect = (PotionEffect)potionMeta.getCustomEffects().getFirst();
               String var10000 = potionEffect.getType().getName();
               return "POTION_" + var10000 + "_" + potionEffect.getAmplifier();
            }
         }

         return itemStack.getType().name();
      }
   }

   private List<ItemStack> replaceKitItemsWithPlaceholder(List<ItemStack> items) {
      List<ItemStack> replacedItems = this.cloneItems(items);

      for(int index = 0; index < replacedItems.size(); ++index) {
         ItemStack itemStack = (ItemStack)replacedItems.get(index);
         if (this.roundItems.isBlinkItem(itemStack) || this.roundItems.isSuccubusItem(itemStack)) {
            replacedItems.set(index, this.roundItems.createKitPlaceholderItem());
         }
      }

      return replacedItems;
   }

   private boolean isEditorPlaceholderItem(ItemStack itemStack) {
      if (itemStack != null && itemStack.hasItemMeta()) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta != null && itemMeta.hasDisplayName()) {
            String displayName = itemMeta.getDisplayName();
            return " ".equals(displayName) || "Offhand".equals(displayName);
         }
      }

      return false;
   }

   private ItemStack createEditorFillerItem() {
      ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.displayName(Component.text(" "));
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }

   private ItemStack createOffhandPlaceholderItem() {
      ItemStack itemStack = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.displayName(Component.text("Offhand"));
         itemMeta.lore(List.of(Component.text("Place one item here to"), Component.text("start with it offhand.")));
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }

   private List<ItemStack> cloneItems(List<ItemStack> items) {
      List<ItemStack> clonedItems = new ArrayList();
      Iterator var3 = items.iterator();

      while(var3.hasNext()) {
         ItemStack itemStack = (ItemStack)var3.next();
         clonedItems.add(itemStack == null ? null : itemStack.clone());
      }

      return clonedItems;
   }

   private ItemStack createSword(Material material, int sharpnessLevel, int fireAspectLevel, int knockbackLevel) {
      ItemStack sword = new ItemStack(material);
      ItemMeta itemMeta = sword.getItemMeta();
      if (itemMeta != null) {
         itemMeta.addEnchant(Enchantment.SHARPNESS, sharpnessLevel, true);
         if (fireAspectLevel > 0) {
            itemMeta.addEnchant(Enchantment.FIRE_ASPECT, fireAspectLevel, true);
         }

         if (knockbackLevel > 0) {
            itemMeta.addEnchant(Enchantment.KNOCKBACK, knockbackLevel, true);
         }

         sword.setItemMeta(itemMeta);
      }

      return sword;
   }

   private ItemStack createBow(int powerLevel, int flameLevel) {
      ItemStack bow = new ItemStack(Material.BOW);
      ItemMeta itemMeta = bow.getItemMeta();
      if (itemMeta != null) {
         itemMeta.addEnchant(Enchantment.POWER, powerLevel, true);
         if (flameLevel > 0) {
            itemMeta.addEnchant(Enchantment.FLAME, flameLevel, true);
         }

         bow.setItemMeta(itemMeta);
      }

      return bow;
   }

   private ItemStack createPickaxe(int efficiencyLevel, int fortuneLevel) {
      ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
      ItemMeta itemMeta = pickaxe.getItemMeta();
      if (itemMeta != null) {
         itemMeta.addEnchant(Enchantment.EFFICIENCY, efficiencyLevel, true);
         itemMeta.addEnchant(Enchantment.FORTUNE, fortuneLevel, true);
         pickaxe.setItemMeta(itemMeta);
      }

      return pickaxe;
   }

   private void applyArmorLoadout(PlayerInventory inventory, LoadoutType loadoutType) {
      int protectionLevel = loadoutType == LoadoutType.WARRIOR ? 2 : 3;
      int helmetProtectionLevel = loadoutType == LoadoutType.POWERPAD ? 2 : protectionLevel;
      int bootsProtectionLevel = loadoutType == LoadoutType.POWERPAD ? 2 : protectionLevel;
      if (loadoutType == LoadoutType.TANK) {
         helmetProtectionLevel = 4;
         bootsProtectionLevel = 4;
      }

      inventory.setHelmet(this.createArmorPiece(Material.IRON_HELMET, helmetProtectionLevel));
      inventory.setChestplate(this.createArmorPiece(Material.IRON_CHESTPLATE, protectionLevel));
      inventory.setLeggings(this.createArmorPiece(Material.IRON_LEGGINGS, protectionLevel));
      if (loadoutType == LoadoutType.STRIDER) {
         inventory.setBoots(this.createArmorPiece(Material.IRON_BOOTS, bootsProtectionLevel, 1));
      } else {
         inventory.setBoots(this.createArmorPiece(Material.IRON_BOOTS, bootsProtectionLevel));
      }
   }

   private ItemStack createArmorPiece(Material material, int protectionLevel) {
      return this.createArmorPiece(material, protectionLevel, 0);
   }

   private ItemStack createArmorPiece(Material material, int protectionLevel, int depthStriderLevel) {
      ItemStack itemStack = new ItemStack(material);
      itemStack.addUnsafeEnchantment(Enchantment.PROTECTION, protectionLevel);
      if (depthStriderLevel > 0) {
         itemStack.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, depthStriderLevel);
      }

      return itemStack;
   }

   private void ensureLoaded(UUID playerId) {
      if (!this.loadedPlayers.contains(playerId)) {
         this.loadedPlayers.add(playerId);
         this.playerLoadouts.putIfAbsent(playerId, LoadoutType.DEFAULT);
         this.hotbarLayouts.putIfAbsent(playerId, new EnumMap(LoadoutType.class));
         this.inventoryLayouts.putIfAbsent(playerId, new EnumMap(LoadoutType.class));
         this.offhandLayouts.putIfAbsent(playerId, new EnumMap(LoadoutType.class));
         File playerFile = this.getPlayerFile(playerId);
         if (playerFile.exists()) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(playerFile);
            String selectedLoadoutName = configuration.getString("selected-loadout", LoadoutType.DEFAULT.name());
            LoadoutType selectedLoadout = this.parseLoadoutType(selectedLoadoutName);
            this.playerLoadouts.put(playerId, this.isLoadoutBanned(selectedLoadout) ? LoadoutType.DEFAULT : selectedLoadout);
            LoadoutType[] var5 = LoadoutType.values();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               LoadoutType loadoutType = var5[var7];
               String basePath = "loadouts." + loadoutType.name();
               ((Map)this.hotbarLayouts.get(playerId)).put(loadoutType, this.normalizeLayout(configuration.getStringList(basePath + ".hotbar"), 9));
               ((Map)this.inventoryLayouts.get(playerId)).put(loadoutType, this.normalizeLayout(configuration.getStringList(basePath + ".inventory"), 18));
               ((Map)this.offhandLayouts.get(playerId)).put(loadoutType, configuration.getString(basePath + ".offhand", ""));
            }

         }
      }
   }

   private LoadoutType parseLoadoutType(String value) {
      try {
         return LoadoutType.valueOf(value.toUpperCase());
      } catch (IllegalArgumentException var3) {
         return LoadoutType.DEFAULT;
      }
   }

    private void loadBannedLoadouts() {
      this.bannedLoadouts.clear();
      Iterator var1 = this.plugin.getConfig().getStringList("banned-loadouts").iterator();

      while(var1.hasNext()) {
         String loadoutName = (String)var1.next();
         LoadoutType loadoutType = LoadoutType.fromInput(loadoutName);
         if (loadoutType != null && loadoutType != LoadoutType.DEFAULT) {
            this.bannedLoadouts.add(loadoutType);
         }
      }
   }

   private void saveBannedLoadouts() {
      this.plugin.getConfig().set("banned-loadouts", this.bannedLoadouts.stream().map(LoadoutType::name).sorted().toList());
      this.plugin.saveConfig();
   }

   private void resetSelectedLoadout(LoadoutType bannedLoadout, GameManager gameManager) {
      Iterator var3 = Bukkit.getOnlinePlayers().iterator();

      while(var3.hasNext()) {
         Player player = (Player)var3.next();
         if (this.getSelectedLoadoutInternal(player.getUniqueId()) == bannedLoadout) {
            this.playerLoadouts.put(player.getUniqueId(), LoadoutType.DEFAULT);
            this.savePlayerData(player.getUniqueId());
            gameManager.handleBannedLoadoutReset(player);
         }
      }

      File[] playerFiles = this.playerDataFolder.listFiles((dir, name) -> {
         return name.endsWith(".yml");
      });
      if (playerFiles != null) {
         File[] var8 = playerFiles;
         int var5 = playerFiles.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            File playerFile = var8[var6];
            UUID playerId = this.parsePlayerId(playerFile);
            if (playerId != null && this.getSelectedLoadoutInternal(playerId) != bannedLoadout) {
               YamlConfiguration configuration = YamlConfiguration.loadConfiguration(playerFile);
               LoadoutType selectedLoadout = this.parseLoadoutType(configuration.getString("selected-loadout", LoadoutType.DEFAULT.name()));
               if (selectedLoadout == bannedLoadout) {
                  configuration.set("selected-loadout", LoadoutType.DEFAULT.name());

                  try {
                     configuration.save(playerFile);
                  } catch (IOException var10) {
                     Logger var10000 = this.plugin.getLogger();
                     String var10001 = playerFile.getName();
                     var10000.warning("Failed to update banned loadout for " + var10001 + ": " + var10.getMessage());
                  }
               }
            }
         }
      }
   }

   private LoadoutType getSelectedLoadoutInternal(UUID playerId) {
      this.ensureLoaded(playerId);
      return (LoadoutType)this.playerLoadouts.getOrDefault(playerId, LoadoutType.DEFAULT);
   }

   private UUID parsePlayerId(File playerFile) {
      String fileName = playerFile.getName();
      if (!fileName.endsWith(".yml")) {
         return null;
      } else {
         String uuidText = fileName.substring(0, fileName.length() - 4);

         try {
            return UUID.fromString(uuidText);
         } catch (IllegalArgumentException var5) {
            return null;
         }
      }
   }

   private void savePlayerData(UUID playerId) {
      File playerFile = this.getPlayerFile(playerId);
      YamlConfiguration configuration = new YamlConfiguration();
      configuration.set("selected-loadout", ((LoadoutType)this.playerLoadouts.getOrDefault(playerId, LoadoutType.DEFAULT)).name());
      LoadoutType[] var4 = LoadoutType.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         LoadoutType loadoutType = var4[var6];
         String basePath = "loadouts." + loadoutType.name();
         configuration.set(basePath + ".hotbar", this.normalizeLayout((List)((Map)this.hotbarLayouts.get(playerId)).get(loadoutType), 9));
         configuration.set(basePath + ".inventory", this.normalizeLayout((List)((Map)this.inventoryLayouts.get(playerId)).get(loadoutType), 18));
         configuration.set(basePath + ".offhand", ((Map)this.offhandLayouts.get(playerId)).getOrDefault(loadoutType, ""));
      }

      try {
         configuration.save(playerFile);
      } catch (IOException var9) {
         Logger var10000 = this.plugin.getLogger();
         String var10001 = String.valueOf(playerId);
         var10000.warning("Failed to save loadout data for " + var10001 + ": " + var9.getMessage());
      }

   }

   private File getPlayerFile(UUID playerId) {
      return new File(this.playerDataFolder, String.valueOf(playerId) + ".yml");
   }

   private static record LayoutArrangement(List<ItemStack> mainInventoryItems, ItemStack offhandItem) {
   }
}
