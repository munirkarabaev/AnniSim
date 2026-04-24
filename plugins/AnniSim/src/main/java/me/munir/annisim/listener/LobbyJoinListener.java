package me.munir.annisim.listener;

import me.munir.annisim.lobby.LobbyItems;
import me.munir.annisim.lobby.LobbyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.PlayerInventory;

public class LobbyJoinListener implements Listener {
   private final LobbyManager lobbyManager;
   private final LobbyItems lobbyItems;

   public LobbyJoinListener(LobbyManager lobbyManager, LobbyItems lobbyItems) {
      this.lobbyManager = lobbyManager;
      this.lobbyItems = lobbyItems;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      PlayerInventory inventory = player.getInventory();
      this.lobbyManager.teleportToLobbySpawn(player);
      inventory.clear();
      inventory.setItem(0, this.lobbyItems.createTeamSelector());
      inventory.setItem(4, this.lobbyItems.createKitSelector());
      inventory.setItem(8, this.lobbyItems.createLoadoutSelector());
   }
}
