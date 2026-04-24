package me.munir.annisim.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.munir.annisim.game.GameManager;
import me.munir.annisim.loadout.LoadoutManager;
import me.munir.annisim.loadout.LoadoutType;
import me.munir.annisim.lobby.LobbyItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LoadoutSelectorListener implements Listener {
   private static final String LOADOUT_MENU_TITLE = "Select Loadout";
   private final LobbyItems lobbyItems;
   private final GameManager gameManager;
   private final LoadoutManager loadoutManager;
   private final Map<UUID, Integer> selectedEditorSlots = new HashMap();

   public LoadoutSelectorListener(LobbyItems lobbyItems, GameManager gameManager, LoadoutManager loadoutManager) {
      this.lobbyItems = lobbyItems;
      this.gameManager = gameManager;
      this.loadoutManager = loadoutManager;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         Action action = event.getAction();
         if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (this.lobbyItems.isLoadoutSelector(event.getItem())) {
               event.setCancelled(true);
               event.getPlayer().openInventory(this.createLoadoutSelectionMenu());
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      String title = event.getView().getTitle();
      if ("Select Loadout".equals(title)) {
         this.handleSelectionClick(event);
      } else {
         LoadoutType loadoutType = this.loadoutManager.parseLayoutEditorTitle(title);
         if (loadoutType != null) {
            this.handleLayoutEditorClick(event, loadoutType);
         }

      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      String title = event.getView().getTitle();
      if ("Select Loadout".equals(title) || this.loadoutManager.parseLayoutEditorTitle(title) != null) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      HumanEntity var3 = event.getPlayer();
      if (var3 instanceof Player) {
         Player player = (Player)var3;
         LoadoutType loadoutType = this.loadoutManager.parseLayoutEditorTitle(event.getView().getTitle());
         if (loadoutType == null) {
            this.selectedEditorSlots.remove(player.getUniqueId());
         } else {
            this.selectedEditorSlots.remove(player.getUniqueId());
            this.loadoutManager.saveLayout(player, loadoutType, event.getInventory());
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.selectedEditorSlots.remove(event.getPlayer().getUniqueId());
   }

   private void handleSelectionClick(InventoryClickEvent event) {
      event.setCancelled(true);
      HumanEntity var3 = event.getWhoClicked();
      if (var3 instanceof Player) {
         Player player = (Player)var3;
         LoadoutType loadoutType = this.getClickedLoadout(event.getCurrentItem());
         if (loadoutType != null) {
            if (this.loadoutManager.isLoadoutBanned(loadoutType)) {
               player.sendMessage(loadoutType.getDisplayName() + " is currently banned.");
               player.closeInventory();
               return;
            }

            this.loadoutManager.setSelectedLoadout(player, loadoutType);
            player.openInventory(this.loadoutManager.createLayoutEditorInventory(player, loadoutType, this.gameManager.createBaseRoundItems(player)));
            player.sendMessage("You selected the " + loadoutType.getDisplayName() + " loadout.");
         }
      }
   }

   private void handleLayoutEditorClick(InventoryClickEvent event, LoadoutType loadoutType) {
      event.setCancelled(true);
      HumanEntity var4 = event.getWhoClicked();
      if (var4 instanceof Player) {
         Player player = (Player)var4;
         if (event.getClickedInventory() != null) {
            if (event.getRawSlot() >= 0 && event.getRawSlot() < event.getView().getTopInventory().getSize()) {
               Inventory inventory = event.getView().getTopInventory();
               UUID playerId = player.getUniqueId();
               int clickedSlot = event.getRawSlot();
               if (this.loadoutManager.isLockedEditorSlot(clickedSlot)) {
                  return;
               }

               if (!this.loadoutManager.isEditorSlot(clickedSlot)) {
                  return;
               }

               Integer selectedSlot = (Integer)this.selectedEditorSlots.get(playerId);
               if (selectedSlot == null) {
                  if (this.loadoutManager.getEditorSlotItem(inventory, clickedSlot) != null) {
                     this.selectedEditorSlots.put(playerId, clickedSlot);
                     player.sendMessage("Selected " + this.describeSlot(clickedSlot) + ". Click another slot to move it.");
                  }
               } else if (selectedSlot == clickedSlot) {
                  this.selectedEditorSlots.remove(playerId);
                  player.sendMessage("Loadout slot selection cleared.");
               } else {
                  ItemStack firstItem = this.loadoutManager.getEditorSlotItem(inventory, selectedSlot);
                  ItemStack secondItem = this.loadoutManager.getEditorSlotItem(inventory, clickedSlot);
                  this.loadoutManager.setEditorSlotItem(inventory, selectedSlot, secondItem);
                  this.loadoutManager.setEditorSlotItem(inventory, clickedSlot, firstItem);
                  this.loadoutManager.refreshLayoutEditorDecorations(inventory);
                  this.selectedEditorSlots.remove(playerId);
                  player.sendMessage("Updated " + loadoutType.getDisplayName() + " loadout layout.");
               }
            }
         }
      }
   }

   private String describeSlot(int slot) {
      return this.loadoutManager.isOffhandEditorSlot(slot) ? "the offhand slot" : "slot " + (slot + 1);
   }

   private LoadoutType getClickedLoadout(ItemStack itemStack) {
      if (itemStack != null && itemStack.hasItemMeta()) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta != null && itemMeta.hasDisplayName()) {
            String displayName = itemMeta.getDisplayName();
            LoadoutType[] var4 = LoadoutType.values();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               LoadoutType loadoutType = var4[var6];
               if (loadoutType.getDisplayName().equals(displayName)) {
                  return loadoutType;
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private Inventory createLoadoutSelectionMenu() {
      List<LoadoutType> selectableLoadouts = this.loadoutManager.getSelectableLoadouts();
      int size = Math.max(9, ((selectableLoadouts.size() + 8) / 9) * 9);
      Inventory inventory = Bukkit.createInventory((InventoryHolder)null, size, "Select Loadout");

      for(int index = 0; index < selectableLoadouts.size(); ++index) {
         LoadoutType loadoutType = (LoadoutType)selectableLoadouts.get(index);
         inventory.setItem(index, this.createMenuItem(this.getLoadoutIcon(loadoutType), loadoutType.getDisplayName()));
      }

      return inventory;
   }

   private Material getLoadoutIcon(LoadoutType loadoutType) {
      switch(loadoutType) {
      case WARRIOR:
         return Material.DIAMOND_SWORD;
      case TANK:
         return Material.SHIELD;
      case POWERPAD:
         return Material.STONE_PRESSURE_PLATE;
      case STRIDER:
         return Material.IRON_BOOTS;
      case ARCHER:
         return Material.BOW;
      case FIRE:
         return Material.BLAZE_POWDER;
      case FORTUNE:
         return Material.IRON_PICKAXE;
      case GOD_APPLE:
         return Material.ENCHANTED_GOLDEN_APPLE;
      case DEFAULT:
      default:
         return Material.IRON_SWORD;
      }
   }

   private ItemStack createMenuItem(Material material, String displayName) {
      ItemStack itemStack = new ItemStack(material);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.displayName(Component.text(displayName));
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }
}
