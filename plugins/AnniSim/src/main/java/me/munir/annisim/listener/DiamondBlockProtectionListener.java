package me.munir.annisim.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class DiamondBlockProtectionListener implements Listener {
   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (this.isProtectedBlock(event.getBlock())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onBlockExplode(BlockExplodeEvent event) {
      event.blockList().removeIf(this::isProtectedBlock);
   }

   @EventHandler
   public void onEntityExplode(EntityExplodeEvent event) {
      event.blockList().removeIf(this::isProtectedBlock);
   }

   private boolean isProtectedBlock(Block block) {
      return block != null && block.getType() == Material.DIAMOND_BLOCK;
   }
}
