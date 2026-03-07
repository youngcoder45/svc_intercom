package eu.projnull.spelis.svci.commands;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.projnull.spelis.svci.Intercom;
import eu.projnull.spelis.svci.voice.BroadcasterState;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.util.Arrays;

public class Helpers {
    public static void getActiveWorldsSuggestion(SuggestionsBuilder builder) {
        BroadcasterState.inst().activeBroadcastWorldNames().forEach(builder::suggest);
    }

    public static void getAllWorldsSuggestion(SuggestionsBuilder builder) {
        Bukkit.getWorlds()
                .stream()
                .map(World::getName)
                .forEach(builder::suggest);
    }

    public static void getAllSoundsSuggestion(SuggestionsBuilder builder) {
        File soundsDir = new File(
                Intercom.getPlugin(Intercom.class).getDataFolder(),
                "sounds"
        );
        File[] files = soundsDir.listFiles();
        if (files == null) return;

        Arrays.stream(files)
                .filter(File::isFile)
                .map(File::getName)
                .forEach(builder::suggest);
    }
}
