package dev.niekv.sysutils.bungee.listener;

import dev.niekv.sysutils.AutoServer;
import dev.niekv.sysutils.bungee.BungeePlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerServerSwitchListener implements Listener {

    private final BungeePlugin bungeePlugin;

    public PlayerServerSwitchListener(BungeePlugin bungeePlugin) {
        this.bungeePlugin = bungeePlugin;
    }

    @EventHandler
    public void onPlayerSwitch(ServerConnectEvent event) {
        ServerInfo serverInfo = event.getTarget();

        if(this.bungeePlugin.getLocalCache().containsKey(serverInfo.getMotd())) {
            AutoServer dataObject = this.bungeePlugin.getLocalCache().get(serverInfo.getMotd());

            if(dataObject.getPermission() != null && !dataObject.getPermission().isEmpty()) {
                if(!event.getPlayer().hasPermission(dataObject.getPermission())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You don't have access to the server you want to connect to."));
                }
            }
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage((TextComponent.fromLegacyText(ChatColor.RED + "Something went wrong while trying to connect to this server.")));
        }
    }
}
