package me.munir.annisim.command;

import me.munir.annisim.game.WinTracker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ResetCommand implements CommandExecutor {
   private final WinTracker winTracker;
   private final CommandProtectionManager commandProtectionManager;

   public ResetCommand(WinTracker winTracker, CommandProtectionManager commandProtectionManager) {
      this.winTracker = winTracker;
      this.commandProtectionManager = commandProtectionManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!this.commandProtectionManager.canUseProtectedCommand(sender)) {
         sender.sendMessage("Only operators can use this command while protected mode is enabled.");
         return true;
      } else {
         this.winTracker.reset();
         sender.sendMessage("Red vs Blue win tracker reset.");
         return true;
      }
   }
}
