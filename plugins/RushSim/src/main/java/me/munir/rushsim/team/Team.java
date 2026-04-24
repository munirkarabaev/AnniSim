package me.munir.rushsim.team;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public enum Team {
   RED("Red Team", Material.RED_WOOL, NamedTextColor.RED, true),
   BLUE("Blue Team", Material.BLUE_WOOL, NamedTextColor.BLUE, true),
   SPECTATOR("Spectator", Material.COMPASS, NamedTextColor.GRAY, false);

   private final String displayName;
   private final Material menuMaterial;
   private final NamedTextColor textColor;
   private final boolean playingTeam;

   Team(String displayName, Material menuMaterial, NamedTextColor textColor, boolean playingTeam) {
      this.displayName = displayName;
      this.menuMaterial = menuMaterial;
      this.textColor = textColor;
      this.playingTeam = playingTeam;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public Material getMenuMaterial() {
      return this.menuMaterial;
   }

   public NamedTextColor getTextColor() {
      return this.textColor;
   }

   public boolean isPlayingTeam() {
      return this.playingTeam;
   }
}
