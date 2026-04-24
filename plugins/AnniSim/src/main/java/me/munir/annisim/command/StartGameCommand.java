package me.munir.annisim.command;

import me.munir.annisim.arena.ArenaManager;
import me.munir.annisim.game.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartGameCommand implements CommandExecutor {
   private final GameManager gameManager;
   private final CommandProtectionManager commandProtectionManager;
   private final StartGameRequestParser startGameRequestParser;

   public StartGameCommand(GameManager gameManager, ArenaManager arenaManager, CommandProtectionManager commandProtectionManager) {
      this.gameManager = gameManager;
      this.commandProtectionManager = commandProtectionManager;
      this.startGameRequestParser = new StartGameRequestParser(arenaManager);
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!this.commandProtectionManager.canUseProtectedCommand(sender)) {
         sender.sendMessage("Only operators can use this command while protected mode is enabled.");
         return true;
      }

      StartGameRequest startGameRequest = this.startGameRequestParser.parse(args);
      if (startGameRequest == null) {
         sender.sendMessage("Usage: " + this.startGameRequestParser.getUsage());
         sender.sendMessage("Available maps: " + String.join(", ", this.startGameRequestParser.getArenaManager().getAvailableMapNames()));
         return true;
      } else if (!this.gameManager.startGame(startGameRequest)) {
         String mapLabel = startGameRequest.mode().usesRandomMap() ? "random map" : startGameRequest.arenaMap().getCommandName();
         sender.sendMessage("The game is already running, already starting, or the " + mapLabel + " arena could not be loaded.");
         return true;
      } else {
         sender.sendMessage("Starting " + startGameRequest.getCountdownLabel() + " in 5 seconds.");
         return true;
      }
   }
}
