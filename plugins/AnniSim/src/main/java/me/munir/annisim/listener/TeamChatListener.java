package me.munir.annisim.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Iterator;
import me.munir.annisim.chat.ChatMuteManager;
import me.munir.annisim.team.Team;
import me.munir.annisim.team.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TeamChatListener implements Listener {
   private final TeamManager teamManager;
   private final ChatMuteManager chatMuteManager;

   public TeamChatListener(TeamManager teamManager, ChatMuteManager chatMuteManager) {
      this.teamManager = teamManager;
      this.chatMuteManager = chatMuteManager;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onAsyncChat(AsyncChatEvent event) {
      Player player = event.getPlayer();
      String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
      if (message.isEmpty()) {
         event.setCancelled(true);
      } else if (this.chatMuteManager.isMuted(player)) {
         event.setCancelled(true);
         player.sendMessage(Component.text("You are muted.", NamedTextColor.RED));
      } else {
         Team team = this.teamManager.getTeam(player);
         String globalMessage;
         if (team == null) {
            if (message.startsWith("!")) {
               globalMessage = message.substring(1).trim();
               if (globalMessage.isEmpty()) {
                  event.setCancelled(true);
                  return;
               }

               event.message(Component.text(globalMessage));
            }

         } else {
            event.setCancelled(true);
            if (!message.startsWith("!")) {
               Component formattedMessage = ((TextComponent)((TextComponent)Component.text("[Team] ", this.getTeamColor(team)).append(Component.text(player.getName(), this.getTeamColor(team)))).append(Component.text(": ", NamedTextColor.GRAY))).append(Component.text(message, NamedTextColor.WHITE));
               Iterator var10 = Bukkit.getOnlinePlayers().iterator();

               while(var10.hasNext()) {
                  Player onlinePlayer = (Player)var10.next();
                  if (team == this.teamManager.getTeam(onlinePlayer)) {
                     onlinePlayer.sendMessage(formattedMessage);
                  }
               }

            } else {
               globalMessage = message.substring(1).trim();
               if (!globalMessage.isEmpty()) {
                  Component formattedMessage = ((TextComponent)((TextComponent)Component.text("[All] ", NamedTextColor.GRAY).append(Component.text(player.getName(), this.getTeamColor(team)))).append(Component.text(": ", NamedTextColor.GRAY))).append(Component.text(globalMessage, NamedTextColor.WHITE));
                  Iterator var7 = Bukkit.getOnlinePlayers().iterator();

                  while(var7.hasNext()) {
                     Player onlinePlayer = (Player)var7.next();
                     onlinePlayer.sendMessage(formattedMessage);
                  }

               }
            }
         }
      }
   }

   private NamedTextColor getTeamColor(Team team) {
      return team.getTextColor();
   }
}
