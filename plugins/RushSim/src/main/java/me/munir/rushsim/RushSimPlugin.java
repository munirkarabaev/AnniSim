package me.munir.rushsim;

import java.util.Objects;
import me.munir.rushsim.arena.ArenaManager;
import me.munir.rushsim.command.ReloadCommand;
import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.game.NexusManager;
import me.munir.rushsim.game.RoundItems;
import me.munir.rushsim.kit.KitManager;
import me.munir.rushsim.kit.SuccubusManager;
import me.munir.rushsim.listener.AcrobatListener;
import me.munir.rushsim.listener.BlinkListener;
import me.munir.rushsim.listener.CombatListener;
import me.munir.rushsim.listener.DasherItemListener;
import me.munir.rushsim.listener.DeathListener;
import me.munir.rushsim.listener.KitSelectorListener;
import me.munir.rushsim.listener.LoadoutSelectorListener;
import me.munir.rushsim.listener.NexusListener;
import me.munir.rushsim.listener.SuccubusListener;
import me.munir.rushsim.listener.TeamSelectorListener;
import me.munir.rushsim.loadout.LoadoutManager;
import me.munir.rushsim.lobby.LobbyManager;
import me.munir.rushsim.scoreboard.ScoreboardManager;
import me.munir.rushsim.team.TeamManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RushSimPlugin extends JavaPlugin {
   private LobbyManager lobbyManager;
   private ArenaManager arenaManager;
   private TeamManager teamManager;
   private KitManager kitManager;
   private LoadoutManager loadoutManager;
   private GameManager gameManager;
   private TeamSelectorListener teamSelectorListener;
   private KitSelectorListener kitSelectorListener;
   private LoadoutSelectorListener loadoutSelectorListener;

   @Override
   public void onEnable() {
      this.saveDefaultConfig();
      this.lobbyManager = new LobbyManager(this);
      this.arenaManager = new ArenaManager(this);
      this.teamManager = new TeamManager();
      this.kitManager = new KitManager();
      RoundItems roundItems = new RoundItems(this);
      this.loadoutManager = new LoadoutManager(roundItems);
      NexusManager nexusManager = new NexusManager();
      this.gameManager = new GameManager(this, this.arenaManager, this.teamManager, this.kitManager, this.loadoutManager, nexusManager);
      SuccubusManager succubusManager = new SuccubusManager(this.gameManager, this.teamManager, this.kitManager, roundItems);
      this.lobbyManager.loadLobbyWorld();

      this.teamSelectorListener = new TeamSelectorListener(this.gameManager, this.teamManager);
      this.kitSelectorListener = new KitSelectorListener(this.kitManager);
      this.loadoutSelectorListener = new LoadoutSelectorListener(this.loadoutManager);

      this.getServer().getPluginManager().registerEvents(this.teamSelectorListener, this);
      this.getServer().getPluginManager().registerEvents(this.kitSelectorListener, this);
      this.getServer().getPluginManager().registerEvents(this.loadoutSelectorListener, this);
      this.getServer().getPluginManager().registerEvents(new DeathListener(this, this.gameManager, this.teamManager), this);
      this.getServer().getPluginManager().registerEvents(new NexusListener(this.gameManager, this.teamManager), this);
      this.getServer().getPluginManager().registerEvents(new CombatListener(this.gameManager, this.teamManager), this);
      this.getServer().getPluginManager().registerEvents(new AcrobatListener(this, this.gameManager, this.kitManager, this.teamManager), this);
      this.getServer().getPluginManager().registerEvents(new BlinkListener(this, this.gameManager, roundItems, this.kitManager), this);
      this.getServer().getPluginManager().registerEvents(new SuccubusListener(this, succubusManager), this);
      this.getServer().getPluginManager().registerEvents(new DasherItemListener(roundItems), this);

      ScoreboardManager scoreboardManager = new ScoreboardManager(this, this.gameManager, this.teamManager);
      scoreboardManager.start();

      ((PluginCommand)Objects.requireNonNull(this.getCommand("rushreload"))).setExecutor(new ReloadCommand(this, this.arenaManager));
   }

   public boolean isRoundActive() {
      return this.gameManager != null && this.gameManager.isRoundActive();
   }

   public void resetForModeSwitch() {
      if (this.gameManager != null && this.gameManager.isRoundActive()) {
         this.gameManager.endGame();
      }

      if (this.teamManager != null) {
         this.teamManager.clearTeams();
      }
   }

   public void preparePlayerForHubLobby(Player player) {
      this.gameManager.preparePlayerForLobby(player);
      this.lobbyManager.teleportToLobbySpawn(player);
   }

   public void openTeamSelectionMenu(Player player) {
      this.teamSelectorListener.openMenu(player);
   }

   public void openKitSelectionMenu(Player player) {
      this.kitSelectorListener.openMenu(player);
   }

   public void openLoadoutSelectionMenu(Player player) {
      this.loadoutSelectorListener.openMenu(player);
   }

   public boolean handleSharedStartGame(org.bukkit.command.CommandSender sender, String[] args) {
      return new me.munir.rushsim.command.StartGameCommand(this.gameManager, this.arenaManager).onCommand(sender, null, "startgame", args);
   }

   public boolean handleSharedEndGame(org.bukkit.command.CommandSender sender, String[] args) {
      return new me.munir.rushsim.command.EndGameCommand(this.gameManager).onCommand(sender, null, "endgame", args);
   }
}
