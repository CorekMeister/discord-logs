package pl.me.corek.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import pl.me.corek.Main;
import pl.me.corek.config.Config;

public class DiscordReloadCommand implements CommandExecutor {

    private final Main plugin;

    public DiscordReloadCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("corekdiscord.reload")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Config.get().messages.noPermission));
            return true;
        }

        plugin.reloadConfiguration();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Config.get().messages.reloadSuccess));
        return true;
    }
}