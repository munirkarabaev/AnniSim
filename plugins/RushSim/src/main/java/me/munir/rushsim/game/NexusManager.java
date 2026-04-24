package me.munir.rushsim.game;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NexusManager {
   private static final int STARTING_HEALTH = 50;
   private int health = STARTING_HEALTH;
   private UUID currentMiner;
   private Location currentBlock;

   public void reset() {
      this.health = STARTING_HEALTH;
      this.currentMiner = null;
      this.currentBlock = null;
   }

   public int getHealth() {
      return this.health;
   }

   public boolean tryClaim(Player player, Location location) {
      if (this.currentMiner == null || this.currentBlock == null) {
         this.currentMiner = player.getUniqueId();
         this.currentBlock = location;
         return true;
      }

      return this.currentMiner.equals(player.getUniqueId()) && this.currentBlock.equals(location);
   }

   public boolean isCurrentMiner(Player player, Location location) {
      return this.currentMiner != null && this.currentMiner.equals(player.getUniqueId()) && this.currentBlock != null && this.currentBlock.equals(location);
   }

   public void release(Player player, Location location) {
      if (this.isCurrentMiner(player, location)) {
         this.currentMiner = null;
         this.currentBlock = null;
      }
   }

   public boolean damage(Player player, Location location) {
      if (!this.isCurrentMiner(player, location)) {
         return false;
      }

      if (this.health > 0) {
         --this.health;
      }

      this.currentMiner = null;
      this.currentBlock = null;
      return true;
   }
}
