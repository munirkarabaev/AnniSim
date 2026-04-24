package me.munir.annisim.command;

import me.munir.annisim.game.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndGameCommand implements CommandExecutor {
   private final GameManager gameManager;

   public EndGameCommand(GameManager gameManager) {
      this.gameManager = gameManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!this.gameManager.endGame()) {
         sender.sendMessage("The game is not running.");
         return true;
      } else {
         sender.sendMessage("The round has ended.");
         return true;
      }
   }
}
