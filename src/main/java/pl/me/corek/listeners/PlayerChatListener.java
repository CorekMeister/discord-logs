package pl.me.corek.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.me.corek.Main;
import pl.me.corek.config.Config;

public class PlayerChatListener implements Listener {

    private final Main plugin;

    public PlayerChatListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!Config.get().chatLogging.enabled) {
            return;
        }
        plugin.getDiscordManager().sendChatMessage(event.getPlayer(), event.getMessage());
    }
}