package me.munir.annisim.loadout;

public enum LoadoutType {
   DEFAULT("Default"),
   WARRIOR("Warrior"),
   TANK("Tank"),
   POWERPAD("Launchpad"),
   STRIDER("Strider"),
   ARCHER("Archer"),
   FIRE("Fire"),
   FORTUNE("Fortune"),
   GOD_APPLE("God Apple");

   private final String displayName;

   LoadoutType(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public boolean matches(String value) {
      return this.name().equalsIgnoreCase(value) || this.displayName.equalsIgnoreCase(value);
   }

   public static LoadoutType fromInput(String value) {
      if (value == null || value.isBlank()) {
         return null;
      } else {
         for (LoadoutType loadoutType : values()) {
            if (loadoutType.matches(value)) {
               return loadoutType;
            }
         }

         return null;
      }
   }
}
