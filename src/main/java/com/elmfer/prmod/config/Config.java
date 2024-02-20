package com.elmfer.prmod.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import com.elmfer.prmod.ParkourRecorder;
import com.elmfer.prmod.ui.NumberLineView.TimeStampFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Config {
    public static final File CONFIG_FILE = new File("config/" + ParkourRecorder.MOD_ID + ".json");

    private static JsonObject config = new JsonObject();
    private static CompletableFuture<Void> saveOperation = null;

    static {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));

            config = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            ParkourRecorder.LOGGER.info("No config file, loading defaults.");

            config.addProperty("loopMode", isLoopMode());
            config.addProperty("hiddenIp", isHiddenIp());
            config.addProperty("showInputs", showInputs());
            config.addProperty("timeStampFormat", getTimeStampFormat().name());

            save();
        }
    }

    public static boolean isLoopMode() {
        if (config.has("loopMode")) {
            return config.get("loopMode").getAsBoolean();
        }

        return false;
    }

    public static boolean isHiddenIp() {
        if (config.has("hiddenIp")) {
            return config.get("hiddenIp").getAsBoolean();
        }

        return false;
    }

    public static boolean showInputs() {
        if (config.has("showInputs")) {
            return config.get("showInputs").getAsBoolean();
        }

        return false;
    }
    
    public static boolean playbackAttacks() {
        if (config.has("playbackAttacks")) {
            return config.get("playbackAttacks").getAsBoolean();
        }

        return false;
    }
    
    public static boolean playbackUses() {
        if (config.has("playbackUses")) {
            return config.get("playbackUses").getAsBoolean();
        }

        return false;
    }

    public static TimeStampFormat getTimeStampFormat() {
        if (config.has("timeStampFormat")) {
            return TimeStampFormat.valueOf(config.get("timeStampFormat").getAsString());
        }

        return TimeStampFormat.DEFAULT;
    }

    public static void setLoopMode(boolean loopMode) {
        config.addProperty("loopMode", loopMode);
    }

    public static void setHiddenIp(boolean hiddenIp) {
        config.addProperty("hiddenIp", hiddenIp);
    }

    public static void setShowInputs(boolean showInputs) {
        config.addProperty("showInputs", showInputs);
    }

    public static void setTimeStampFormat(TimeStampFormat format) {
        config.addProperty("timeStampFormat", format.name());
    }
    
    public static void setPlaybackAttacks(boolean playbackAttacks) {
        config.addProperty("playbackAttacks", playbackAttacks);
    }
    
    public static void setPlaybackUses(boolean playbackUses) {
        config.addProperty("playbackUses", playbackUses);
    }

    public static void save() {
        waitForSave();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(config);

        saveOperation = CompletableFuture.runAsync(() -> {
            try {
                Files.write(CONFIG_FILE.toPath(), json.getBytes());
            } catch (IOException e) {
                ParkourRecorder.LOGGER.error("Failed to save config file", e);
            }
            saveOperation = null;
        });
    }

    public static void waitForSave() {
        if (saveOperation != null) {
            saveOperation.join();
        }
    }
}
