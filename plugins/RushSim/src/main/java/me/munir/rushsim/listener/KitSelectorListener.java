package me.munir.rushsim.listener;

import me.munir.rushsim.kit.Kit;
import me.munir.rushsim.kit.KitManager;
import me.munir.rushsim.util.ModeAccess;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitSelectorListener implements Listener {
   private final KitManager kitManager;

   public KitSelectorListener(KitManager kitManager) {
      this.kitManager = kitManager;
   }

   public void openMenu(Player player) {
      if (ModeAccess.isRushSimActive()) {
         player.openInventory(this.createMenu());
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (!ModeAccess.isRushSimActive() || !"Select Kit".equals(event.getView().getTitle())) {
         return;
      }

      event.setCancelled(true);
      HumanEntity humanEntity = event.getWhoClicked();
      if (!(humanEntity instanceof Player player)) {
         return;
      }

      ItemStack itemStack = event.getCurrentItem();
      if (itemStack == null || !itemStack.hasItemMeta() || itemStack.getItemMeta() == null || !itemStack.getItemMeta().hasDisplayName()) {
         return;
      }

      String displayName = itemStack.getItemMeta().getDisplayName();
      if ("Dasher Kit".equals(displayName)) {
         this.kitManager.setKit(player, Kit.DASHER);
      } else if ("Succubus Kit".equals(displayName)) {
         this.kitManager.setKit(player, Kit.SUCCUBUS);
      } else if ("Acrobat Kit".equals(displayName)) {
         this.kitManager.setKit(player, Kit.ACROBAT);
      } else {
         return;
      }

      player.sendMessage("You selected " + displayName + ".");
      player.closeInventory();
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (ModeAccess.isRushSimActive() && "Select Kit".equals(event.getView().getTitle())) {
         event.setCancelled(true);
      }
   }

   private Inventory createMenu() {
      Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 9, "Select Kit");
      inventory.setItem(2, this.createItem(Material.ENDER_PEARL, "Dasher Kit"));
      inventory.setItem(4, this.createItem(Material.FEATHER, "Acrobat Kit"));
      inventory.setItem(6, this.createItem(Material.RED_DYE, "Succubus Kit"));
      return inventory;
   }

   private ItemStack createItem(Material material, String displayName) {
      ItemStack itemStack = new ItemStack(material);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.displayName(Component.text(displayName));
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }
}
