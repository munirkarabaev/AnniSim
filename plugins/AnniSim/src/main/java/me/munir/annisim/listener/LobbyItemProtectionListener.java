package me.munir.annisim.listener;

import java.util.Iterator;
import me.munir.annisim.lobby.LobbyItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class LobbyItemProtectionListener implements Listener {
   private final LobbyItems lobbyItems;

   public LobbyItemProtectionListener(LobbyItems lobbyItems) {
      this.lobbyItems = lobbyItems;
   }

   @EventHandler
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      if (this.lobbyItems.isLobbyItem(event.getItemDrop().getItemStack())) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      if (this.lobbyItems.isLobbyItem(event.getItemInHand())) {
         event.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (this.lobbyItems.isLobbyItem(event.getCurrentItem()) || this.lobbyItems.isLobbyItem(event.getCursor())) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (this.lobbyItems.isLobbyItem(event.getOldCursor())) {
         event.setCancelled(true);
      } else {
         Iterator var2 = event.getNewItems().values().iterator();

         ItemStack itemStack;
         do {
            if (!var2.hasNext()) {
               return;
            }

            itemStack = (ItemStack)var2.next();
         } while(!this.lobbyItems.isLobbyItem(itemStack));

         event.setCancelled(true);
      }
   }
}
