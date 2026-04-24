package me.munir.hubcontroller.mode;

public enum ServerMode {
   ANNISIM("annisim"),
   RUSHSIM("rushsim");

   private final String commandName;

   ServerMode(String commandName) {
      this.commandName = commandName;
   }

   public String getCommandName() {
      return this.commandName;
   }

   public static ServerMode fromInput(String value) {
      if (value == null) {
         return null;
      }

      for (ServerMode mode : values()) {
         if (mode.commandName.equalsIgnoreCase(value) || mode.name().equalsIgnoreCase(value)) {
            return mode;
         }
      }

      return null;
   }
}
