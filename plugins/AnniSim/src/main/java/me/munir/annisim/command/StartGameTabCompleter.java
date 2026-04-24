package me.munir.annisim.command;

import java.util.List;
import me.munir.annisim.arena.ArenaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class StartGameTabCompleter implements TabCompleter {
   private static final List<String> START_OPTIONS = List.of("random_map", "random_kit_loadout", "random_all");
   private final ArenaManager arenaManager;

   public StartGameTabCompleter(ArenaManager arenaManager) {
      this.arenaManager = arenaManager;
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length == 1) {
         String input = args[0].toLowerCase();
         return java.util.stream.Stream.concat(this.arenaManager.getAvailableMapNames().stream(), START_OPTIONS.stream()).filter((value) -> {
            return value.startsWith(input);
         }).toList();
      } else if (args.length == 2 && "random_kit_loadout".equalsIgnoreCase(args[0])) {
         String input = args[1].toLowerCase();
         return this.arenaManager.getAvailableMapNames().stream().filter((mapName) -> {
            return mapName.startsWith(input);
         }).toList();
      } else {
         return List.of();
      }
   }
}
