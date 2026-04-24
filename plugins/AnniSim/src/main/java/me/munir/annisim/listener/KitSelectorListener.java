package me.munir.annisim.listener;

import me.munir.annisim.kit.Kit;
import me.munir.annisim.kit.KitManager;
import me.munir.annisim.lobby.LobbyItems;
import me.munir.annisim.util.ModeAccess;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitSelectorListener implements Listener {
   private static final String KIT_MENU_TITLE = "Select Kit";
   private static final String DASHER_KIT_NAME = "Dasher Kit";
   private static final String SUCCUBUS_KIT_NAME = "Succubus Kit";
   private static final String ACROBAT_KIT_NAME = "Acrobat Kit";
   private final LobbyItems lobbyItems;
   private final KitManager kitManager;

   public KitSelectorListener(LobbyItems lobbyItems, KitManager kitManager) {
      this.lobbyItems = lobbyItems;
      this.kitManager = kitManager;
   }

    public void openMenu(Player player) {
      if (ModeAccess.isAnniSimActive()) {
         player.openInventory(this.createKitSelectionMenu());
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (!ModeAccess.isAnniSimActive()) {
         return;
      }

      if (event.getHand() == EquipmentSlot.HAND) {
         Action action = event.getAction();
         if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (this.lobbyItems.isKitSelector(event.getItem())) {
               event.setCancelled(true);
               event.getPlayer().openInventory(this.createKitSelectionMenu());
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (!ModeAccess.isAnniSimActive()) {
         return;
      }

      if ("Select Kit".equals(event.getView().getTitle())) {
         event.setCancelled(true);
         HumanEntity var3 = event.getWhoClicked();
         if (var3 instanceof Player) {
            Player player = (Player)var3;
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta()) {
               ItemMeta itemMeta = clickedItem.getItemMeta();
               if (itemMeta != null && itemMeta.hasDisplayName()) {
                  if ("Dasher Kit".equals(itemMeta.getDisplayName())) {
                     this.kitManager.setKit(player, Kit.DASHER);
                     player.closeInventory();
                     player.sendMessage("You selected the Dasher kit.");
                  } else {
                     if ("Succubus Kit".equals(itemMeta.getDisplayName())) {
                        this.kitManager.setKit(player, Kit.SUCCUBUS);
                        player.closeInventory();
                        player.sendMessage("You selected the Succubus kit.");
                     } else if ("Acrobat Kit".equals(itemMeta.getDisplayName())) {
                        this.kitManager.setKit(player, Kit.ACROBAT);
                        player.closeInventory();
                        player.sendMessage("You selected the Acrobat kit.");
                     }

                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (!ModeAccess.isAnniSimActive()) {
         return;
      }

      if ("Select Kit".equals(event.getView().getTitle())) {
         event.setCancelled(true);
      }

   }

   private Inventory createKitSelectionMenu() {
      Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 9, "Select Kit");
      inventory.setItem(2, this.createMenuItem(Material.ENDER_PEARL, "Dasher Kit"));
      inventory.setItem(4, this.createMenuItem(Material.FEATHER, "Acrobat Kit"));
      inventory.setItem(6, this.createMenuItem(Material.RED_DYE, "Succubus Kit"));
      return inventory;
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
