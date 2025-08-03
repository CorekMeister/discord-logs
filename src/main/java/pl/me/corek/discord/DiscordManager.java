package pl.me.corek.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import pl.me.corek.Main;
import pl.me.corek.config.Config;

public class DiscordManager extends ListenerAdapter {

    private final Main plugin;
    private JDA jda;
    private BukkitTask statusUpdateTask;

    public DiscordManager(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Config.BotSettings settings = Config.get().botSettings;
        if (settings.token.equals("TUTAJ_WKLEJ_TOKEN_BOTA")) {
            plugin.getLogger().warning("Token bota Discord nie został ustawiony w config.json! Bot nie zostanie uruchomiony.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(settings.token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .build();
            jda.awaitReady();
            plugin.getLogger().info("Bot Discord połączony pomyślnie!");

            registerSlashCommands();
            startStatusUpdater();

        } catch (Exception e) {
            plugin.getLogger().severe("Nie udało się połączyć z botem Discord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (statusUpdateTask != null) {
            statusUpdateTask.cancel();
            statusUpdateTask = null;
        }
        if (jda != null) {
            jda.shutdownNow();
            plugin.getLogger().info("Bot Discord został zatrzymany.");
        }
    }

    private void registerSlashCommands() {
        Config.RemoteExecution settings = Config.get().remoteExecution;
        if (jda == null || !settings.enabled) return;

        jda.updateCommands().addCommands(
                Commands.slash(settings.commandName, settings.commandDescription)
                        .addOptions(new OptionData(OptionType.STRING, settings.argumentName, settings.argumentDescription, true))
                        .setGuildOnly(true)
        ).queue();
    }

    private void startStatusUpdater() {
        Config.BotSettings settings = Config.get().botSettings;
        if (jda == null || settings.statusFormat.isEmpty()) return;

        statusUpdateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            int onlinePlayers = Bukkit.getOnlinePlayers().size();
            int maxPlayers = Bukkit.getMaxPlayers();
            String status = settings.statusFormat
                    .replace("{players}", String.valueOf(onlinePlayers))
                    .replace("{max_players}", String.valueOf(maxPlayers));
            jda.getPresence().setActivity(Activity.watching(status));
        }, 0L, settings.statusUpdateIntervalSeconds * 20L);
    }

    public void sendLogMessage(String message) {
        if (jda == null || !Config.get().commandLogging.enabled) return;
        TextChannel channel = jda.getTextChannelById(Config.get().commandLogging.channelId);
        if (channel != null) {
            channel.sendMessage(message).queue();
        }
    }

    public void sendChatMessage(Player player, String message) {
        Config.ChatLogging settings = Config.get().chatLogging;
        if (jda == null || !settings.enabled) return;

        TextChannel channel = jda.getTextChannelById(settings.channelId);
        if (channel != null) {
            String formattedMessage = settings.chatFormat
                    .replace("{player}", player.getName())
                    .replace("{message}", message);
            channel.sendMessage(formattedMessage).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Config.RemoteExecution settings = Config.get().remoteExecution;
        if (!settings.enabled || !event.getName().equals(settings.commandName)) {
            return;
        }

        boolean hasRole = event.getMember().getRoles().stream()
                .anyMatch(role -> role.getId().equals(settings.requiredRoleId));

        if (!hasRole) {
            event.reply("Nie masz uprawnień do użycia tej komendy.").setEphemeral(true).queue();
            return;
        }

        String command = event.getOption(settings.argumentName).getAsString();

        event.deferReply(true).queue();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        });

        String confirmationMessage = settings.consoleLogFormat.replace("{command}", command);
        event.getHook().sendMessage(confirmationMessage).queue();
    }
}