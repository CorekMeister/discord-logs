package pl.me.corek;

import org.bukkit.plugin.java.JavaPlugin;
import pl.me.corek.commands.DiscordReloadCommand;
import pl.me.corek.config.Config;
import pl.me.corek.discord.DiscordManager;
import pl.me.corek.listeners.PlayerCommandListener;

import java.io.File;

public final class Main extends JavaPlugin {

    private DiscordManager discordManager;
    private File configFile;

    @Override
    public void onEnable() {
        this.configFile = new File(getDataFolder(), "config.json");
        Config.load(configFile);

        this.discordManager = new DiscordManager(this);
        this.discordManager.start();

        getCommand("discordreload").setExecutor(new DiscordReloadCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerCommandListener(this), this);

        getLogger().info("Plugin Corek-Discord został włączony!");
    }

    @Override
    public void onDisable() {
        if (discordManager != null) {
            discordManager.stop();
        }
        getLogger().info("Plugin Corek-Discord został wyłączony.");
    }

    public void reloadConfiguration() {
        if (discordManager != null) {
            discordManager.stop();
        }
        Config.load(configFile);
        this.discordManager = new DiscordManager(this);
        this.discordManager.start();
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }
}