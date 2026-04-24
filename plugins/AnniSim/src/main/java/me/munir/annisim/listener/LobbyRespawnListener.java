package me.munir.annisim.listener;

import me.munir.annisim.arena.ArenaManager;
import me.munir.annisim.game.GameManager;
import me.munir.annisim.game.GameState;
import me.munir.annisim.lobby.LobbyManager;
import me.munir.annisim.team.Team;
import me.munir.annisim.util.ModeAccess;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class LobbyRespawnListener implements Listener {
   private final LobbyManager lobbyManager;
   private final ArenaManager arenaManager;
   private final GameManager gameManager;

   public LobbyRespawnListener(LobbyManager lobbyManager, ArenaManager arenaManager, GameManager gameManager) {
      this.lobbyManager = lobbyManager;
      this.arenaManager = arenaManager;
      this.gameManager = gameManager;
   }

   @EventHandler
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      if (!ModeAccess.isAnniSimActive()) {
         return;
      }

      if (this.gameManager.getGameState() == GameState.IN_GAME) {
         if (this.gameManager.getSpectatorRespawnLocation(event.getPlayer()) != null) {
            event.setRespawnLocation(this.gameManager.getSpectatorRespawnLocation(event.getPlayer()));
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
         } else {
            Team team = this.gameManager.getTeamManager().getTeam(event.getPlayer());
            if (team == Team.SPECTATOR) {
               Location spectatorSpawn = this.arenaManager.getArenaSpawnLocation(Team.RED);
               if (spectatorSpawn != null) {
                  event.setRespawnLocation(spectatorSpawn);
               }

               event.getPlayer().setGameMode(GameMode.SPECTATOR);
            } else {
               Location arenaSpawn = this.arenaManager.getArenaSpawnLocation(team);
               if (arenaSpawn != null) {
                  event.setRespawnLocation(arenaSpawn);
               }

               event.getPlayer().setGameMode(GameMode.SURVIVAL);
            }
         }
      } else {
         Location lobbySpawn = this.lobbyManager.getLobbySpawnLocation();
         if (lobbySpawn != null) {
            event.setRespawnLocation(lobbySpawn);
         }

         event.getPlayer().setGameMode(GameMode.ADVENTURE);
      }
   }
}
