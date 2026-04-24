package me.munir.annisim.listener;

import me.munir.annisim.arena.ArenaManager;
import me.munir.annisim.game.GameManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class ArenaMobSpawnListener implements Listener {
   private final ArenaManager arenaManager;
   private final GameManager gameManager;

   public ArenaMobSpawnListener(ArenaManager arenaManager, GameManager gameManager) {
      this.arenaManager = arenaManager;
      this.gameManager = gameManager;
   }

   @EventHandler
   public void onCreatureSpawn(CreatureSpawnEvent event) {
      if (this.arenaManager.isArenaWorld(event.getLocation().getWorld())) {
         if (this.shouldBlockMobs()) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler
   public void onChunkLoad(ChunkLoadEvent event) {
      if (this.arenaManager.isArenaWorld(event.getWorld())) {
         if (this.shouldBlockMobs()) {
            this.removeLivingMobs(event.getChunk());
         }
      }
   }

   private boolean shouldBlockMobs() {
      return this.arenaManager.getSelectedArenaMap().shouldBlockMobSpawns() || this.gameManager.isInGame();
   }

   private void removeLivingMobs(Chunk chunk) {
      Entity[] var2 = chunk.getEntities();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Entity entity = var2[var4];
         if (!(entity instanceof Player) && entity instanceof LivingEntity) {
            entity.remove();
         }
      }

   }
}
