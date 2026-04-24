package me.munir.hubcontroller.mode;

public class ModeManager {
   private ServerMode activeMode = ServerMode.ANNISIM;

   public ServerMode getActiveMode() {
      return this.activeMode;
   }

   public boolean isActive(ServerMode mode) {
      return this.activeMode == mode;
   }

   public void setActiveMode(ServerMode mode) {
      if (mode != null) {
         this.activeMode = mode;
      }
   }
}
