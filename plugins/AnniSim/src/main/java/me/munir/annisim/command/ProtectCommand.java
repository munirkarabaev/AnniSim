package me.munir.annisim.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ProtectCommand implements CommandExecutor {
   private final CommandProtectionManager commandProtectionManager;

   public ProtectCommand(CommandProtectionManager commandProtectionManager) {
      this.commandProtectionManager = commandProtectionManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.isOp()) {
         sender.sendMessage("Only operators can use this command.");
         return true;
      } else {
         boolean protectedMode = this.commandProtectionManager.toggleProtectedMode();
         sender.sendMessage(protectedMode ? "Protected mode enabled. Only operators can use key game commands." : "Protected mode disabled. Everyone can use key game commands.");
         return true;
      }
   }
}
