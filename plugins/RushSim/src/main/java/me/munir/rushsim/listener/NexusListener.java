package me.munir.rushsim.listener;

import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.game.NexusManager;
import me.munir.rushsim.team.Team;
import me.munir.rushsim.team.TeamManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;

public class NexusListener implements Listener {
   private final GameManager gameManager;
   private final TeamManager teamManager;

   public NexusListener(GameManager gameManager, TeamManager teamManager) {
      this.gameManager = gameManager;
      this.teamManager = teamManager;
   }

   @EventHandler
   public void onBlockDamage(BlockDamageEvent event) {
      if (!this.isNexus(event.getBlock()) || !this.gameManager.isInGame()) {
         return;
      }

      Player player = event.getPlayer();
      if (this.teamManager.getTeam(player) != Team.RED) {
         event.setCancelled(true);
         return;
      }

      if (!event.getBlock().isPreferredTool(player.getInventory().getItemInMainHand())) {
         event.setCancelled(true);
         return;
      }

      Location location = event.getBlock().getLocation();
      NexusManager nexusManager = this.gameManager.getNexusManager();
      if (!nexusManager.tryClaim(player, location)) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onBlockDamageAbort(BlockDamageAbortEvent event) {
      if (this.isNexus(event.getBlock())) {
         this.gameManager.getNexusManager().release(event.getPlayer(), event.getBlock().getLocation());
      }
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      Block block = event.getBlock();
      if (!this.isNexus(block) || !this.gameManager.isInGame()) {
         return;
      }

      event.setDropItems(false);
      event.setCancelled(true);
      Player player = event.getPlayer();
      if (this.teamManager.getTeam(player) != Team.RED) {
         return;
      }

      if (this.gameManager.getNexusManager().damage(player, block.getLocation())) {
         block.setType(Material.END_STONE);
         this.gameManager.handleNexusBreak();
      }
   }

   private boolean isNexus(Block block) {
      return block.getType() == Material.END_STONE && this.gameManager.getArenaManager().isArenaWorld(block.getWorld());
   }
}
