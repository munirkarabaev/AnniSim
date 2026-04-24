package me.munir.annisim.command;

import me.munir.annisim.game.GameManager;
import me.munir.annisim.loadout.LoadoutManager;
import me.munir.annisim.loadout.LoadoutType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BanLoadoutCommand implements CommandExecutor {
   private final LoadoutManager loadoutManager;
   private final GameManager gameManager;
   private final CommandProtectionManager commandProtectionManager;

   public BanLoadoutCommand(LoadoutManager loadoutManager, GameManager gameManager, CommandProtectionManager commandProtectionManager) {
      this.loadoutManager = loadoutManager;
      this.gameManager = gameManager;
      this.commandProtectionManager = commandProtectionManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!this.commandProtectionManager.canUseProtectedCommand(sender)) {
         sender.sendMessage("Only operators can use this command while protected mode is enabled.");
         return true;
      } else if (args.length != 1) {
         sender.sendMessage("Usage: /ban <loadout>");
         return true;
      } else {
         LoadoutType loadoutType = LoadoutType.fromInput(args[0]);
         if (loadoutType == null) {
            sender.sendMessage("Unknown loadout: " + args[0]);
            return true;
         } else if (loadoutType == LoadoutType.DEFAULT) {
            sender.sendMessage("Default cannot be banned.");
            return true;
         } else if (!this.loadoutManager.banLoadout(loadoutType, this.gameManager)) {
            sender.sendMessage(loadoutType.getDisplayName() + " is already banned.");
            return true;
         } else {
            Bukkit.broadcastMessage(loadoutType.getDisplayName() + " loadout has been banned and all affected players were moved to Default.");
            sender.sendMessage("Banned " + loadoutType.getDisplayName() + ".");
            return true;
         }
      }
   }
}
