package me.munir.rushsim.listener;

import me.munir.rushsim.loadout.LoadoutManager;
import me.munir.rushsim.loadout.LoadoutType;
import me.munir.rushsim.util.ModeAccess;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class LoadoutSelectorListener implements Listener {
   private final LoadoutManager loadoutManager;

   public LoadoutSelectorListener(LoadoutManager loadoutManager) {
      this.loadoutManager = loadoutManager;
   }

   public void openMenu(Player player) {
      if (ModeAccess.isRushSimActive()) {
         player.openInventory(this.loadoutManager.createSelectionMenu());
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (!ModeAccess.isRushSimActive() || !"Select Loadout".equals(event.getView().getTitle())) {
         return;
      }

      event.setCancelled(true);
      HumanEntity humanEntity = event.getWhoClicked();
      if (humanEntity instanceof Player player) {
         this.loadoutManager.setSelectedLoadout(player, LoadoutType.DEFAULT);
         player.sendMessage("You selected the Rush loadout.");
         player.closeInventory();
      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (ModeAccess.isRushSimActive() && "Select Loadout".equals(event.getView().getTitle())) {
         event.setCancelled(true);
      }
   }
}
