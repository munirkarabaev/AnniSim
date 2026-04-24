package me.munir.annisim.arena;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.munir.annisim.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ArenaManager {
   private final JavaPlugin plugin;
   private ArenaMap selectedArenaMap;
   private World arenaWorld;

   public ArenaManager(JavaPlugin plugin) {
      this.selectedArenaMap = ArenaMap.COASTALV3;
      this.plugin = plugin;
   }

   public void loadArenaWorld() {
      this.ensureTemplateWorldExists(this.selectedArenaMap);
      this.arenaWorld = Bukkit.getWorld(this.selectedArenaMap.getWorldName());
      if (this.arenaWorld == null) {
         this.arenaWorld = Bukkit.createWorld(WorldCreator.name(this.selectedArenaMap.getWorldName()));
      }

      if (this.arenaWorld == null) {
         this.plugin.getLogger().warning("Could not load the " + this.selectedArenaMap.getWorldName() + " world.");
      } else {
         this.arenaWorld.setDifficulty(Difficulty.NORMAL);
      }
   }

   public boolean selectArenaMap(ArenaMap arenaMap) {
      if (arenaMap == null) {
         return false;
      } else if (this.selectedArenaMap == arenaMap) {
         this.loadArenaWorld();
         return this.arenaWorld != null;
      } else {
         boolean sameWorld = this.selectedArenaMap.getWorldName().equals(arenaMap.getWorldName());
         if (!sameWorld && !this.unloadArenaWorld()) {
            return false;
         } else {
            this.selectedArenaMap = arenaMap;
            this.loadArenaWorld();
            return this.arenaWorld != null;
         }
      }
   }

   public void teleportToArenaSpawn(Player player, Team team) {
      Location arenaSpawn = this.getArenaSpawnLocation(team);
      if (arenaSpawn != null) {
         player.teleport(arenaSpawn);
      }

   }

   public Location getArenaSpawnLocation() {
      return this.getArenaSpawnLocation((Team)null);
   }

   public Location getArenaSpawnLocation(Team team) {
      if (this.arenaWorld == null) {
         this.loadArenaWorld();
      }

      return this.arenaWorld == null ? null : new Location(this.arenaWorld, (double)this.selectedArenaMap.getSpawnX(team) + 0.5D, (double)this.selectedArenaMap.getSpawnY(team), (double)this.selectedArenaMap.getSpawnZ(team) + 0.5D);
   }

   public boolean isArenaWorld(World world) {
      if (world == null) {
         return false;
      } else {
         if (this.arenaWorld == null) {
            this.loadArenaWorld();
         }

         return this.arenaWorld != null && this.arenaWorld.getUID().equals(world.getUID());
      }
   }

   public void clearLivingMobs() {
      if (this.arenaWorld == null) {
         this.loadArenaWorld();
      }

      if (this.arenaWorld != null) {
         Iterator var1 = this.arenaWorld.getEntities().iterator();

         while(var1.hasNext()) {
            Entity entity = (Entity)var1.next();
            if (!(entity instanceof Player) && entity instanceof LivingEntity) {
               entity.remove();
            }
         }

      }
   }

   public boolean resetArenaWorld() {
      try {
         this.ensureTemplateWorldExists(this.selectedArenaMap);
         if (!this.unloadArenaWorld()) {
            this.plugin.getLogger().warning("Could not unload the " + this.selectedArenaMap.getWorldName() + " world for reset.");
            return false;
         } else {
            Path arenaPath = this.getArenaWorldFolder(this.selectedArenaMap).toPath();
            Path templatePath = this.getArenaTemplateWorldFolder(this.selectedArenaMap).toPath();
            if (!Files.exists(templatePath, new LinkOption[0])) {
               this.plugin.getLogger().warning("Could not find the " + this.selectedArenaMap.getTemplateWorldName() + " template world.");
               return false;
            } else {
               this.deleteDirectory(arenaPath);
               this.copyDirectory(templatePath, arenaPath);
               this.loadArenaWorld();
               return this.arenaWorld != null;
            }
         }
      } catch (IOException var3) {
         Logger var10000 = this.plugin.getLogger();
         String var10001 = this.selectedArenaMap.getWorldName();
         var10000.warning("Could not reset the " + var10001 + " world: " + var3.getMessage());
         return false;
      }
   }

   public ArenaMap getSelectedArenaMap() {
      return this.selectedArenaMap;
   }

   public String getSelectedArenaWorldName() {
      return this.selectedArenaMap.getWorldName();
   }

   public List<String> getAvailableMapNames() {
      return Arrays.stream(ArenaMap.values()).map(ArenaMap::getCommandName).toList();
   }

   private void ensureTemplateWorldExists(ArenaMap arenaMap) {
      File templateFolder = this.getArenaTemplateWorldFolder(arenaMap);
      if (!templateFolder.exists()) {
         File arenaFolder = this.getArenaWorldFolder(arenaMap);
         if (arenaFolder.exists()) {
            try {
               this.copyDirectory(arenaFolder.toPath(), templateFolder.toPath());
            } catch (IOException var5) {
               Logger var10000 = this.plugin.getLogger();
               String var10001 = arenaMap.getTemplateWorldName();
               var10000.warning("Could not create the " + var10001 + " template world: " + var5.getMessage());
            }

         }
      }
   }

   private boolean unloadArenaWorld() {
      World loadedWorld = Bukkit.getWorld(this.selectedArenaMap.getWorldName());
      if (loadedWorld == null) {
         this.arenaWorld = null;
         return true;
      } else {
         boolean unloaded = Bukkit.unloadWorld(loadedWorld, false);
         if (unloaded) {
            this.arenaWorld = null;
         }

         return unloaded;
      }
   }

   private File getArenaWorldFolder(ArenaMap arenaMap) {
      return new File(Bukkit.getWorldContainer(), arenaMap.getWorldName());
   }

   private File getArenaTemplateWorldFolder(ArenaMap arenaMap) {
      return new File(Bukkit.getWorldContainer(), arenaMap.getTemplateWorldName());
   }

   private void copyDirectory(Path sourcePath, Path targetPath) throws IOException {
      try {
         Stream<Path> pathStream = Files.walk(sourcePath);

         try {
            pathStream.forEach((path) -> {
               try {
                  Path relativePath = sourcePath.relativize(path);
                  Path target = targetPath.resolve(relativePath);
                  if (Files.isDirectory(path, new LinkOption[0])) {
                     Files.createDirectories(target);
                  } else if (!"session.lock".equals(path.getFileName().toString())) {
                     Files.createDirectories(target.getParent());
                     Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                  }
               } catch (IOException var5) {
                  throw new RuntimeException(var5);
               }
            });
         } catch (Throwable var7) {
            if (pathStream != null) {
               try {
                  pathStream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (pathStream != null) {
            pathStream.close();
         }

      } catch (RuntimeException var8) {
         Throwable var5 = var8.getCause();
         if (var5 instanceof IOException) {
            IOException ioException = (IOException)var5;
            throw ioException;
         } else {
            throw var8;
         }
      }
   }

   private void deleteDirectory(Path directoryPath) throws IOException {
      if (Files.exists(directoryPath, new LinkOption[0])) {
         try {
            Stream<Path> pathStream = Files.walk(directoryPath);

            try {
               pathStream.sorted(Comparator.reverseOrder()).forEach((path) -> {
                  try {
                     Files.deleteIfExists(path);
                  } catch (IOException var2) {
                     throw new RuntimeException(var2);
                  }
               });
            } catch (Throwable var6) {
               if (pathStream != null) {
                  try {
                     pathStream.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (pathStream != null) {
               pathStream.close();
            }

         } catch (RuntimeException var7) {
            Throwable var4 = var7.getCause();
            if (var4 instanceof IOException) {
               IOException ioException = (IOException)var4;
               throw ioException;
            } else {
               throw var7;
            }
         }
      }
   }
}
