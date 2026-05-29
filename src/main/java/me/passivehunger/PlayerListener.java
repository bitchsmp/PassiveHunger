package me.passivehunger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {

    private final PassiveHunger plugin;

    public PlayerListener(PassiveHunger plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getHungerTicker() != null) {
            plugin.getHungerTicker().removePlayer(event.getPlayer().getUniqueId());
        }
    }
}
