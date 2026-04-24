package me.munir.annisim.command;

import me.munir.annisim.arena.ArenaManager;
import me.munir.annisim.arena.ArenaMap;

public class StartGameRequestParser {
   private static final String RANDOM_MAP_TOKEN = "random_map";
   private static final String RANDOM_KIT_LOADOUT_TOKEN = "random_kit_loadout";
   private static final String RANDOM_ALL_TOKEN = "random_all";
   private final ArenaManager arenaManager;

   public StartGameRequestParser(ArenaManager arenaManager) {
      this.arenaManager = arenaManager;
   }

   public StartGameRequest parse(String[] args) {
      if (args.length == 1) {
         if (RANDOM_MAP_TOKEN.equalsIgnoreCase(args[0])) {
            return new StartGameRequest(StartGameMode.RANDOM_MAP, (ArenaMap)null);
         } else if (RANDOM_ALL_TOKEN.equalsIgnoreCase(args[0])) {
            return new StartGameRequest(StartGameMode.RANDOM_ALL, (ArenaMap)null);
         } else {
            ArenaMap arenaMap = ArenaMap.fromName(args[0]);
            return arenaMap == null ? null : new StartGameRequest(StartGameMode.STANDARD, arenaMap);
         }
      } else if (args.length == 2 && RANDOM_KIT_LOADOUT_TOKEN.equalsIgnoreCase(args[0])) {
         ArenaMap arenaMap = ArenaMap.fromName(args[1]);
         return arenaMap == null ? null : new StartGameRequest(StartGameMode.RANDOM_KIT_LOADOUT, arenaMap);
      } else {
         return null;
      }
   }

   public String getUsage() {
      return "/startgame <mapname|random_map|random_all|random_kit_loadout mapname>";
   }

   public ArenaManager getArenaManager() {
      return this.arenaManager;
   }
}
