package me.munir.annisim.team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class TeamManager {
   private final Map<UUID, Team> playerTeams = new HashMap();
   private final Set<UUID> deadPlayers = new HashSet();
   private final Set<UUID> roundParticipants = new HashSet();

   public void setTeam(Player player, Team team) {
      this.playerTeams.put(player.getUniqueId(), team);
      this.deadPlayers.remove(player.getUniqueId());
      this.updateNameTag(player, team);
   }

   public void clearTeam(Player player) {
      this.playerTeams.remove(player.getUniqueId());
      this.deadPlayers.remove(player.getUniqueId());
      this.updateNameTag(player, (Team)null);
   }

   public Team getTeam(Player player) {
      return (Team)this.playerTeams.get(player.getUniqueId());
   }

   public boolean isActivePlayer(Player player) {
      Team team = this.getTeam(player);
      return team != null && team.isPlayingTeam() && this.roundParticipants.contains(player.getUniqueId()) && !this.deadPlayers.contains(player.getUniqueId());
   }

   public int getPlayerCount(Team team) {
      int count = 0;
      Iterator var3 = Bukkit.getOnlinePlayers().iterator();

      while(var3.hasNext()) {
         Player player = (Player)var3.next();
         if (team == this.getTeam(player) && this.isActivePlayer(player)) {
            ++count;
         }
      }

      return count;
   }

   public void markDead(Player player) {
      if (this.getTeam(player) != null) {
         this.deadPlayers.add(player.getUniqueId());
      }

   }

   public void resetDeadPlayers() {
      this.deadPlayers.clear();
   }

   public void beginRound() {
      this.roundParticipants.clear();
      Iterator var1 = Bukkit.getOnlinePlayers().iterator();

      while(var1.hasNext()) {
         Player player = (Player)var1.next();
         Team team = this.getTeam(player);
         if (team != null && team.isPlayingTeam()) {
            this.roundParticipants.add(player.getUniqueId());
         }
      }
   }

   public void clearRoundParticipants() {
      this.roundParticipants.clear();
   }

   public void clearTeams() {
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
      Team[] var3 = Team.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Team team = var3[var5];
         org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(this.getScoreboardTeamName(team));
         if (scoreboardTeam != null) {
            Iterator var8 = (new HashSet(scoreboardTeam.getEntries())).iterator();

            while(var8.hasNext()) {
               String entry = (String)var8.next();
               scoreboardTeam.removeEntry(entry);
            }
         }
      }

      this.playerTeams.clear();
      this.deadPlayers.clear();
      this.roundParticipants.clear();
   }

   public void applyNameTags(Scoreboard scoreboard) {
      Map<Team, org.bukkit.scoreboard.Team> scoreboardTeams = new HashMap();
      Team[] var3 = Team.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Team team = var3[var5];
         org.bukkit.scoreboard.Team scoreboardTeam = this.getOrCreateScoreboardTeam(scoreboard, team);
         scoreboardTeams.put(team, scoreboardTeam);
         Iterator var8 = (new HashSet(scoreboardTeam.getEntries())).iterator();

         while(var8.hasNext()) {
            String entry = (String)var8.next();
            scoreboardTeam.removeEntry(entry);
         }
      }

      Iterator var9 = Bukkit.getOnlinePlayers().iterator();

      while(var9.hasNext()) {
         Player player = (Player)var9.next();
         Team team = this.getTeam(player);
         if (team != null) {
            ((org.bukkit.scoreboard.Team)scoreboardTeams.get(team)).addEntry(player.getName());
         }
      }

   }

   private void updateNameTag(Player player, Team team) {
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
      Team[] var4 = Team.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Team existingTeam = var4[var6];
         this.getOrCreateScoreboardTeam(scoreboard, existingTeam).removeEntry(player.getName());
      }

      if (team != null) {
         this.getOrCreateScoreboardTeam(scoreboard, team).addEntry(player.getName());
      }
   }

   public int getAssignedPlayerCount(Team team) {
      int count = 0;
      Iterator var3 = Bukkit.getOnlinePlayers().iterator();

      while(var3.hasNext()) {
         Player player = (Player)var3.next();
         if (team == this.getTeam(player)) {
            ++count;
         }
      }

      return count;
   }

   private org.bukkit.scoreboard.Team getOrCreateScoreboardTeam(Scoreboard scoreboard, Team team) {
      String teamName = this.getScoreboardTeamName(team);
      org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(teamName);
      if (scoreboardTeam == null) {
         scoreboardTeam = scoreboard.registerNewTeam(teamName);
      }

      scoreboardTeam.color(team.getTextColor());
      return scoreboardTeam;
   }

   private String getScoreboardTeamName(Team team) {
      return "annisim_" + team.name().toLowerCase();
   }
}
