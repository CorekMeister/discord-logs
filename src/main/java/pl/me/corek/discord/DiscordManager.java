package pl.me.corek.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import pl.me.corek.Main;
import pl.me.corek.config.Config;

public class DiscordManager extends ListenerAdapter {

    private final Main plugin;
    private JDA jda;

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
                    .setActivity(Activity.watching(settings.activityMessage))
                    .addEventListeners(this)
                    .build();
            jda.awaitReady();
            plugin.getLogger().info("Bot Discord połączony pomyślnie!");
        } catch (Exception e) {
            plugin.getLogger().severe("Nie udało się połączyć z botem Discord: " + e.getMessage());
        }
    }

    public void stop() {
        if (jda != null) {
            jda.shutdownNow();
            plugin.getLogger().info("Bot Discord został zatrzymany.");
        }
    }

    public void sendLogMessage(String message) {
        if (jda == null || !Config.get().commandLogging.enabled) return;

        try {
            TextChannel channel = jda.getTextChannelById(Config.get().commandLogging.channelId);
            if (channel != null) {
                channel.sendMessage(message).queue();
            } else {
                plugin.getLogger().warning("Nie znaleziono kanału do logów o ID: " + Config.get().commandLogging.channelId);
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("ID kanału do logów w config.json jest nieprawidłowe!");
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Config.RemoteExecution settings = Config.get().remoteExecution;
        if (!settings.enabled || event.getAuthor().isBot() || !event.isFromGuild() || event.getMember() == null) {
            return;
        }

        if (!event.getChannel().getId().equals(settings.channelId)) {
            return;
        }

        boolean hasRole = event.getMember().getRoles().stream()
                .anyMatch(role -> role.getId().equals(settings.requiredRoleId));

        if (!hasRole) {
            return;
        }

        String command = event.getMessage().getContentRaw();

        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        event.getMessage().addReaction(Emoji.fromUnicode("🤔")).queue();

        final String finalCommand = command;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        });

        String confirmationMessage = settings.consoleLogFormat.replace("{command}", finalCommand);

        event.getChannel().sendMessage(confirmationMessage).queue(
                (success) -> event.getMessage().removeReaction(Emoji.fromUnicode("🤔")).queue(v ->
                        event.getMessage().addReaction(Emoji.fromUnicode("✅")).queue()
                ),
                (error) -> plugin.getLogger().warning("Nie udało się wysłać potwierdzenia na kanał Discord o ID: " + settings.channelId)
        );
    }
}