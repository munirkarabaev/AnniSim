package me.munir.rushsim.loadout;

public enum LoadoutType {
   DEFAULT("Rush");

   private final String displayName;

   LoadoutType(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return this.displayName;
   }
}
