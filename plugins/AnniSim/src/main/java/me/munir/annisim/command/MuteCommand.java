package me.munir.annisim.command;

import me.munir.annisim.chat.ChatMuteManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteCommand implements CommandExecutor {
   private final ChatMuteManager chatMuteManager;

   public MuteCommand(ChatMuteManager chatMuteManager) {
      this.chatMuteManager = chatMuteManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.isOp()) {
         sender.sendMessage("Only operators can use this command.");
         return true;
      } else if (args.length != 1) {
         sender.sendMessage("Usage: /mute <player>");
         return true;
      } else {
         Player target = Bukkit.getPlayerExact(args[0]);
         if (target == null) {
            sender.sendMessage("Player not found: " + args[0]);
            return true;
         } else {
            boolean muted = this.chatMuteManager.toggleMute(target);
            if (muted) {
               sender.sendMessage("Muted " + target.getName() + ".");
               target.sendMessage("You have been muted.");
               return true;
            } else {
               sender.sendMessage("Unmuted " + target.getName() + ".");
               target.sendMessage("You have been unmuted.");
               return true;
            }
         }
      }
   }
}
