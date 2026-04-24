package me.munir.annisim.listener;

import java.util.Objects;
import me.munir.annisim.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class GameWinListener implements Listener {
   private final JavaPlugin plugin;
   private final GameManager gameManager;

   public GameWinListener(JavaPlugin plugin, GameManager gameManager) {
      this.plugin = plugin;
      this.gameManager = gameManager;
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      BukkitScheduler var10000 = Bukkit.getScheduler();
      JavaPlugin var10001 = this.plugin;
      GameManager var10002 = this.gameManager;
      Objects.requireNonNull(var10002);
      var10000.runTask(var10001, var10002::checkWinConditions);
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      BukkitScheduler var10000 = Bukkit.getScheduler();
      JavaPlugin var10001 = this.plugin;
      GameManager var10002 = this.gameManager;
      Objects.requireNonNull(var10002);
      var10000.runTask(var10001, var10002::checkWinConditions);
   }
}
