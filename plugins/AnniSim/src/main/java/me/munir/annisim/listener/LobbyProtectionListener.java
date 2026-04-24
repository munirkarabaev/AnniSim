package me.munir.annisim.listener;

import me.munir.annisim.lobby.LobbyManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class LobbyProtectionListener implements Listener {
   private final LobbyManager lobbyManager;

   public LobbyProtectionListener(LobbyManager lobbyManager) {
      this.lobbyManager = lobbyManager;
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (this.lobbyManager.isLobbyWorld(event.getBlock().getWorld()) && !event.getPlayer().isOp()) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      if (this.lobbyManager.isLobbyWorld(event.getBlock().getWorld()) && !event.getPlayer().isOp()) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onEntityDamage(EntityDamageEvent event) {
      if (this.lobbyManager.isLobbyWorld(event.getEntity().getWorld())) {
         event.setCancelled(true);
      }

   }
}
