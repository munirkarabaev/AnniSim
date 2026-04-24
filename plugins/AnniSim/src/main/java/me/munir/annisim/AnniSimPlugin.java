package me.munir.annisim;

import java.util.Objects;
import me.munir.annisim.arena.ArenaManager;
import me.munir.annisim.chat.ChatMuteManager;
import me.munir.annisim.command.EndGameCommand;
import me.munir.annisim.command.BanLoadoutCommand;
import me.munir.annisim.command.CommandProtectionManager;
import me.munir.annisim.command.LoadoutTabCompleter;
import me.munir.annisim.command.MuteCommand;
import me.munir.annisim.command.ProtectCommand;
import me.munir.annisim.command.ResetCommand;
import me.munir.annisim.command.StartGameCommand;
import me.munir.annisim.command.StartGameTabCompleter;
import me.munir.annisim.command.UnbanLoadoutCommand;
import me.munir.annisim.game.GameManager;
import me.munir.annisim.game.RoundItems;
import me.munir.annisim.game.WinTracker;
import me.munir.annisim.kit.KitManager;
import me.munir.annisim.kit.SuccubusManager;
import me.munir.annisim.listener.ArenaMobSpawnListener;
import me.munir.annisim.listener.AcrobatListener;
import me.munir.annisim.listener.BlinkListener;
import me.munir.annisim.listener.DasherItemListener;
import me.munir.annisim.listener.DiamondBlockProtectionListener;
import me.munir.annisim.listener.DiamondListener;
import me.munir.annisim.listener.GameDeathListener;
import me.munir.annisim.listener.GameWinListener;
import me.munir.annisim.listener.GodAppleConsumeListener;
import me.munir.annisim.listener.IronArmorDurabilityListener;
import me.munir.annisim.listener.KitSelectorListener;
import me.munir.annisim.listener.LaunchPadListener;
import me.munir.annisim.listener.LoadoutSelectorListener;
import me.munir.annisim.listener.LobbyItemProtectionListener;
import me.munir.annisim.listener.LobbyJoinListener;
import me.munir.annisim.listener.LobbyProtectionListener;
import me.munir.annisim.listener.LobbyRespawnListener;
import me.munir.annisim.listener.SuccubusListener;
import me.munir.annisim.listener.TeamChatListener;
import me.munir.annisim.listener.TeamDamageListener;
import me.munir.annisim.listener.TeamQuitListener;
import me.munir.annisim.listener.TeamSelectorListener;
import me.munir.annisim.loadout.LoadoutManager;
import me.munir.annisim.lobby.LobbyItems;
import me.munir.annisim.lobby.LobbyManager;
import me.munir.annisim.scoreboard.ScoreboardManager;
import me.munir.annisim.team.TeamManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class AnniSimPlugin extends JavaPlugin {
   private LobbyManager lobbyManager;
   private TeamManager teamManager;
   private GameManager gameManager;
   private KitSelectorListener kitSelectorListener;
   private TeamSelectorListener teamSelectorListener;
   private LoadoutSelectorListener loadoutSelectorListener;

   public void onEnable() {
      this.saveDefaultConfig();
      this.lobbyManager = new LobbyManager(this);
      ArenaManager arenaManager = new ArenaManager(this);
      LobbyItems lobbyItems = new LobbyItems(this);
      RoundItems roundItems = new RoundItems(this);
      this.teamManager = new TeamManager();
      ChatMuteManager chatMuteManager = new ChatMuteManager();
      CommandProtectionManager commandProtectionManager = new CommandProtectionManager();
      KitManager kitManager = new KitManager();
      LoadoutManager loadoutManager = new LoadoutManager(this, roundItems);
      WinTracker winTracker = new WinTracker(this);
      this.gameManager = new GameManager(this, this.lobbyManager, arenaManager, lobbyItems, roundItems, kitManager, this.teamManager, loadoutManager, winTracker);
      SuccubusManager succubusManager = new SuccubusManager(this.gameManager, this.teamManager, kitManager, roundItems);
      ScoreboardManager scoreboardManager = new ScoreboardManager(this, this.gameManager, this.teamManager, succubusManager, winTracker);
      DiamondListener diamondListener = new DiamondListener(this, this.gameManager, arenaManager, this.teamManager);
      this.gameManager.setScoreboardManager(scoreboardManager);
      this.gameManager.setDiamondListener(diamondListener);
      this.lobbyManager.loadLobbyWorld();
      arenaManager.loadArenaWorld();
      this.getServer().getPluginManager().registerEvents(new LobbyJoinListener(this.lobbyManager, lobbyItems), this);
      this.getServer().getPluginManager().registerEvents(new LobbyRespawnListener(this.lobbyManager, arenaManager, this.gameManager), this);
      this.getServer().getPluginManager().registerEvents(new LobbyProtectionListener(this.lobbyManager), this);
      this.getServer().getPluginManager().registerEvents(new LobbyItemProtectionListener(lobbyItems), this);
      this.getServer().getPluginManager().registerEvents(new LaunchPadListener(this), this);
      this.teamSelectorListener = new TeamSelectorListener(lobbyItems, this.gameManager, this.teamManager);
      this.kitSelectorListener = new KitSelectorListener(lobbyItems, kitManager);
      this.loadoutSelectorListener = new LoadoutSelectorListener(lobbyItems, this.gameManager, loadoutManager);
      this.getServer().getPluginManager().registerEvents(this.teamSelectorListener, this);
      this.getServer().getPluginManager().registerEvents(new TeamQuitListener(this.teamManager), this);
      this.getServer().getPluginManager().registerEvents(this.kitSelectorListener, this);
      this.getServer().getPluginManager().registerEvents(this.loadoutSelectorListener, this);
      this.getServer().getPluginManager().registerEvents(new GodAppleConsumeListener(this), this);
      this.getServer().getPluginManager().registerEvents(new IronArmorDurabilityListener(this), this);
      this.getServer().getPluginManager().registerEvents(new TeamDamageListener(this.gameManager, this.teamManager), this);
      this.getServer().getPluginManager().registerEvents(new DiamondBlockProtectionListener(), this);
      this.getServer().getPluginManager().registerEvents(new TeamChatListener(this.teamManager, chatMuteManager), this);
      this.getServer().getPluginManager().registerEvents(new AcrobatListener(this, this.gameManager, kitManager, this.teamManager), this);
      this.getServer().getPluginManager().registerEvents(new BlinkListener(this, this.gameManager, roundItems, kitManager), this);
      this.getServer().getPluginManager().registerEvents(new SuccubusListener(this, succubusManager), this);
      this.getServer().getPluginManager().registerEvents(new DasherItemListener(roundItems), this);
      this.getServer().getPluginManager().registerEvents(new ArenaMobSpawnListener(arenaManager, this.gameManager), this);
      this.getServer().getPluginManager().registerEvents(diamondListener, this);
      this.getServer().getPluginManager().registerEvents(new GameWinListener(this, this.gameManager), this);
      this.getServer().getPluginManager().registerEvents(new GameDeathListener(this, this.gameManager, kitManager, loadoutManager, this.teamManager), this);
      scoreboardManager.start();
      ((PluginCommand)Objects.requireNonNull(this.getCommand("startgame"))).setExecutor(new StartGameCommand(this.gameManager, arenaManager, commandProtectionManager));
      ((PluginCommand)Objects.requireNonNull(this.getCommand("startgame"))).setTabCompleter(new StartGameTabCompleter(arenaManager));
      ((PluginCommand)Objects.requireNonNull(this.getCommand("endgame"))).setExecutor(new EndGameCommand(this.gameManager));
      ((PluginCommand)Objects.requireNonNull(this.getCommand("mute"))).setExecutor(new MuteCommand(chatMuteManager));
      ((PluginCommand)Objects.requireNonNull(this.getCommand("reset"))).setExecutor(new ResetCommand(winTracker, commandProtectionManager));
      ((PluginCommand)Objects.requireNonNull(this.getCommand("ban"))).setExecutor(new BanLoadoutCommand(loadoutManager, gameManager, commandProtectionManager));
      ((PluginCommand)Objects.requireNonNull(this.getCommand("ban"))).setTabCompleter(new LoadoutTabCompleter());
      ((PluginCommand)Objects.requireNonNull(this.getCommand("unban"))).setExecutor(new UnbanLoadoutCommand(loadoutManager, commandProtectionManager));
      ((PluginCommand)Objects.requireNonNull(this.getCommand("unban"))).setTabCompleter(new LoadoutTabCompleter());
      ((PluginCommand)Objects.requireNonNull(this.getCommand("protect"))).setExecutor(new ProtectCommand(commandProtectionManager));
      this.getLogger().info("AnniSim plugin enabled!");
   }

   public void onDisable() {
      this.saveConfig();
      this.getLogger().info("AnniSim plugin disabled!");
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

   public void preparePlayerForHubLobby(org.bukkit.entity.Player player) {
      if (this.gameManager != null) {
         this.gameManager.preparePlayerForLobby(player);
      }
   }

   public void openTeamSelectionMenu(org.bukkit.entity.Player player) {
      if (this.teamSelectorListener != null) {
         this.teamSelectorListener.openMenu(player);
      }
   }

   public void openKitSelectionMenu(org.bukkit.entity.Player player) {
      if (this.kitSelectorListener != null) {
         this.kitSelectorListener.openMenu(player);
      }
   }

   public void openLoadoutSelectionMenu(org.bukkit.entity.Player player) {
      if (this.loadoutSelectorListener != null) {
         this.loadoutSelectorListener.openMenu(player);
      }
   }
}
