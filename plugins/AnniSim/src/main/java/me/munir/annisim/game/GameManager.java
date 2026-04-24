package me.munir.annisim.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import me.munir.annisim.arena.ArenaManager;
import me.munir.annisim.arena.ArenaMap;
import me.munir.annisim.command.StartGameRequest;
import me.munir.annisim.kit.Kit;
import me.munir.annisim.kit.KitManager;
import me.munir.annisim.listener.DiamondListener;
import me.munir.annisim.loadout.LoadoutType;
import me.munir.annisim.loadout.LoadoutManager;
import me.munir.annisim.lobby.LobbyItems;
import me.munir.annisim.lobby.LobbyManager;
import me.munir.annisim.scoreboard.ScoreboardManager;
import me.munir.annisim.team.Team;
import me.munir.annisim.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GameManager {
   private final JavaPlugin plugin;
   private final LobbyManager lobbyManager;
   private final ArenaManager arenaManager;
   private final LobbyItems lobbyItems;
   private final RoundItems roundItems;
   private final KitManager kitManager;
   private final TeamManager teamManager;
   private final LoadoutManager loadoutManager;
   private final WinTracker winTracker;
   private GameState gameState;
   private long gameStartTime;
   private int redDiamonds;
   private int blueDiamonds;
   private int lastRedCount;
   private int lastBlueCount;
   private boolean gameEnding;
   private boolean gameStarting;
   private ScoreboardManager scoreboardManager;
   private DiamondListener diamondListener;
   private final Map<UUID, Location> spectatorRespawnLocations;

   public GameManager(JavaPlugin plugin, LobbyManager lobbyManager, ArenaManager arenaManager, LobbyItems lobbyItems, RoundItems roundItems, KitManager kitManager, TeamManager teamManager, LoadoutManager loadoutManager, WinTracker winTracker) {
      this.gameState = GameState.LOBBY;
      this.gameEnding = false;
      this.spectatorRespawnLocations = new HashMap();
      this.plugin = plugin;
      this.lobbyManager = lobbyManager;
      this.arenaManager = arenaManager;
      this.lobbyItems = lobbyItems;
      this.roundItems = roundItems;
      this.kitManager = kitManager;
      this.teamManager = teamManager;
      this.loadoutManager = loadoutManager;
      this.winTracker = winTracker;
   }

   public void setScoreboardManager(ScoreboardManager scoreboardManager) {
      this.scoreboardManager = scoreboardManager;
   }

   public void setDiamondListener(DiamondListener diamondListener) {
      this.diamondListener = diamondListener;
   }

   public boolean startGame(ArenaMap arenaMap) {
      return this.startGame(new StartGameRequest(me.munir.annisim.command.StartGameMode.STANDARD, arenaMap));
   }

   public boolean startGame(StartGameRequest startGameRequest) {
      if (this.gameState != GameState.LOBBY || this.gameStarting) {
         return false;
      }

      ArenaMap arenaMap = this.resolveArenaMap(startGameRequest);
      if (arenaMap == null) {
         return false;
      } else if (!this.arenaManager.selectArenaMap(arenaMap)) {
         return false;
      } else if (!this.arenaManager.resetArenaWorld()) {
         return false;
      } else if (this.arenaManager.getArenaSpawnLocation(Team.RED) != null && this.arenaManager.getArenaSpawnLocation(Team.BLUE) != null) {
         this.gameStarting = true;
         this.runCountdown(5, startGameRequest.getCountdownLabel(), () -> {
            this.gameStarting = false;
            this.startPreparedGame(startGameRequest);
         });
         return true;
      } else {
         return false;
      }
   }

   private ArenaMap resolveArenaMap(StartGameRequest startGameRequest) {
      if (!startGameRequest.mode().usesRandomMap()) {
         return startGameRequest.arenaMap();
      }

      ArenaMap[] maps = ArenaMap.values();
      return maps.length == 0 ? null : maps[ThreadLocalRandom.current().nextInt(maps.length)];
   }

   private boolean startPreparedGame(StartGameRequest startGameRequest) {
      this.clearRoundOverrides();
      if (startGameRequest.mode().usesRandomKitLoadout()) {
         this.assignRandomRoundSelections();
         this.showRandomSelectionTitles();
      }

      return this.startPreparedGame();
   }

   private boolean startPreparedGame() {
      if (this.gameState != GameState.LOBBY) {
         return false;
      } else if (this.arenaManager.getArenaSpawnLocation(Team.RED) != null && this.arenaManager.getArenaSpawnLocation(Team.BLUE) != null) {
         this.gameState = GameState.IN_GAME;
         this.gameEnding = false;
         this.gameStartTime = System.currentTimeMillis();
         this.resetDiamonds();
         this.teamManager.resetDeadPlayers();
         this.teamManager.beginRound();
         this.spectatorRespawnLocations.clear();
         this.updateLastTeamCounts();
         if (this.diamondListener != null) {
            this.diamondListener.clearTrackedDiamonds();
         }

         this.arenaManager.clearLivingMobs();
         if (this.scoreboardManager != null) {
            this.scoreboardManager.start();
         }

         Iterator var2 = Bukkit.getOnlinePlayers().iterator();

         while(var2.hasNext()) {
            Player player = (Player)var2.next();
            if (this.teamManager.getTeam(player) != null) {
               this.preparePlayerForRound(player);
            } else {
               this.preparePlayerForLobby(player);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean endGame() {
      if (this.gameState != GameState.IN_GAME) {
         return false;
      } else {
         this.gameState = GameState.LOBBY;
         this.gameEnding = false;
         this.gameStarting = false;
         this.resetDiamonds();
         this.gameStartTime = 0L;
         this.lastRedCount = 0;
         this.lastBlueCount = 0;
         this.spectatorRespawnLocations.clear();
         this.clearRoundOverrides();
         this.teamManager.clearRoundParticipants();
         if (this.diamondListener != null) {
            this.diamondListener.clearTrackedDiamonds();
         }

         this.teamManager.resetDeadPlayers();
         Iterator var1 = Bukkit.getOnlinePlayers().iterator();

         while(var1.hasNext()) {
            Player player = (Player)var1.next();
            this.preparePlayerForLobby(player);
         }

         this.arenaManager.resetArenaWorld();
         return true;
      }
   }

   public GameState getGameState() {
      return this.gameState;
   }

   public boolean isInGame() {
      return this.gameState == GameState.IN_GAME;
   }

   public boolean isStarting() {
      return this.gameStarting;
   }

   public TeamManager getTeamManager() {
      return this.teamManager;
   }

   public long getGameDurationSeconds() {
      return this.gameStartTime == 0L ? 0L : (System.currentTimeMillis() - this.gameStartTime) / 1000L;
   }

   public void addDiamond(Team team) {
      if (team == Team.RED) {
         ++this.redDiamonds;
      } else if (team == Team.BLUE) {
         ++this.blueDiamonds;
      }

      this.checkWinConditions();
   }

   public int getDiamonds(Team team) {
      if (team == Team.RED) {
         return this.redDiamonds;
      } else {
         return team == Team.BLUE ? this.blueDiamonds : 0;
      }
   }

   public void resetDiamonds() {
      this.redDiamonds = 0;
      this.blueDiamonds = 0;
   }

   public void checkWinConditions() {
      if (this.isInGame() && !this.gameEnding) {
         int currentRedCount = this.teamManager.getPlayerCount(Team.RED);
         int currentBlueCount = this.teamManager.getPlayerCount(Team.BLUE);
         int requiredDiamonds = this.getRequiredDiamondsToWin();
         if (this.redDiamonds >= requiredDiamonds) {
            this.handleWin(Team.RED);
         } else if (this.blueDiamonds >= requiredDiamonds) {
            this.handleWin(Team.BLUE);
         } else if (this.lastRedCount > 0 && currentRedCount == 0) {
            this.handleWin(Team.BLUE);
         } else if (this.lastBlueCount > 0 && currentBlueCount == 0) {
            this.handleWin(Team.RED);
         } else {
            this.lastRedCount = currentRedCount;
            this.lastBlueCount = currentBlueCount;
         }
      }
   }

   private int getRequiredDiamondsToWin() {
      return this.arenaManager.getSelectedArenaMap() == ArenaMap.ANDORRA ? 15 : 25;
   }

   public void markPlayerDead(Player player, Location deathLocation) {
      this.teamManager.markDead(player);
      this.spectatorRespawnLocations.put(player.getUniqueId(), deathLocation.clone());
      this.checkWinConditions();
   }

   public Location getSpectatorRespawnLocation(Player player) {
      return (Location)this.spectatorRespawnLocations.get(player.getUniqueId());
   }

   public void preparePlayerForRound(Player player) {
      this.clearPlayerState(player);
      Team team = this.teamManager.getTeam(player);
      if (team == Team.SPECTATOR) {
         this.teleportPlayerToSpectatorSpawn(player);
         player.setGameMode(GameMode.SPECTATOR);
      } else {
         this.arenaManager.teleportToArenaSpawn(player, team);
         player.setGameMode(GameMode.SURVIVAL);
         this.giveRoundLoadout(player);
      }
   }

   public void preparePlayerForLobby(Player player) {
      this.clearPlayerState(player);
      this.lobbyManager.teleportToLobbySpawn(player);
      this.giveLobbyItems(player);
   }

   public void handleBannedLoadoutReset(Player player) {
      if (this.isInGame() && this.teamManager.isActivePlayer(player)) {
         this.clearPlayerState(player);
         this.giveRoundLoadout(player);
      } else {
         this.preparePlayerForLobby(player);
      }
   }

   private void clearPlayerState(Player player) {
      player.getInventory().clear();
      player.setAllowFlight(false);
      player.setFlying(false);
      Iterator var2 = player.getActivePotionEffects().iterator();

      while(var2.hasNext()) {
         PotionEffect potionEffect = (PotionEffect)var2.next();
         player.removePotionEffect(potionEffect.getType());
      }

   }

   private void giveLobbyItems(Player player) {
      PlayerInventory inventory = player.getInventory();
      inventory.setItem(0, this.lobbyItems.createTeamSelector());
      inventory.setItem(4, this.lobbyItems.createKitSelector());
      inventory.setItem(8, this.lobbyItems.createLoadoutSelector());
   }

   private void giveRoundLoadout(Player player) {
      PlayerInventory inventory = player.getInventory();
      this.loadoutManager.applyLoadout(player, inventory, this.createBaseRoundItems(player));
   }

   public List<ItemStack> createBaseRoundItems(Player player) {
      List<ItemStack> items = new ArrayList();
      items.add(this.createSword());
      this.addKitItem(player, items);
      items.add(this.createBuildingBlocks());
      items.add(this.createBuildingBlocks());
      items.add(this.createTool(Material.IRON_PICKAXE, 4));
      items.add(this.createTool(Material.IRON_AXE, 1));
      items.add(this.createTool(Material.IRON_SHOVEL, 1));
      items.add(new ItemStack(Material.WATER_BUCKET));
      items.add(new ItemStack(Material.SHEARS));
      items.add(new ItemStack(Material.COBWEB, 10));
      items.add(new ItemStack(Material.COOKED_BEEF, 10));
      items.add(this.createPotion(PotionEffectType.REGENERATION, 90, 0));
      items.add(this.createPotion(PotionEffectType.REGENERATION, 90, 0));
      items.add(this.createPotion(PotionEffectType.REGENERATION, 90, 0));
      items.add(this.createPotion(PotionEffectType.SPEED, 90, 1));
      items.add(this.createPotion(PotionEffectType.SPEED, 90, 1));
      items.add(this.createPotion(PotionEffectType.SPEED, 90, 1));
      return items;
   }

   private void addKitItem(Player player, List<ItemStack> items) {
      if (this.kitManager.getKit(player) == Kit.DASHER) {
         items.add(this.roundItems.createBlinkItem());
      } else {
         if (this.kitManager.getKit(player) == Kit.SUCCUBUS) {
            items.add(this.roundItems.createSuccubusItem());
         }

      }
   }

   private ItemStack createSword() {
      ItemStack itemStack = new ItemStack(Material.IRON_SWORD);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.addEnchant(Enchantment.SHARPNESS, 4, true);
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }

   private ItemStack createTool(Material material, int efficiencyLevel) {
      ItemStack itemStack = new ItemStack(material);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.addEnchant(Enchantment.EFFICIENCY, efficiencyLevel, true);
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }

   private ItemStack createBuildingBlocks() {
      return new ItemStack(Material.OAK_PLANKS, 64);
   }

   private ItemStack createPotion(PotionEffectType potionEffectType, int durationSeconds, int amplifier) {
      ItemStack itemStack = new ItemStack(Material.POTION);
      PotionMeta potionMeta = (PotionMeta)itemStack.getItemMeta();
      if (potionMeta != null) {
         potionMeta.addCustomEffect(new PotionEffect(potionEffectType, durationSeconds * 20, amplifier), true);
         itemStack.setItemMeta(potionMeta);
      }

      return itemStack;
   }

   private void updateLastTeamCounts() {
      this.lastRedCount = this.teamManager.getPlayerCount(Team.RED);
      this.lastBlueCount = this.teamManager.getPlayerCount(Team.BLUE);
   }

   public void teleportPlayerToSpectatorSpawn(Player player) {
      Location spectatorSpawn = this.arenaManager.getArenaSpawnLocation(Team.RED);
      if (spectatorSpawn != null) {
         player.teleport(spectatorSpawn);
      }
   }

   private void assignRandomRoundSelections() {
      List<LoadoutType> selectableLoadouts = this.loadoutManager.getSelectableLoadouts();
      if (selectableLoadouts.isEmpty()) {
         selectableLoadouts = List.of(LoadoutType.DEFAULT);
      }

      Iterator var2 = Bukkit.getOnlinePlayers().iterator();

      while(var2.hasNext()) {
         Player player = (Player)var2.next();
         Team team = this.teamManager.getTeam(player);
         if (team != null && team.isPlayingTeam()) {
            this.kitManager.setRoundKit(player, this.getRandomKit());
            this.loadoutManager.setRoundLoadout(player, (LoadoutType)selectableLoadouts.get(ThreadLocalRandom.current().nextInt(selectableLoadouts.size())));
         }
      }
   }

   private Kit getRandomKit() {
      Kit[] kits = Kit.values();
      return kits[ThreadLocalRandom.current().nextInt(kits.length)];
   }

   private void showRandomSelectionTitles() {
      Iterator var1 = Bukkit.getOnlinePlayers().iterator();

      while(var1.hasNext()) {
         Player player = (Player)var1.next();
         Team team = this.teamManager.getTeam(player);
         if (team != null && team.isPlayingTeam()) {
            player.sendTitle("Your kit: " + this.getKitDisplayName(this.kitManager.getKit(player)), "Your loadout: " + this.loadoutManager.getSelectedLoadout(player).getDisplayName(), 0, 60, 0);
         }
      }
   }

   private String getKitDisplayName(Kit kit) {
      if (kit == null) {
         return "None";
      } else {
         switch(kit) {
         case DASHER:
            return "Dasher";
         case SUCCUBUS:
            return "Succubus";
         case ACROBAT:
            return "Acrobat";
         default:
            throw new MatchException((String)null, (Throwable)null);
         }
      }
   }

   private void clearRoundOverrides() {
      this.kitManager.clearRoundKits();
      this.loadoutManager.clearRoundLoadouts();
   }

   private void handleWin(Team winningTeam) {
      this.gameEnding = true;
      this.winTracker.recordWin(winningTeam);
      String title = winningTeam == Team.RED ? "§cRED WINS" : "§9BLUE WINS";
      Iterator var3 = Bukkit.getOnlinePlayers().iterator();

      while(var3.hasNext()) {
         Player player = (Player)var3.next();
         player.sendTitle(title, "", 10, 100, 10);
         player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
      }

      Bukkit.getScheduler().runTaskLater(this.plugin, this::endGame, 100L);
   }

   private void runCountdown(int seconds, String countdownLabel, Runnable onComplete) {
      for(int index = 0; index < seconds; ++index) {
         int countdownValue = seconds - index;
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            Iterator var2 = Bukkit.getOnlinePlayers().iterator();

            while(var2.hasNext()) {
               Player player = (Player)var2.next();
               player.sendTitle("§0" + countdownValue, "Starting " + countdownLabel, 0, 20, 0);
               player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0F, 1.0F + (float)(seconds - countdownValue) * 0.1F);
            }

         }, (long)(index * 20));
      }

      Bukkit.getScheduler().runTaskLater(this.plugin, onComplete, (long)(seconds * 20));
   }
}
