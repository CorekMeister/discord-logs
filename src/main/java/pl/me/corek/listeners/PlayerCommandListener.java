package pl.me.corek.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import pl.me.corek.Main;
import pl.me.corek.config.Config;

public class PlayerCommandListener implements Listener {

    private final Main plugin;

    public PlayerCommandListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Config.CommandLogging settings = Config.get().commandLogging;
        if (!settings.enabled) return;

        Player player = event.getPlayer();
        String command = event.getMessage();

        boolean isIgnored = settings.ignoredCommands.stream()
                .anyMatch(ignoredCmd -> command.toLowerCase().startsWith(ignoredCmd.toLowerCase()));

        if (isIgnored) {
            return;
        }

        String logMessage = settings.logFormat
                .replace("{player}", player.getName())
                .replace("{command}", command);

        plugin.getDiscordManager().sendLogMessage(logMessage);
    }
}