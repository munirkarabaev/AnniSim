package me.munir.hubcontroller.util;

import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class PluginBridge {
   private PluginBridge() {
   }

   public static boolean isRoundActive(String pluginName) {
      return callBoolean(pluginName, "isRoundActive");
   }

   public static void resetForModeSwitch(String pluginName) {
      callVoid(pluginName, "resetForModeSwitch");
   }

   public static void preparePlayerForHubLobby(String pluginName, Player player) {
      callVoid(pluginName, "preparePlayerForHubLobby", Player.class, player);
   }

   public static void openTeamSelectionMenu(String pluginName, Player player) {
      callVoid(pluginName, "openTeamSelectionMenu", Player.class, player);
   }

   public static void openKitSelectionMenu(String pluginName, Player player) {
      callVoid(pluginName, "openKitSelectionMenu", Player.class, player);
   }

   public static void openLoadoutSelectionMenu(String pluginName, Player player) {
      callVoid(pluginName, "openLoadoutSelectionMenu", Player.class, player);
   }

   private static boolean callBoolean(String pluginName, String methodName) {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
      if (plugin == null || !plugin.isEnabled()) {
         return false;
      }

      try {
         Method method = plugin.getClass().getMethod(methodName);
         Object result = method.invoke(plugin);
         return result instanceof Boolean && (Boolean)result;
      } catch (ReflectiveOperationException ignored) {
         return false;
      }
   }

   private static void callVoid(String pluginName, String methodName) {
      callVoid(pluginName, methodName, null, null);
   }

   private static void callVoid(String pluginName, String methodName, Class<?> parameterType, Object argument) {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
      if (plugin == null || !plugin.isEnabled()) {
         return;
      }

      try {
         Method method = parameterType == null ? plugin.getClass().getMethod(methodName) : plugin.getClass().getMethod(methodName, parameterType);
         if (parameterType == null) {
            method.invoke(plugin);
         } else {
            method.invoke(plugin, argument);
         }
      } catch (ReflectiveOperationException ignored) {
      }
   }
}
