package me.munir.annisim.scoreboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.munir.annisim.game.GameManager;
import me.munir.annisim.game.WinTracker;
import me.munir.annisim.kit.SuccubusManager;
import me.munir.annisim.team.Team;
import me.munir.annisim.team.TeamManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {
   private static final String OBJECTIVE_NAME = "annisim";
   private static final String HEALTH_OBJECTIVE_NAME = "annisim_hp";
   private static final String TITLE = "§cA§en§an§bi§9S§di§cm";
   private static final String SUBTITLE = "§7by ILLUMINATE";
   private static final String BLANK_LINE_ONE = " ";
   private static final String BLANK_LINE_TWO = " §r";
   private static final String BLANK_LINE_THREE = "  ";
   private static final String BLANK_LINE_FOUR = "   ";
   private static final String RED_HEADER = "§c§lRED";
   private static final String BLUE_HEADER = "§9§lBLUE";
   private static final String SPECTATOR_HEADER = "§7§lSPECTATORS";
   private static final String RED_SCORE_ENTRY = ChatColor.RED.toString();
   private static final String RED_DIAMONDS_ENTRY = ChatColor.DARK_RED.toString();
   private static final String BLUE_SCORE_ENTRY = ChatColor.BLUE.toString();
   private static final String BLUE_DIAMONDS_ENTRY = ChatColor.DARK_BLUE.toString();
   private static final String SPECTATOR_COUNT_ENTRY = ChatColor.GRAY.toString();
   private static final String FOOTER_ENTRY = ChatColor.YELLOW.toString();
   private final JavaPlugin plugin;
   private final GameManager gameManager;
   private final TeamManager teamManager;
   private final SuccubusManager succubusManager;
   private final WinTracker winTracker;
   private final Map<UUID, PlayerScoreboard> playerScoreboards = new HashMap();
   private int taskId = -1;

   public ScoreboardManager(JavaPlugin plugin, GameManager gameManager, TeamManager teamManager, SuccubusManager succubusManager, WinTracker winTracker) {
      this.plugin = plugin;
      this.gameManager = gameManager;
      this.teamManager = teamManager;
      this.succubusManager = succubusManager;
      this.winTracker = winTracker;
   }

   public void start() {
      if (this.taskId == -1) {
         this.updateScoreboards();
         this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::updateScoreboards, 5L, 5L);
      }
   }

   public void stop() {
      if (this.taskId != -1) {
         Bukkit.getScheduler().cancelTask(this.taskId);
         this.taskId = -1;
      }

      Iterator<? extends Player> players = Bukkit.getOnlinePlayers().iterator();

      while(players.hasNext()) {
         Player player = (Player)players.next();
         player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
      }

      Iterator<PlayerScoreboard> scoreboards = this.playerScoreboards.values().iterator();

      while(scoreboards.hasNext()) {
         PlayerScoreboard playerScoreboard = (PlayerScoreboard)scoreboards.next();
         playerScoreboard.unregister();
      }

      this.playerScoreboards.clear();
   }

   private void updateScoreboards() {
      Set<UUID> onlinePlayerIds = new HashSet();
      Iterator<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers().iterator();

      Player player;
      while(onlinePlayers.hasNext()) {
         player = (Player)onlinePlayers.next();
         onlinePlayerIds.add(player.getUniqueId());
      }

      this.playerScoreboards.entrySet().removeIf((entry) -> {
         if (onlinePlayerIds.contains(entry.getKey())) {
            return false;
         } else {
            ((PlayerScoreboard)entry.getValue()).unregister();
            return true;
         }
      });
      onlinePlayers = Bukkit.getOnlinePlayers().iterator();

      while(onlinePlayers.hasNext()) {
         player = (Player)onlinePlayers.next();
         PlayerScoreboard playerScoreboard = (PlayerScoreboard)this.playerScoreboards.computeIfAbsent(player.getUniqueId(), (ignored) -> {
            return this.createScoreboard();
         });
         this.updateLines(playerScoreboard);
         this.updateHealthDisplay(player, playerScoreboard);
         player.setScoreboard(playerScoreboard.scoreboard());
      }
   }

   private PlayerScoreboard createScoreboard() {
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
      Objective sidebarObjective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy", TITLE);
      sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
      Objective healthObjective = scoreboard.registerNewObjective(HEALTH_OBJECTIVE_NAME, "dummy", "HP");
      healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);

      org.bukkit.scoreboard.Team redScoreLine = this.createSidebarLine(scoreboard, "redScore", RED_SCORE_ENTRY);
      org.bukkit.scoreboard.Team redDiamondsLine = this.createSidebarLine(scoreboard, "redDiamonds", RED_DIAMONDS_ENTRY);
      org.bukkit.scoreboard.Team blueScoreLine = this.createSidebarLine(scoreboard, "blueScore", BLUE_SCORE_ENTRY);
      org.bukkit.scoreboard.Team blueDiamondsLine = this.createSidebarLine(scoreboard, "blueDiamonds", BLUE_DIAMONDS_ENTRY);
      org.bukkit.scoreboard.Team spectatorCountLine = this.createSidebarLine(scoreboard, "spectatorCount", SPECTATOR_COUNT_ENTRY);
      org.bukkit.scoreboard.Team timeLine = this.createSidebarLine(scoreboard, "timeLine", FOOTER_ENTRY);

      sidebarObjective.getScore(SUBTITLE).setScore(14);
      sidebarObjective.getScore(BLANK_LINE_ONE).setScore(13);
      sidebarObjective.getScore(RED_HEADER).setScore(12);
      sidebarObjective.getScore(RED_SCORE_ENTRY).setScore(11);
      sidebarObjective.getScore(RED_DIAMONDS_ENTRY).setScore(10);
      sidebarObjective.getScore(BLANK_LINE_TWO).setScore(9);
      sidebarObjective.getScore(BLUE_HEADER).setScore(8);
      sidebarObjective.getScore(BLUE_SCORE_ENTRY).setScore(7);
      sidebarObjective.getScore(BLUE_DIAMONDS_ENTRY).setScore(6);
      sidebarObjective.getScore(BLANK_LINE_THREE).setScore(5);
      sidebarObjective.getScore(SPECTATOR_HEADER).setScore(4);
      sidebarObjective.getScore(SPECTATOR_COUNT_ENTRY).setScore(3);
      sidebarObjective.getScore(BLANK_LINE_FOUR).setScore(2);
      sidebarObjective.getScore(FOOTER_ENTRY).setScore(1);

      return new PlayerScoreboard(scoreboard, healthObjective, redScoreLine, redDiamondsLine, blueScoreLine, blueDiamondsLine, spectatorCountLine, timeLine);
   }

   private org.bukkit.scoreboard.Team createSidebarLine(Scoreboard scoreboard, String teamName, String entry) {
      org.bukkit.scoreboard.Team lineTeam = scoreboard.registerNewTeam(teamName);
      lineTeam.addEntry(entry);
      return lineTeam;
   }

   private void updateLines(PlayerScoreboard playerScoreboard) {
      if (this.gameManager.isInGame()) {
         playerScoreboard.redScoreLine().prefix(Component.text("§fScore: " + this.winTracker.getWins(Team.RED)));
         playerScoreboard.redDiamondsLine().prefix(Component.text("§fDiamonds: " + this.gameManager.getDiamonds(Team.RED)));
         playerScoreboard.blueScoreLine().prefix(Component.text("§fScore: " + this.winTracker.getWins(Team.BLUE)));
         playerScoreboard.blueDiamondsLine().prefix(Component.text("§fDiamonds: " + this.gameManager.getDiamonds(Team.BLUE)));
         playerScoreboard.spectatorCountLine().prefix(Component.text("§fPlayers: " + this.teamManager.getAssignedPlayerCount(Team.SPECTATOR)));
         playerScoreboard.timeLine().prefix(Component.text("§eTime: " + this.formatTime(this.gameManager.getGameDurationSeconds())));
      } else {
         playerScoreboard.redScoreLine().prefix(Component.text("§fWins: " + this.winTracker.getWins(Team.RED)));
         playerScoreboard.redDiamondsLine().prefix(Component.text("§fReady"));
         playerScoreboard.blueScoreLine().prefix(Component.text("§fWins: " + this.winTracker.getWins(Team.BLUE)));
         playerScoreboard.blueDiamondsLine().prefix(Component.text("§fReady"));
         playerScoreboard.spectatorCountLine().prefix(Component.text("§fPlayers: " + this.teamManager.getAssignedPlayerCount(Team.SPECTATOR)));
         playerScoreboard.timeLine().prefix(Component.text("§eRed vs Blue"));
      }

      this.teamManager.applyNameTags(playerScoreboard.scoreboard());
   }

   private void updateHealthDisplay(Player viewer, PlayerScoreboard playerScoreboard) {
      Set<String> onlinePlayerNames = new HashSet();
      Iterator<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers().iterator();

      while(onlinePlayers.hasNext()) {
         Player observed = (Player)onlinePlayers.next();
         onlinePlayerNames.add(observed.getName());
         int displayedHealth = 0;
         if (this.gameManager.isInGame() && this.succubusManager.shouldShowEnemyHealth(viewer, observed)) {
            displayedHealth = this.succubusManager.getDisplayedHealth(observed);
         }

         playerScoreboard.healthObjective().getScore(observed.getName()).setScore(displayedHealth);
         playerScoreboard.trackedPlayers().add(observed.getName());
      }

      playerScoreboard.trackedPlayers().removeIf((entry) -> {
         if (onlinePlayerNames.contains(entry)) {
            return false;
         } else {
            playerScoreboard.scoreboard().resetScores(entry);
            return true;
         }
      });
   }

   private String formatTime(long totalSeconds) {
      long minutes = totalSeconds / 60L;
      long seconds = totalSeconds % 60L;
      return String.format("%02d:%02d", minutes, seconds);
   }

   private static record PlayerScoreboard(Scoreboard scoreboard, Objective healthObjective, org.bukkit.scoreboard.Team redScoreLine, org.bukkit.scoreboard.Team redDiamondsLine, org.bukkit.scoreboard.Team blueScoreLine, org.bukkit.scoreboard.Team blueDiamondsLine, org.bukkit.scoreboard.Team spectatorCountLine, org.bukkit.scoreboard.Team timeLine, Set<String> trackedPlayers) {
      private PlayerScoreboard(Scoreboard scoreboard, Objective healthObjective, org.bukkit.scoreboard.Team redScoreLine, org.bukkit.scoreboard.Team redDiamondsLine, org.bukkit.scoreboard.Team blueScoreLine, org.bukkit.scoreboard.Team blueDiamondsLine, org.bukkit.scoreboard.Team spectatorCountLine, org.bukkit.scoreboard.Team timeLine) {
         this(scoreboard, healthObjective, redScoreLine, redDiamondsLine, blueScoreLine, blueDiamondsLine, spectatorCountLine, timeLine, new HashSet());
      }

      private PlayerScoreboard(Scoreboard scoreboard, Objective healthObjective, org.bukkit.scoreboard.Team redScoreLine, org.bukkit.scoreboard.Team redDiamondsLine, org.bukkit.scoreboard.Team blueScoreLine, org.bukkit.scoreboard.Team blueDiamondsLine, org.bukkit.scoreboard.Team spectatorCountLine, org.bukkit.scoreboard.Team timeLine, Set<String> trackedPlayers) {
         this.scoreboard = scoreboard;
         this.healthObjective = healthObjective;
         this.redScoreLine = redScoreLine;
         this.redDiamondsLine = redDiamondsLine;
         this.blueScoreLine = blueScoreLine;
         this.blueDiamondsLine = blueDiamondsLine;
         this.spectatorCountLine = spectatorCountLine;
         this.timeLine = timeLine;
         this.trackedPlayers = trackedPlayers;
      }

      private void unregister() {
         if (this.healthObjective != null) {
            this.healthObjective.unregister();
         }

         if (this.redScoreLine != null) {
            this.redScoreLine.unregister();
         }

         if (this.redDiamondsLine != null) {
            this.redDiamondsLine.unregister();
         }

         if (this.blueScoreLine != null) {
            this.blueScoreLine.unregister();
         }

         if (this.blueDiamondsLine != null) {
            this.blueDiamondsLine.unregister();
         }

         if (this.spectatorCountLine != null) {
            this.spectatorCountLine.unregister();
         }

         if (this.timeLine != null) {
            this.timeLine.unregister();
         }
      }
   }
}
