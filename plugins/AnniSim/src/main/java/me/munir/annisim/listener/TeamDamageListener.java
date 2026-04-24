package me.munir.annisim.listener;

import me.munir.annisim.game.GameManager;
import me.munir.annisim.team.Team;
import me.munir.annisim.team.TeamManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class TeamDamageListener implements Listener {
   private final GameManager gameManager;
   private final TeamManager teamManager;

   public TeamDamageListener(GameManager gameManager, TeamManager teamManager) {
      this.gameManager = gameManager;
      this.teamManager = teamManager;
   }

   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (this.gameManager.isInGame()) {
         Entity var3 = event.getEntity();
         if (var3 instanceof Player) {
            Player victim = (Player)var3;
            Player attacker = this.getAttackingPlayer(event.getDamager());
            if (attacker != null) {
               Team attackerTeam = this.teamManager.getTeam(attacker);
               Team victimTeam = this.teamManager.getTeam(victim);
               if (attackerTeam == null || victimTeam == null || !attackerTeam.isPlayingTeam() || !victimTeam.isPlayingTeam() || attackerTeam == victimTeam) {
                  event.setCancelled(true);
               }

            }
         }
      }
   }

   private Player getAttackingPlayer(Entity entity) {
      if (entity instanceof Player) {
         Player player = (Player)entity;
         return player;
      } else {
         if (entity instanceof Arrow) {
            Arrow arrow = (Arrow)entity;
            ProjectileSource var4 = arrow.getShooter();
            if (var4 instanceof Player) {
               Player player = (Player)var4;
               return player;
            }
         }

         return null;
      }
   }
}
