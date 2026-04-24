package me.munir.rushsim.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyManager {
   private static final String LOBBY_WORLD_NAME = "lobby";
   private final JavaPlugin plugin;
   private World lobbyWorld;

   public LobbyManager(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public void loadLobbyWorld() {
      this.lobbyWorld = Bukkit.getWorld(LOBBY_WORLD_NAME);
      if (this.lobbyWorld == null) {
         this.lobbyWorld = Bukkit.createWorld(WorldCreator.name(LOBBY_WORLD_NAME));
      }

      if (this.lobbyWorld == null) {
         this.plugin.getLogger().warning("Could not load the lobby world.");
      } else {
         this.lobbyWorld.setDifficulty(Difficulty.PEACEFUL);
         this.lobbyWorld.setSpawnLocation(-74, 8, -544);
      }
   }

   public void teleportToLobbySpawn(Player player) {
      Location lobbySpawn = this.getLobbySpawnLocation();
      if (lobbySpawn != null) {
         player.teleport(lobbySpawn);
         player.setGameMode(GameMode.ADVENTURE);
      }
   }

   public Location getLobbySpawnLocation() {
      if (this.lobbyWorld == null) {
         this.loadLobbyWorld();
      }

      return this.lobbyWorld == null ? null : new Location(this.lobbyWorld, -73.5D, 8.0D, -543.5D);
   }

   public boolean isLobbyWorld(World world) {
      if (world == null) {
         return false;
      }

      if (this.lobbyWorld == null) {
         this.loadLobbyWorld();
      }

      return this.lobbyWorld != null && this.lobbyWorld.getUID().equals(world.getUID());
   }
}
