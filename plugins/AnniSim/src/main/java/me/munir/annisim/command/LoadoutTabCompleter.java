package me.munir.annisim.command;

import java.util.Arrays;
import java.util.List;
import me.munir.annisim.loadout.LoadoutType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class LoadoutTabCompleter implements TabCompleter {
   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length != 1) {
         return List.of();
      } else {
         String input = args[0].toLowerCase();
         return Arrays.stream(LoadoutType.values()).map(LoadoutType::name).map(String::toLowerCase).filter((name) -> {
            return name.startsWith(input);
         }).toList();
      }
   }
}
