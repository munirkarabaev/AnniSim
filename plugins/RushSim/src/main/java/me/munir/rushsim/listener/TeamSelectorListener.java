package me.munir.rushsim.listener;

import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.team.Team;
import me.munir.rushsim.team.TeamManager;
import me.munir.rushsim.util.ModeAccess;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeamSelectorListener implements Listener {
   private final GameManager gameManager;
   private final TeamManager teamManager;

   public TeamSelectorListener(GameManager gameManager, TeamManager teamManager) {
      this.gameManager = gameManager;
      this.teamManager = teamManager;
   }

   public void openMenu(Player player) {
      if (ModeAccess.isRushSimActive()) {
         player.openInventory(this.createMenu());
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (!ModeAccess.isRushSimActive() || !"Select Team".equals(event.getView().getTitle())) {
         return;
      }

      event.setCancelled(true);
      HumanEntity humanEntity = event.getWhoClicked();
      if (!(humanEntity instanceof Player player)) {
         return;
      }

      Team team = this.getClickedTeam(event.getCurrentItem());
      if (team == null) {
         return;
      }

      if (this.gameManager.isInGame() && team == Team.RED) {
         player.sendMessage("You cannot join the rushing team mid-round.");
         player.closeInventory();
         return;
      }

      this.teamManager.setTeam(player, team);
      if (this.gameManager.isInGame()) {
         this.gameManager.preparePlayerForRound(player);
      }

      player.sendMessage("You selected " + team.getDisplayName() + ".");
      player.closeInventory();
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (ModeAccess.isRushSimActive() && "Select Team".equals(event.getView().getTitle())) {
         event.setCancelled(true);
      }
   }

   private Inventory createMenu() {
      Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 9, "Select Team");
      inventory.setItem(2, this.createItem(Team.RED));
      inventory.setItem(4, this.createItem(Team.SPECTATOR));
      inventory.setItem(6, this.createItem(Team.BLUE));
      return inventory;
   }

   private ItemStack createItem(Team team) {
      ItemStack itemStack = new ItemStack(team.getMenuMaterial());
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         itemMeta.displayName(Component.text(team.getDisplayName(), team.getTextColor()));
         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }

   private Team getClickedTeam(ItemStack itemStack) {
      if (itemStack == null) {
         return null;
      }

      for (Team team : Team.values()) {
         if (team.getMenuMaterial() == itemStack.getType()) {
            return team;
         }
      }

      return null;
   }
}
