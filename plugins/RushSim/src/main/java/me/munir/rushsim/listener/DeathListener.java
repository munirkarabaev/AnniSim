package me.munir.rushsim.listener;

import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.team.Team;
import me.munir.rushsim.team.TeamManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathListener implements Listener {
   private final JavaPlugin plugin;
   private final GameManager gameManager;
   private final TeamManager teamManager;

   public DeathListener(JavaPlugin plugin, GameManager gameManager, TeamManager teamManager) {
      this.plugin = plugin;
      this.gameManager = gameManager;
      this.teamManager = teamManager;
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      if (!this.gameManager.isInGame()) {
         return;
      }

      Player player = event.getEntity();
      Team team = this.teamManager.getTeam(player);
      if (team == Team.RED) {
         this.gameManager.markRedDead(player);
         this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.spigot().respawn();
         });
      } else if (team == Team.BLUE) {
         this.plugin.getServer().getScheduler().runTask(this.plugin, player.spigot()::respawn);
      }
   }

   @EventHandler
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      if (!this.gameManager.isInGame()) {
         return;
      }

      Player player = event.getPlayer();
      Team team = this.teamManager.getTeam(player);
      if (team == Team.RED) {
         event.setRespawnLocation(this.gameManager.getArenaManager().getSpawn(Team.BLUE));
         this.plugin.getServer().getScheduler().runTask(this.plugin, () -> player.setGameMode(GameMode.SPECTATOR));
      } else if (team == Team.BLUE) {
         event.setRespawnLocation(this.gameManager.getArenaManager().getSpawn(Team.BLUE));
         this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.gameManager.preparePlayerForRound(player));
      }
   }
}
