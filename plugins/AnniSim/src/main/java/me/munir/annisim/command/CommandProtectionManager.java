package me.munir.annisim.command;

import org.bukkit.command.CommandSender;

public class CommandProtectionManager {
   private boolean protectedMode;

   public boolean isProtectedMode() {
      return this.protectedMode;
   }

   public boolean toggleProtectedMode() {
      this.protectedMode = !this.protectedMode;
      return this.protectedMode;
   }

   public boolean canUseProtectedCommand(CommandSender sender) {
      return !this.protectedMode || sender.isOp();
   }
}
