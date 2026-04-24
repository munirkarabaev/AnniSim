package me.munir.annisim.listener;

import me.munir.annisim.team.TeamManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TeamQuitListener implements Listener {
   private final TeamManager teamManager;

   public TeamQuitListener(TeamManager teamManager) {
      this.teamManager = teamManager;
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.teamManager.clearTeam(event.getPlayer());
   }
}
