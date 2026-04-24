package me.munir.hubcontroller;

import java.util.Objects;
import me.munir.hubcontroller.command.ModeCommand;
import me.munir.hubcontroller.lobby.SharedLobbyItems;
import me.munir.hubcontroller.lobby.SharedLobbyListener;
import me.munir.hubcontroller.mode.ModeManager;
import me.munir.hubcontroller.mode.ServerMode;
import me.munir.hubcontroller.util.PluginBridge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HubControllerPlugin extends JavaPlugin {
   private final ModeManager modeManager = new ModeManager();
   private SharedLobbyItems sharedLobbyItems;

   @Override
   public void onEnable() {
      this.sharedLobbyItems = new SharedLobbyItems(this);
      this.getServer().getPluginManager().registerEvents(new SharedLobbyListener(this, this.sharedLobbyItems), this);
      PluginCommand modeCommand = (PluginCommand)Objects.requireNonNull(this.getCommand("mode"));
      modeCommand.setExecutor(new ModeCommand(this));
   }

   public ServerMode getActiveMode() {
      return this.modeManager.getActiveMode();
   }

   public boolean isModeActive(String modeName) {
      ServerMode mode = ServerMode.fromInput(modeName);
      return mode != null && this.modeManager.isActive(mode);
   }

   public boolean isAnyRoundActive() {
      return PluginBridge.isRoundActive("AnniSim") || PluginBridge.isRoundActive("RushSim");
   }

   public void switchMode(ServerMode targetMode) {
      PluginBridge.resetForModeSwitch("AnniSim");
      PluginBridge.resetForModeSwitch("RushSim");
      this.modeManager.setActiveMode(targetMode);

      for (Player player : Bukkit.getOnlinePlayers()) {
         this.resetPlayerState(player);
         this.preparePlayerForActiveLobby(player);
         this.sharedLobbyItems.applyToPlayer(player);
      }
   }

   public void preparePlayerForActiveLobby(Player player) {
      PluginBridge.preparePlayerForHubLobby(this.getActiveGameplayPluginName(), player);
   }

   public String getActiveGameplayPluginName() {
      return this.getActiveMode() == ServerMode.RUSHSIM ? "RushSim" : "AnniSim";
   }

   private void resetPlayerState(Player player) {
      player.getInventory().clear();
      player.setAllowFlight(false);
      player.setFlying(false);
      player.setFireTicks(0);
      player.setFoodLevel(20);
      player.setSaturation(20.0F);
      player.setHealth(player.getMaxHealth());
      player.setGameMode(GameMode.ADVENTURE);
      player.getActivePotionEffects().forEach((potionEffect) -> player.removePotionEffect(potionEffect.getType()));
   }
}
