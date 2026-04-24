package me.munir.rushsim.scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.team.Team;
import me.munir.rushsim.team.TeamManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {
   private final JavaPlugin plugin;
   private final GameManager gameManager;
   private final TeamManager teamManager;
   private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();
   private int taskId = -1;

   public ScoreboardManager(JavaPlugin plugin, GameManager gameManager, TeamManager teamManager) {
      this.plugin = plugin;
      this.gameManager = gameManager;
      this.teamManager = teamManager;
   }

   public void start() {
      if (this.taskId == -1) {
         this.update();
         this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::update, 20L, 20L);
      }
   }

   private void update() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         Scoreboard scoreboard = this.scoreboards.computeIfAbsent(player.getUniqueId(), (ignored) -> this.createScoreboard());
         Objective objective = scoreboard.getObjective("rushsim");
         if (objective == null) {
            continue;
         }

         scoreboard.getEntries().forEach(scoreboard::resetScores);
         objective.getScore("§cRushSim").setScore(6);
         objective.getScore("§fRed Alive: " + this.teamManager.getPlayerCount(Team.RED)).setScore(5);
         objective.getScore("§fBlue Alive: " + this.teamManager.getPlayerCount(Team.BLUE)).setScore(4);
         objective.getScore("§fNexus HP: " + this.gameManager.getNexusHealth()).setScore(3);
         objective.getScore("§fTime: " + this.formatTime(this.gameManager.getGameDurationSeconds())).setScore(2);
         objective.getScore(ChatColor.DARK_GRAY.toString()).setScore(1);
         player.setScoreboard(scoreboard);
      }
   }

   private Scoreboard createScoreboard() {
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
      Objective objective = scoreboard.registerNewObjective("rushsim", "dummy", Component.text("RushSim"));
      objective.setDisplaySlot(DisplaySlot.SIDEBAR);
      return scoreboard;
   }

   private String formatTime(long totalSeconds) {
      long minutes = totalSeconds / 60L;
      long seconds = totalSeconds % 60L;
      return String.format("%02d:%02d", minutes, seconds);
   }
}
