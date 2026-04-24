package me.munir.annisim.command;

import me.munir.annisim.arena.ArenaMap;

public record StartGameRequest(StartGameMode mode, ArenaMap arenaMap) {
   public String getCountdownLabel() {
      return this.mode.usesRandomMap() ? "random map" : this.arenaMap.getCommandName();
   }
}
