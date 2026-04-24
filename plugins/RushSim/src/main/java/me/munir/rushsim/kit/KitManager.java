package me.munir.rushsim.kit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class KitManager {
   private final Map<UUID, Kit> playerKits = new HashMap();
   private final Map<UUID, Kit> roundKitOverrides = new HashMap();

   public void setKit(Player player, Kit kit) {
      this.playerKits.put(player.getUniqueId(), kit);
   }

   public Kit getKit(Player player) {
      Kit roundKit = (Kit)this.roundKitOverrides.get(player.getUniqueId());
      return roundKit != null ? roundKit : (Kit)this.playerKits.get(player.getUniqueId());
   }

   public void setRoundKit(Player player, Kit kit) {
      if (kit == null) {
         this.roundKitOverrides.remove(player.getUniqueId());
      } else {
         this.roundKitOverrides.put(player.getUniqueId(), kit);
      }
   }

   public void clearRoundKits() {
      this.roundKitOverrides.clear();
   }

   public void clearKits() {
      this.playerKits.clear();
      this.roundKitOverrides.clear();
   }
}
