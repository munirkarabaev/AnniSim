package me.munir.annisim.listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.munir.annisim.arena.ArenaManager;
import me.munir.annisim.game.GameManager;
import me.munir.annisim.team.Team;
import me.munir.annisim.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class DiamondListener implements Listener {
   private final JavaPlugin plugin;
   private final GameManager gameManager;
   private final ArenaManager arenaManager;
   private final TeamManager teamManager;
   private final Map<Location, Material> trackedDiamondBlocks = new HashMap();
   private int resetVersion;

   public DiamondListener(JavaPlugin plugin, GameManager gameManager, ArenaManager arenaManager, TeamManager teamManager) {
      this.plugin = plugin;
      this.gameManager = gameManager;
      this.arenaManager = arenaManager;
      this.teamManager = teamManager;
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      Block block = event.getBlock();
      if (this.arenaManager.getSelectedArenaWorldName().equals(block.getWorld().getName())) {
         if (block.getType() == Material.DIAMOND_ORE || block.getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            if (!this.gameManager.isInGame()) {
               event.setCancelled(true);
            } else {
               Location blockLocation = block.getLocation();
               if (this.trackedDiamondBlocks.containsKey(blockLocation)) {
                  event.setCancelled(true);
               } else {
                  Team playerTeam = this.teamManager.getTeam(event.getPlayer());
                  if (playerTeam == null) {
                     event.setCancelled(true);
                  } else {
                     event.setCancelled(true);
                     this.trackedDiamondBlocks.put(blockLocation, block.getType());
                     this.giveDiamondDrops(event.getPlayer(), block);
                     this.gameManager.addDiamond(playerTeam);
                     block.setType(Material.BEDROCK);
                     int scheduledResetVersion = this.resetVersion;
                     Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                        this.restoreDiamondOre(blockLocation, scheduledResetVersion);
                     }, 600L);
                  }
               }
            }
         }
      }
   }

   public void clearTrackedDiamonds() {
      this.trackedDiamondBlocks.clear();
      ++this.resetVersion;
   }

   private void giveDiamondDrops(Player player, Block block) {
      Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand(), player);
      if (((Collection)drops).isEmpty()) {
         drops = List.of(new ItemStack(Material.DIAMOND));
      }

      Iterator var4 = ((Collection)drops).iterator();

      while(true) {
         ItemStack drop;
         do {
            do {
               do {
                  if (!var4.hasNext()) {
                     return;
                  }

                  drop = (ItemStack)var4.next();
               } while(drop == null);
            } while(drop.getType() != Material.DIAMOND);
         } while(drop.getAmount() <= 0);

         Map<Integer, ItemStack> leftovers = player.getInventory().addItem(new ItemStack[]{drop});
         Iterator var7 = leftovers.values().iterator();

         while(var7.hasNext()) {
            ItemStack leftover = (ItemStack)var7.next();
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5D, 0.5D, 0.5D), leftover);
         }
      }
   }

   private void restoreDiamondOre(Location blockLocation, int scheduledResetVersion) {
      if (scheduledResetVersion == this.resetVersion) {
         Material originalMaterial = (Material)this.trackedDiamondBlocks.remove(blockLocation);
         if (originalMaterial != null) {
            blockLocation.getBlock().setType(originalMaterial);
         }
      }
   }
}
