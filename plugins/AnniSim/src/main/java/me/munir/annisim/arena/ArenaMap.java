package me.munir.annisim.arena;

import me.munir.annisim.team.Team;

public enum ArenaMap {
   GRASSLANDS("grasslands", "grasslands", "grasslands_template", 271, 285, 66, 313, 285, 144, true),
   COASTALV3("coastalv3", "coastal", "coastal_template", -1550, 12, 427, -1589, 12, 430, false),
   ANDORRA("andorra", "CleanMaps", "CleanMaps_template", 112, -18, 343, 21, -18, 343, false),
   NATURE("nature", "CleanMaps", "CleanMaps_template", 530, 7, -82, 445, 7, -3, false),
   ARID("arid", "CleanMaps", "CleanMaps_template", -294, 12, -40, -242, 12, 35, false),
   COASTAL("coastal", "CleanMaps", "CleanMaps_template", 449, -20, 268, 445, -20, 364, false),
   CANYON("canyon", "CleanMaps", "CleanMaps_template", 105, 24, -85, 50, 24, -28, false),
   AFTERMATH("aftermath", "Hamafter", "Hamafter_template", 3201, 148, 4338, 3107, 148, 4432, true),
   HALMET("hamlet", "Hamafter", "Hamafter_template", 878, 196, 329, 803, 196, 384, true),
   CASTAWAY("castaway", "Cleanmaps2", "Cleanmaps2_template", 1567, 210, -240, 1567, 210, -155, false),
   CLASHSTAL("clashstal", "Cleanmaps2", "Cleanmaps2_template", 1263, 191, -167, 1274, 191, -77, false),
   SIEGE("siege", "Cleanmaps2", "Cleanmaps2_template", 901, 193, -59, 959, 193, 22, false),
   CHASM("chasm", "Cleanmaps2", "Cleanmaps2_template", 2963, 164, 4030, 2941, 164, 4118, false);

   private final String commandName;
   private final String worldName;
   private final String templateWorldName;
   private final int redSpawnX;
   private final int redSpawnY;
   private final int redSpawnZ;
   private final int blueSpawnX;
   private final int blueSpawnY;
   private final int blueSpawnZ;
   private final boolean blockMobSpawns;

   ArenaMap(String commandName, String worldName, String templateWorldName, int redSpawnX, int redSpawnY, int redSpawnZ, int blueSpawnX, int blueSpawnY, int blueSpawnZ, boolean blockMobSpawns) {
      this.commandName = commandName;
      this.worldName = worldName;
      this.templateWorldName = templateWorldName;
      this.redSpawnX = redSpawnX;
      this.redSpawnY = redSpawnY;
      this.redSpawnZ = redSpawnZ;
      this.blueSpawnX = blueSpawnX;
      this.blueSpawnY = blueSpawnY;
      this.blueSpawnZ = blueSpawnZ;
      this.blockMobSpawns = blockMobSpawns;
   }

   public String getCommandName() {
      return this.commandName;
   }

   public String getWorldName() {
      return this.worldName;
   }

   public String getTemplateWorldName() {
      return this.templateWorldName;
   }

   public int getSpawnX(Team team) {
      return team == Team.BLUE ? this.blueSpawnX : this.redSpawnX;
   }

   public int getSpawnY(Team team) {
      return team == Team.BLUE ? this.blueSpawnY : this.redSpawnY;
   }

   public int getSpawnZ(Team team) {
      return team == Team.BLUE ? this.blueSpawnZ : this.redSpawnZ;
   }

   public boolean shouldBlockMobSpawns() {
      return this.blockMobSpawns;
   }

   public static ArenaMap fromName(String name) {
      for (ArenaMap arenaMap : values()) {
         if (arenaMap.commandName.equalsIgnoreCase(name)) {
            return arenaMap;
         }
      }

      return null;
   }
}
