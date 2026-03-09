package eu.projnull.spelis.svci.voice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eu.projnull.spelis.svci.Intercom;
import org.bukkit.Location;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SpeakerManager {
    private static final SpeakerManager INSTANCE = new SpeakerManager();
    private final Map<UUID, List<Speaker>> speakers = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File dataFile;

    private SpeakerManager() {
    }

    public static SpeakerManager inst() {
        return INSTANCE;
    }

    public void initialize(File dataFolder) {
        this.dataFile = new File(dataFolder, "speakers.json");
        load();
    }

    public void addSpeaker(Speaker speaker) {
        speakers.computeIfAbsent(speaker.getWorldId(), k -> new ArrayList<>()).add(speaker);
        save();
    }

    public boolean removeSpeaker(UUID worldId, String name) {
        List<Speaker> worldSpeakers = speakers.get(worldId);
        if (worldSpeakers == null) return false;

        boolean removed = worldSpeakers.removeIf(s -> s.getName().equalsIgnoreCase(name));
        if (removed) {
            if (worldSpeakers.isEmpty()) {
                speakers.remove(worldId);
            }
            save();
        }
        return removed;
    }

    public List<Speaker> getSpeakers(UUID worldId) {
        return speakers.getOrDefault(worldId, Collections.emptyList());
    }

    public List<Speaker> getSpeakersInRange(Location location) {
        if (location == null || location.getWorld() == null) return Collections.emptyList();
        
        List<Speaker> worldSpeakers = speakers.get(location.getWorld().getUID());
        if (worldSpeakers == null) return Collections.emptyList();

        return worldSpeakers.stream()
                .filter(speaker -> speaker.isInRange(location))
                .collect(Collectors.toList());
    }

    public Speaker getSpeaker(UUID worldId, String name) {
        List<Speaker> worldSpeakers = speakers.get(worldId);
        if (worldSpeakers == null) return null;

        return worldSpeakers.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean speakerExists(UUID worldId, String name) {
        return getSpeaker(worldId, name) != null;
    }

    private void save() {
        try {
            dataFile.getParentFile().mkdirs();
            
            List<SpeakerData> data = new ArrayList<>();
            for (Map.Entry<UUID, List<Speaker>> entry : speakers.entrySet()) {
                for (Speaker speaker : entry.getValue()) {
                    data.add(new SpeakerData(
                            speaker.getWorldId().toString(),
                            speaker.getX(),
                            speaker.getY(),
                            speaker.getZ(),
                            speaker.getRange(),
                            speaker.getName()
                    ));
                }
            }

            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(data, writer);
            }
            
            Intercom.LOGGER.info("Saved {} speakers to disk", data.size());
        } catch (IOException e) {
            Intercom.LOGGER.error("Failed to save speakers", e);
        }
    }

    private void load() {
        if (!dataFile.exists()) {
            Intercom.LOGGER.info("No speakers file found, starting fresh");
            return;
        }

        try (FileReader reader = new FileReader(dataFile)) {
            Type listType = new TypeToken<List<SpeakerData>>() {}.getType();
            List<SpeakerData> data = gson.fromJson(reader, listType);

            if (data == null) {
                Intercom.LOGGER.warn("Speakers file is empty or corrupted");
                return;
            }

            speakers.clear();
            for (SpeakerData speakerData : data) {
                UUID worldId = UUID.fromString(speakerData.worldId);
                Speaker speaker = new Speaker(
                        worldId,
                        speakerData.x,
                        speakerData.y,
                        speakerData.z,
                        speakerData.range,
                        speakerData.name
                );
                speakers.computeIfAbsent(worldId, k -> new ArrayList<>()).add(speaker);
            }

            Intercom.LOGGER.info("Loaded {} speakers from disk", data.size());
        } catch (IOException e) {
            Intercom.LOGGER.error("Failed to load speakers", e);
        }
    }

    private static class SpeakerData {
        String worldId;
        double x;
        double y;
        double z;
        double range;
        String name;

        SpeakerData(String worldId, double x, double y, double z, double range, String name) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.range = range;
            this.name = name;
        }
    }
}
