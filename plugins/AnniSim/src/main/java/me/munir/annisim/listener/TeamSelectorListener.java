package me.munir.annisim.listener;

import me.munir.annisim.lobby.LobbyItems;
import me.munir.annisim.game.GameManager;
import me.munir.annisim.team.Team;
import me.munir.annisim.team.TeamManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeamSelectorListener implements Listener {
   private static final String TEAM_MENU_TITLE = "Select Team";
   private final LobbyItems lobbyItems;
   private final GameManager gameManager;
   private final TeamManager teamManager;

   public TeamSelectorListener(LobbyItems lobbyItems, GameManager gameManager, TeamManager teamManager) {
      this.lobbyItems = lobbyItems;
      this.gameManager = gameManager;
      this.teamManager = teamManager;
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         Action action = event.getAction();
         if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (this.lobbyItems.isTeamSelector(event.getItem())) {
               event.setCancelled(true);
               event.getPlayer().openInventory(this.createTeamSelectionMenu());
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if ("Select Team".equals(event.getView().getTitle())) {
         event.setCancelled(true);
         HumanEntity var3 = event.getWhoClicked();
         if (var3 instanceof Player) {
            Player player = (Player)var3;
            ItemStack clickedItem = event.getCurrentItem();
            Team selectedTeam = this.getClickedTeam(clickedItem);
            if (selectedTeam != null) {
               this.selectTeam(player, selectedTeam);
            }
         }
      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if ("Select Team".equals(event.getView().getTitle())) {
         event.setCancelled(true);
      }

   }

   private void selectTeam(Player player, Team team) {
      if (this.gameManager.isInGame() && team.isPlayingTeam()) {
         player.closeInventory();
         player.sendMessage("You cannot join red or blue while a round is already running.");
         return;
      }

      this.teamManager.setTeam(player, team);
      if (this.gameManager.isInGame() && team == Team.SPECTATOR) {
         this.gameManager.preparePlayerForRound(player);
      }

      player.closeInventory();
      player.sendMessage("You selected " + team.getDisplayName() + ".");
   }

   private Inventory createTeamSelectionMenu() {
      Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 9, "Select Team");
      inventory.setItem(2, this.createMenuItem(Team.RED));
      inventory.setItem(4, this.createMenuItem(Team.SPECTATOR));
      inventory.setItem(6, this.createMenuItem(Team.BLUE));
      return inventory;
   }

   private ItemStack createMenuItem(Team team) {
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
      } else {
         Team[] var2 = Team.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Team team = var2[var4];
            if (team.getMenuMaterial() == itemStack.getType()) {
               return team;
            }
         }

         return null;
      }
   }
}
