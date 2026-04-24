package me.munir.annisim.game;

import me.munir.annisim.team.Team;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class WinTracker {
   private static final String RED_WINS_PATH = "wins.red";
   private static final String BLUE_WINS_PATH = "wins.blue";
   private final JavaPlugin plugin;
   private int redWins;
   private int blueWins;

   public WinTracker(JavaPlugin plugin) {
      this.plugin = plugin;
      this.load();
   }

   public int getWins(Team team) {
      if (team == Team.RED) {
         return this.redWins;
      } else {
         return team == Team.BLUE ? this.blueWins : 0;
      }
   }

   public void recordWin(Team team) {
      if (team == Team.RED) {
         ++this.redWins;
      } else if (team == Team.BLUE) {
         ++this.blueWins;
      }

      this.save();
   }

   public void reset() {
      this.redWins = 0;
      this.blueWins = 0;
      this.save();
   }

   private void load() {
      FileConfiguration config = this.plugin.getConfig();
      this.redWins = config.getInt("wins.red");
      this.blueWins = config.getInt("wins.blue");
   }

   private void save() {
      FileConfiguration config = this.plugin.getConfig();
      config.set("wins.red", this.redWins);
      config.set("wins.blue", this.blueWins);
      this.plugin.saveConfig();
   }
}
