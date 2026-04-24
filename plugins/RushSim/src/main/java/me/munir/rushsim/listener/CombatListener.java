package me.munir.rushsim.listener;

import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.team.Team;
import me.munir.rushsim.team.TeamManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class CombatListener implements Listener {
   private final GameManager gameManager;
   private final TeamManager teamManager;

   public CombatListener(GameManager gameManager, TeamManager teamManager) {
      this.gameManager = gameManager;
      this.teamManager = teamManager;
   }

   @EventHandler
   public void onEntityDamage(EntityDamageByEntityEvent event) {
      if (!this.gameManager.isInGame()) {
         return;
      }

      if (!(event.getEntity() instanceof Player victim)) {
         return;
      }

      Player attacker = this.getAttackingPlayer(event.getDamager());
      if (attacker == null) {
         return;
      }

      Team attackerTeam = this.teamManager.getTeam(attacker);
      Team victimTeam = this.teamManager.getTeam(victim);
      if (attackerTeam == null || victimTeam == null || attackerTeam == victimTeam) {
         event.setCancelled(true);
         return;
      }

      if (this.gameManager.isDefenderInvulnerable(victim)) {
         event.setCancelled(true);
      }

      if (this.gameManager.isDefenderInvulnerable(attacker)) {
         this.gameManager.removeDefenderInvulnerability(attacker);
      }
   }

   private Player getAttackingPlayer(Entity entity) {
      if (entity instanceof Player player) {
         return player;
      }

      if (entity instanceof Arrow arrow) {
         ProjectileSource projectileSource = arrow.getShooter();
         if (projectileSource instanceof Player player) {
            return player;
         }
      }

      return null;
   }
}
