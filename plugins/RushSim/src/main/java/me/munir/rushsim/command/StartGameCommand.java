package me.munir.rushsim.command;

import me.munir.rushsim.arena.ArenaManager;
import me.munir.rushsim.game.GameManager;
import me.munir.rushsim.util.ModeAccess;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartGameCommand implements CommandExecutor {
   private final GameManager gameManager;
   private final ArenaManager arenaManager;

   public StartGameCommand(GameManager gameManager, ArenaManager arenaManager) {
      this.gameManager = gameManager;
      this.arenaManager = arenaManager;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!ModeAccess.isRushSimActive()) {
         sender.sendMessage("RushSim is not the active mode.");
         return true;
      }

      if (args.length != 1) {
         sender.sendMessage("Usage: /startgame <map>");
         sender.sendMessage("Available maps: " + String.join(", ", this.arenaManager.getAvailableMapNames()));
         return true;
      }

      if (!this.gameManager.startGame(args[0])) {
         sender.sendMessage("Could not start RushSim on that map.");
         return true;
      }

      sender.sendMessage("RushSim started on " + args[0] + ".");
      return true;
   }
}
