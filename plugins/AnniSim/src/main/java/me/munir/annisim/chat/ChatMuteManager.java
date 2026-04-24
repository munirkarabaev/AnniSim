package me.munir.annisim.chat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public class ChatMuteManager {
   private final Set<UUID> mutedPlayers = new HashSet();

   public boolean toggleMute(Player player) {
      UUID playerId = player.getUniqueId();
      if (this.mutedPlayers.contains(playerId)) {
         this.mutedPlayers.remove(playerId);
         return false;
      } else {
         this.mutedPlayers.add(playerId);
         return true;
      }
   }

   public boolean isMuted(Player player) {
      return this.mutedPlayers.contains(player.getUniqueId());
   }
}
