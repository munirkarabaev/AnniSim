package me.munir.annisim.util;

import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class ModeAccess {
   private static final String HUB_CONTROLLER = "HubController";

   private ModeAccess() {
   }

   public static boolean isAnniSimActive() {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(HUB_CONTROLLER);
      if (plugin == null || !plugin.isEnabled()) {
         return true;
      }

      try {
         Method method = plugin.getClass().getMethod("isModeActive", String.class);
         Object result = method.invoke(plugin, "annisim");
         return result instanceof Boolean && (Boolean)result;
      } catch (ReflectiveOperationException ignored) {
         return true;
      }
   }

   public static boolean isHubControllerPresent() {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(HUB_CONTROLLER);
      return plugin != null && plugin.isEnabled();
   }
}
