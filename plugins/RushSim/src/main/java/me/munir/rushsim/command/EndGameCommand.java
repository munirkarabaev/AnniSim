package me.munir.rushsim.command;

import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.util.ModeAccess;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndGameCommand implements CommandExecutor {
   private final GameManager gameManager;

   public EndGameCommand(GameManager gameManager) {
      this.gameManager = gameManager;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!ModeAccess.isRushSimActive()) {
         sender.sendMessage("RushSim is not the active mode.");
         return true;
      }

      if (!this.gameManager.endGame()) {
         sender.sendMessage("No RushSim round is running.");
         return true;
      }

      sender.sendMessage("RushSim round ended.");
      return true;
   }
}
