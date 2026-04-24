package me.munir.annisim.command;

import me.munir.annisim.loadout.LoadoutManager;
import me.munir.annisim.loadout.LoadoutType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnbanLoadoutCommand implements CommandExecutor {
   private final LoadoutManager loadoutManager;
   private final CommandProtectionManager commandProtectionManager;

   public UnbanLoadoutCommand(LoadoutManager loadoutManager, CommandProtectionManager commandProtectionManager) {
      this.loadoutManager = loadoutManager;
      this.commandProtectionManager = commandProtectionManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!this.commandProtectionManager.canUseProtectedCommand(sender)) {
         sender.sendMessage("Only operators can use this command while protected mode is enabled.");
         return true;
      } else if (args.length != 1) {
         sender.sendMessage("Usage: /unban <loadout>");
         return true;
      } else {
         LoadoutType loadoutType = LoadoutType.fromInput(args[0]);
         if (loadoutType == null) {
            sender.sendMessage("Unknown loadout: " + args[0]);
            return true;
         } else if (!this.loadoutManager.unbanLoadout(loadoutType)) {
            sender.sendMessage(loadoutType.getDisplayName() + " is not banned.");
            return true;
         } else {
            sender.sendMessage("Unbanned " + loadoutType.getDisplayName() + ".");
            return true;
         }
      }
   }
}
