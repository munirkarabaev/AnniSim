package me.munir.rushsim.arena;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.munir.rushsim.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ArenaManager {
   private final JavaPlugin plugin;
   private final Map<String, RushMap> maps = new HashMap<>();
   private RushMap selectedMap;
   private World arenaWorld;

   public ArenaManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.reload();
   }

   public void reload() {
      this.maps.clear();
      ConfigurationSection mapsSection = this.plugin.getConfig().getConfigurationSection("maps");
      if (mapsSection == null) {
         this.selectedMap = null;
         return;
      }

      for (String key : mapsSection.getKeys(false)) {
         ConfigurationSection mapSection = mapsSection.getConfigurationSection(key);
         if (mapSection == null) {
            continue;
         }

         RushMap map = RushMap.fromConfig(key, mapSection);
         if (map != null) {
            this.maps.put(key.toLowerCase(), map);
         }
      }

      if (this.selectedMap != null) {
         this.selectedMap = this.maps.get(this.selectedMap.name().toLowerCase());
      }
   }

   public java.util.List<String> getAvailableMapNames() {
      return this.maps.values().stream().map(RushMap::name).sorted().toList();
   }

   public boolean selectMap(String mapName) {
      RushMap map = mapName == null ? null : this.maps.get(mapName.toLowerCase());
      if (map == null) {
         return false;
      }

      if (this.selectedMap != null && !this.selectedMap.worldName().equals(map.worldName()) && !this.unloadArenaWorld()) {
         return false;
      }

      this.selectedMap = map;
      return this.loadArenaWorld();
   }

   public boolean hasSelectedMap() {
      return this.selectedMap != null;
   }

   public String getSelectedMapName() {
      return this.selectedMap == null ? null : this.selectedMap.name();
   }

   public boolean resetArenaWorld() {
      if (this.selectedMap == null) {
         return false;
      }

      try {
         if (!this.unloadArenaWorld()) {
            return false;
         }

         Path arenaPath = this.getArenaWorldFolder(this.selectedMap.worldName()).toPath();
         Path templatePath = this.getArenaWorldFolder(this.selectedMap.templateWorldName()).toPath();
         if (!Files.exists(templatePath)) {
            this.plugin.getLogger().warning("Missing template world " + this.selectedMap.templateWorldName() + ".");
            return false;
         }

         this.deleteDirectory(arenaPath);
         this.copyDirectory(templatePath, arenaPath);
         return this.loadArenaWorld();
      } catch (IOException exception) {
         this.plugin.getLogger().warning("Failed to reset arena world: " + exception.getMessage());
         return false;
      }
   }

   public Location getSpawn(Team team) {
      if (this.selectedMap == null) {
         return null;
      }

      if (this.arenaWorld == null && !this.loadArenaWorld()) {
         return null;
      }

      Location baseLocation = team == Team.BLUE ? this.selectedMap.blueSpawn() : this.selectedMap.redSpawn();
      return baseLocation == null ? null : new Location(this.arenaWorld, baseLocation.getX(), baseLocation.getY(), baseLocation.getZ(), baseLocation.getYaw(), baseLocation.getPitch());
   }

   public void teleportToSpawn(Player player, Team team) {
      Location spawn = this.getSpawn(team);
      if (spawn != null) {
         player.teleport(spawn);
      }
   }

   public boolean isArenaWorld(World world) {
      return world != null && this.arenaWorld != null && this.arenaWorld.getUID().equals(world.getUID());
   }

   private boolean loadArenaWorld() {
      if (this.selectedMap == null) {
         return false;
      }

      this.arenaWorld = Bukkit.getWorld(this.selectedMap.worldName());
      if (this.arenaWorld == null) {
         this.arenaWorld = Bukkit.createWorld(WorldCreator.name(this.selectedMap.worldName()));
      }

      if (this.arenaWorld == null) {
         return false;
      }

      this.arenaWorld.setDifficulty(Difficulty.NORMAL);
      return true;
   }

   private boolean unloadArenaWorld() {
      if (this.selectedMap == null) {
         this.arenaWorld = null;
         return true;
      }

      World loadedWorld = Bukkit.getWorld(this.selectedMap.worldName());
      if (loadedWorld == null) {
         this.arenaWorld = null;
         return true;
      }

      boolean unloaded = Bukkit.unloadWorld(loadedWorld, false);
      if (unloaded) {
         this.arenaWorld = null;
      }

      return unloaded;
   }

   private File getArenaWorldFolder(String worldName) {
      return new File(Bukkit.getWorldContainer(), worldName);
   }

   private void copyDirectory(Path sourcePath, Path targetPath) throws IOException {
      try (Stream<Path> pathStream = Files.walk(sourcePath)) {
         pathStream.forEach((path) -> {
            try {
               Path relativePath = sourcePath.relativize(path);
               Path target = targetPath.resolve(relativePath);
               if (Files.isDirectory(path)) {
                  Files.createDirectories(target);
               } else if (!"session.lock".equals(path.getFileName().toString())) {
                  Files.createDirectories(target.getParent());
                  Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
               }
            } catch (IOException exception) {
               throw new RuntimeException(exception);
            }
         });
      } catch (RuntimeException exception) {
         if (exception.getCause() instanceof IOException ioException) {
            throw ioException;
         }

         throw exception;
      }
   }

   private void deleteDirectory(Path directoryPath) throws IOException {
      if (!Files.exists(directoryPath, new LinkOption[0])) {
         return;
      }

      try (Stream<Path> pathStream = Files.walk(directoryPath)) {
         pathStream.sorted(Comparator.reverseOrder()).forEach((path) -> {
            try {
               Files.deleteIfExists(path);
            } catch (IOException exception) {
               throw new RuntimeException(exception);
            }
         });
      } catch (RuntimeException exception) {
         if (exception.getCause() instanceof IOException ioException) {
            throw ioException;
         }

         throw exception;
      }
   }

   public record RushMap(String name, String worldName, String templateWorldName, Location redSpawn, Location blueSpawn) {
      static RushMap fromConfig(String name, ConfigurationSection section) {
         String worldName = section.getString("world");
         String templateName = section.getString("template");
         ConfigurationSection redSection = section.getConfigurationSection("red-spawn");
         ConfigurationSection blueSection = section.getConfigurationSection("blue-spawn");
         if (worldName == null || templateName == null || redSection == null || blueSection == null) {
            return null;
         }

         Location redSpawn = new Location(null, redSection.getDouble("x"), redSection.getDouble("y"), redSection.getDouble("z"), (float)redSection.getDouble("yaw"), (float)redSection.getDouble("pitch"));
         Location blueSpawn = new Location(null, blueSection.getDouble("x"), blueSection.getDouble("y"), blueSection.getDouble("z"), (float)blueSection.getDouble("yaw"), (float)blueSection.getDouble("pitch"));
         return new RushMap(name, worldName, templateName, redSpawn, blueSpawn);
      }
   }
}
