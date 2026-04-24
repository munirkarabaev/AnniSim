package me.munir.annisim.command;

public enum StartGameMode {
   STANDARD,
   RANDOM_MAP,
   RANDOM_KIT_LOADOUT,
   RANDOM_ALL;

   public boolean usesRandomMap() {
      return this == RANDOM_MAP || this == RANDOM_ALL;
   }

   public boolean usesRandomKitLoadout() {
      return this == RANDOM_KIT_LOADOUT || this == RANDOM_ALL;
   }
}
