package pl.me.corek.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Config {

    private static transient Config instance;
    public BotSettings botSettings;
    public CommandLogging commandLogging;
    public ChatLogging chatLogging;
    public RemoteExecution remoteExecution;
    public Messages messages;

    public Config() {
        this.botSettings = new BotSettings();
        this.commandLogging = new CommandLogging();
        this.chatLogging = new ChatLogging();
        this.remoteExecution = new RemoteExecution();
        this.messages = new Messages();
    }

    public static void load(File file) {
        if (!file.exists()) {
            instance = new Config();
            instance.toFile(file);
            return;
        }

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            instance = new Gson().fromJson(reader, Config.class);
            if (instance == null) {
                instance = new Config();
            }
        } catch (IOException e) {
            e.printStackTrace();
            instance = new Config();
        }
    }

    public void toFile(File file) {
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                gson.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config get() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public static class BotSettings {
        public String statusFormat = "graczy online: {players}/{max_players}";
        public long statusUpdateIntervalSeconds = 15;
        public String token = "TUTAJ_WKLEJ_TOKEN_BOTA";
    }

    public static class CommandLogging {
        public boolean enabled = true;
        public String channelId = "ID_KANALU_DO_LOGOW_KOMEND";
        public String logFormat = "`{player}` wykonał komendę: `{command}`";
        public List<String> ignoredCommands = Arrays.asList("/login", "/l", "/register", "/reg");
    }

    public static class ChatLogging {
        public boolean enabled = true;
        public String channelId = "ID_KANALU_DO_LOGOW_CZATU";
        public String chatFormat = "**{player}**: {message}";
    }

    public static class RemoteExecution {
        public boolean enabled = true;
        public String commandName = "command";
        public String commandDescription = "Wykonuje komendę na serwerze Minecraft.";
        public String argumentName = "polecenie";
        public String argumentDescription = "Komenda do wykonania (np. 'say Hello World')";
        public String requiredRoleId = "ID_ROLI_WYMAGANEJ_DO_KOMEND";
        public String consoleLogFormat = ":computer: **KONSOLA (DISCORD)**: `{command}`";
    }

    public static class Messages {
        public String reloadSuccess = "&aKonfiguracja pluginu Corek-Discord została przeładowana!";
        public String noPermission = "&cNie masz uprawnień do tej komendy.";
        public String playerOnly = "&cTa komenda jest dostępna tylko dla graczy.";
    }
}