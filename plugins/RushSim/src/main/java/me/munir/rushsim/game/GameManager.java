package me.munir.rushsim.game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.munir.rushsim.arena.ArenaManager;
import me.munir.rushsim.kit.Kit;
import me.munir.rushsim.kit.KitManager;
import me.munir.rushsim.loadout.LoadoutManager;
import me.munir.rushsim.team.Team;
import me.munir.rushsim.team.TeamManager;
import me.munir.rushsim.util.ModeAccess;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class GameManager {
   private final JavaPlugin plugin;
   private final ArenaManager arenaManager;
   private final TeamManager teamManager;
   private final KitManager kitManager;
   private final LoadoutManager loadoutManager;
   private final NexusManager nexusManager;
   private GameState gameState = GameState.LOBBY;
   private long gameStartTime;
   private final Map<UUID, Long> defenderInvulnerabilityEndTimes = new HashMap<>();

   public GameManager(JavaPlugin plugin, ArenaManager arenaManager, TeamManager teamManager, KitManager kitManager, LoadoutManager loadoutManager, NexusManager nexusManager) {
      this.plugin = plugin;
      this.arenaManager = arenaManager;
      this.teamManager = teamManager;
      this.kitManager = kitManager;
      this.loadoutManager = loadoutManager;
      this.nexusManager = nexusManager;
   }

   public boolean startGame(String mapName) {
      if (!ModeAccess.isRushSimActive() || this.gameState != GameState.LOBBY) {
         return false;
      }

      if (!this.arenaManager.selectMap(mapName) || !this.arenaManager.resetArenaWorld()) {
         return false;
      }

      this.gameState = GameState.IN_GAME;
      this.gameStartTime = System.currentTimeMillis();
      this.nexusManager.reset();
      this.teamManager.resetDeadPlayers();
      this.teamManager.beginRound();
      this.defenderInvulnerabilityEndTimes.clear();

      for (Player player : Bukkit.getOnlinePlayers()) {
         Team team = this.teamManager.getTeam(player);
         if (team == Team.RED || team == Team.BLUE) {
            this.preparePlayerForRound(player);
         } else {
            this.preparePlayerForLobby(player);
         }
      }

      return true;
   }

   public boolean endGame() {
      if (this.gameState != GameState.IN_GAME) {
         return false;
      }

      this.gameState = GameState.LOBBY;
      this.teamManager.clearRoundParticipants();
      this.teamManager.resetDeadPlayers();
      this.defenderInvulnerabilityEndTimes.clear();
      this.nexusManager.reset();
      for (Player player : Bukkit.getOnlinePlayers()) {
         this.preparePlayerForLobby(player);
      }

      this.arenaManager.resetArenaWorld();
      return true;
   }

   public boolean isInGame() {
      return this.gameState == GameState.IN_GAME && ModeAccess.isRushSimActive();
   }

   public boolean isRoundActive() {
      return this.gameState == GameState.IN_GAME;
   }

   public long getGameDurationSeconds() {
      return this.gameStartTime == 0L ? 0L : (System.currentTimeMillis() - this.gameStartTime) / 1000L;
   }

   public int getNexusHealth() {
      return this.nexusManager.getHealth();
   }

   public void preparePlayerForLobby(Player player) {
      player.getInventory().clear();
      player.setAllowFlight(false);
      player.setFlying(false);
      player.getActivePotionEffects().forEach((potionEffect) -> player.removePotionEffect(potionEffect.getType()));
      player.setGameMode(GameMode.ADVENTURE);
   }

   public void preparePlayerForRound(Player player) {
      player.getInventory().clear();
      player.getActivePotionEffects().forEach((potionEffect) -> player.removePotionEffect(potionEffect.getType()));
      Team team = this.teamManager.getTeam(player);
      if (team == Team.SPECTATOR) {
         player.setGameMode(GameMode.SPECTATOR);
         this.arenaManager.teleportToSpawn(player, Team.BLUE);
         return;
      }

      this.arenaManager.teleportToSpawn(player, team);
      player.setGameMode(GameMode.SURVIVAL);
      this.loadoutManager.applyLoadout(player);
      ItemStack kitItem = this.loadoutManager.getKitItem(String.valueOf(this.kitManager.getKit(player)));
      if (kitItem != null) {
         player.getInventory().addItem(kitItem);
      }

      if (team == Team.BLUE) {
         this.applyDefenderInvulnerability(player);
      }
   }

   public void markRedDead(Player player) {
      this.teamManager.markDead(player);
      this.checkWinConditions();
   }

   public void checkWinConditions() {
      if (!this.isInGame()) {
         return;
      }

      if (this.nexusManager.getHealth() <= 0) {
         this.handleWin(Team.RED);
      } else if (this.teamManager.getPlayerCount(Team.RED) <= 0) {
         this.handleWin(Team.BLUE);
      }
   }

   public void handleNexusBreak() {
      this.checkWinConditions();
   }

   public void handleWin(Team team) {
      for (Player player : Bukkit.getOnlinePlayers()) {
         player.sendTitle(team == Team.RED ? "§cRUSHERS WIN" : "§9DEFENDERS WIN", "", 10, 80, 10);
      }

      Bukkit.getScheduler().runTaskLater(this.plugin, this::endGame, 100L);
   }

   public boolean isDefenderInvulnerable(Player player) {
      Long endTime = this.defenderInvulnerabilityEndTimes.get(player.getUniqueId());
      if (endTime == null) {
         return false;
      }

      if (endTime <= System.currentTimeMillis()) {
         this.defenderInvulnerabilityEndTimes.remove(player.getUniqueId());
         return false;
      }

      return true;
   }

   public void applyDefenderInvulnerability(Player player) {
      this.defenderInvulnerabilityEndTimes.put(player.getUniqueId(), System.currentTimeMillis() + 4000L);
   }

   public void removeDefenderInvulnerability(Player player) {
      this.defenderInvulnerabilityEndTimes.remove(player.getUniqueId());
   }

   public ArenaManager getArenaManager() {
      return this.arenaManager;
   }

   public TeamManager getTeamManager() {
      return this.teamManager;
   }

   public NexusManager getNexusManager() {
      return this.nexusManager;
   }
}
