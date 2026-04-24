package me.munir.rushsim.command;

import me.munir.rushsim.arena.ArenaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadCommand implements CommandExecutor {
   private final JavaPlugin plugin;
   private final ArenaManager arenaManager;

   public ReloadCommand(JavaPlugin plugin, ArenaManager arenaManager) {
      this.plugin = plugin;
      this.arenaManager = arenaManager;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      this.plugin.reloadConfig();
      this.arenaManager.reload();
      sender.sendMessage("RushSim config reloaded.");
      return true;
   }
}
