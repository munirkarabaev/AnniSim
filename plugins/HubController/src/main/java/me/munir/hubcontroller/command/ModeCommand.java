package me.munir.hubcontroller.command;

import me.munir.hubcontroller.HubControllerPlugin;
import me.munir.hubcontroller.mode.ServerMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ModeCommand implements CommandExecutor {
   private final HubControllerPlugin plugin;

   public ModeCommand(HubControllerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.isOp()) {
         sender.sendMessage("Only operators can use this command.");
         return true;
      }

      if (args.length != 1) {
         sender.sendMessage("Usage: /mode <annisim|rushsim>");
         return true;
      }

      ServerMode targetMode = ServerMode.fromInput(args[0]);
      if (targetMode == null) {
         sender.sendMessage("Unknown mode: " + args[0]);
         return true;
      }

      if (this.plugin.getActiveMode() == targetMode) {
         sender.sendMessage("The server is already in " + targetMode.getCommandName() + " mode.");
         return true;
      }

      if (this.plugin.isAnyRoundActive()) {
         sender.sendMessage("You cannot switch modes while a round is running.");
         return true;
      }

      this.plugin.switchMode(targetMode);
      sender.sendMessage("Switched the server to " + targetMode.getCommandName() + " mode.");
      return true;
   }
}
