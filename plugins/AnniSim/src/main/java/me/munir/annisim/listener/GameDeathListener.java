package me.munir.annisim.listener;

import me.munir.annisim.game.GameManager;
import me.munir.annisim.kit.Kit;
import me.munir.annisim.kit.KitManager;
import me.munir.annisim.loadout.LoadoutManager;
import me.munir.annisim.loadout.LoadoutType;
import me.munir.annisim.team.Team;
import me.munir.annisim.team.TeamManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GameDeathListener implements Listener {
   private final JavaPlugin plugin;
   private final GameManager gameManager;
   private final KitManager kitManager;
   private final LoadoutManager loadoutManager;
   private final TeamManager teamManager;

   public GameDeathListener(JavaPlugin plugin, GameManager gameManager, KitManager kitManager, LoadoutManager loadoutManager, TeamManager teamManager) {
      this.plugin = plugin;
      this.gameManager = gameManager;
      this.kitManager = kitManager;
      this.loadoutManager = loadoutManager;
      this.teamManager = teamManager;
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      if (this.gameManager.isInGame()) {
         Player player = event.getEntity();
         Player killer = player.getKiller();
         if (killer != null) {
            event.setDeathMessage(this.buildKillMessage(killer, player));
         }

         this.gameManager.markPlayerDead(player, player.getLocation());
         this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.spigot().respawn();
         });
      }
   }

   @EventHandler
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      if (this.gameManager.isInGame()) {
         Location spectatorLocation = this.gameManager.getSpectatorRespawnLocation(event.getPlayer());
         if (spectatorLocation != null) {
            event.setRespawnLocation(spectatorLocation);
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
               event.getPlayer().teleport(spectatorLocation);
               event.getPlayer().setGameMode(GameMode.SPECTATOR);
            });
         }
      }
   }

   private String buildKillMessage(Player killer, Player victim) {
      String var10000 = this.formatPlayerName(killer);
      return var10000 + " §7killed " + this.formatPlayerName(victim) + "§7.";
   }

   private String formatPlayerName(Player player) {
      String color = this.getTeamColorCode(player);
      return color + player.getName() + " (" + this.getIdentifier(this.getKitDisplayName(player)) + "/" + this.getIdentifier(this.getLoadoutDisplayName(player)) + ")";
   }

   private String getKitDisplayName(Player player) {
      Kit kit = this.kitManager.getKit(player);
      if (kit == null) {
         return "None";
      } else {
         String var10000;
         switch(kit) {
         case DASHER:
            var10000 = "Dasher";
            break;
         case SUCCUBUS:
            var10000 = "Succubus";
            break;
         case ACROBAT:
            var10000 = "Acrobat";
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }
   }

   private String getLoadoutDisplayName(Player player) {
      LoadoutType loadoutType = this.loadoutManager.getSelectedLoadout(player);
      return loadoutType.getDisplayName();
   }

   private String getIdentifier(String value) {
      String normalized = value == null ? "NON" : value.replaceAll("[^A-Za-z]", "").toUpperCase();
      if (normalized.isEmpty()) {
         return "NON";
      } else {
         return normalized.length() <= 3 ? normalized : normalized.substring(0, 3);
      }
   }

   private String getTeamColorCode(Player player) {
      Team team = this.teamManager.getTeam(player);
      if (team == Team.BLUE) {
         return "§9";
      } else {
         return team == Team.RED ? "§c" : "§f";
      }
   }
}
