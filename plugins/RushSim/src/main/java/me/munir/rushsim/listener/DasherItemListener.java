package me.munir.rushsim.listener;

import me.munir.rushsim.game.RoundItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DasherItemListener implements Listener {
   private final RoundItems roundItems;

   public DasherItemListener(RoundItems roundItems) {
      this.roundItems = roundItems;
   }

   @EventHandler
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      if (this.roundItems.isBlinkItem(event.getItemDrop().getItemStack()) || this.roundItems.isSuccubusItem(event.getItemDrop().getItemStack())) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      event.getDrops().removeIf((itemStack) -> {
         return this.roundItems.isBlinkItem(itemStack) || this.roundItems.isSuccubusItem(itemStack);
      });
   }
}
