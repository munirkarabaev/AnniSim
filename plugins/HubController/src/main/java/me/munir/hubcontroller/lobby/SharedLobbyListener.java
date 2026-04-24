package me.munir.hubcontroller.lobby;

import me.munir.hubcontroller.HubControllerPlugin;
import me.munir.hubcontroller.util.PluginBridge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SharedLobbyListener implements Listener {
   private final HubControllerPlugin plugin;
   private final SharedLobbyItems sharedLobbyItems;

   public SharedLobbyListener(HubControllerPlugin plugin, SharedLobbyItems sharedLobbyItems) {
      this.plugin = plugin;
      this.sharedLobbyItems = sharedLobbyItems;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.plugin.preparePlayerForActiveLobby(player);
      this.sharedLobbyItems.applyToPlayer(player);
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getHand() != EquipmentSlot.HAND) {
         return;
      }

      Action action = event.getAction();
      if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
         return;
      }

      String activePluginName = this.plugin.getActiveGameplayPluginName();
      Player player = event.getPlayer();
      if (this.sharedLobbyItems.isTeamSelector(event.getItem())) {
         event.setCancelled(true);
         PluginBridge.openTeamSelectionMenu(activePluginName, player);
      } else if (this.sharedLobbyItems.isKitSelector(event.getItem())) {
         event.setCancelled(true);
         PluginBridge.openKitSelectionMenu(activePluginName, player);
      } else if (this.sharedLobbyItems.isLoadoutSelector(event.getItem())) {
         event.setCancelled(true);
         PluginBridge.openLoadoutSelectionMenu(activePluginName, player);
      }
   }
}
