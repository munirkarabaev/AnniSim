package me.munir.annisim.command;

import me.munir.annisim.game.GameManager;
import me.munir.annisim.util.ModeAccess;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class EndGameCommand implements CommandExecutor {
   private final GameManager gameManager;

   public EndGameCommand(GameManager gameManager) {
      this.gameManager = gameManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!ModeAccess.isAnniSimActive()) {
         return this.delegateToRushSim(sender, args);
      }

      if (!this.gameManager.endGame()) {
         sender.sendMessage("The game is not running.");
         return true;
      } else {
         sender.sendMessage("The round has ended.");
         return true;
      }
   }

   private boolean delegateToRushSim(CommandSender sender, String[] args) {
      Plugin rushSim = Bukkit.getPluginManager().getPlugin("RushSim");
      if (rushSim == null || !rushSim.isEnabled()) {
         sender.sendMessage("RushSim is not available.");
         return true;
      }

      try {
         Method method = rushSim.getClass().getMethod("handleSharedEndGame", CommandSender.class, String[].class);
         method.invoke(rushSim, sender, args);
      } catch (ReflectiveOperationException exception) {
         sender.sendMessage("RushSim end command is unavailable.");
      }

      return true;
   }
}
