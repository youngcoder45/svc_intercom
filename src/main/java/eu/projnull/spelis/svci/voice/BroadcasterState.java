package eu.projnull.spelis.svci.voice;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class BroadcasterState {

    private static final BroadcasterState INSTANCE = new BroadcasterState();

    // Key = worldId
    private final Map<UUID, Broadcaster> broadcasts = new ConcurrentHashMap<>();

    private BroadcasterState() {
    }

    public static BroadcasterState inst() {
        return INSTANCE;
    }

    public void startBroadcast(Broadcaster broadcaster) {
        UUID worldId = broadcaster.getWorldId();

        // only allow one broadcast per world
        broadcasts.putIfAbsent(worldId, broadcaster);
    }

    public Broadcaster getBroadcast(UUID worldId) {
        Broadcaster b = broadcasts.get(worldId);

        if (b == null) return null;

        if (b.isExpired()) {
            broadcasts.remove(worldId);
            return null;
        }

        return b;
    }

    public Stream<String> activeBroadcastWorldNames() {
        long now = System.currentTimeMillis();

        return broadcasts.values().stream()
                .filter(b -> b.getEndTimeMillis() > now)
                .map(b -> Bukkit.getWorld(b.getWorldId()))
                .filter(Objects::nonNull)
                .map(World::getName);
    }

    public void stopBroadcastWithMessage(UUID worldId, String message) {
        Broadcaster b = broadcasts.remove(worldId);
        if (b == null) return;

        World world = Bukkit.getWorld(worldId);
        if (world == null) return;

        for (Player p : world.getPlayers()) {
            p.sendMessage(message);
        }
    }

    public boolean isBroadcastActive(UUID worldId) {
        return getBroadcast(worldId) != null;
    }

    public static final class Broadcaster {

        private final UUID playerId;      // null for FILE mode
        private final UUID worldId;
        private final BroadcastType type;
        private final long endTimeMillis;
        private final String fileName;    // only for FILE mode

        public Broadcaster(
                UUID playerId,
                UUID worldId,
                BroadcastType type,
                long durationMillis,
                String fileName
        ) {
            if (worldId == null)
                throw new IllegalArgumentException("worldId cannot be null");

            if (durationMillis <= 0)
                throw new IllegalArgumentException("duration must be > 0");

            if (type == BroadcastType.LIVE && playerId == null)
                throw new IllegalArgumentException("LIVE requires playerId");

            if (type == BroadcastType.FILE && fileName == null)
                throw new IllegalArgumentException("FILE requires fileName");

            this.playerId = playerId;
            this.worldId = worldId;
            this.type = type;
            this.endTimeMillis = System.currentTimeMillis() + durationMillis;
            this.fileName = fileName;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > endTimeMillis;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public UUID getWorldId() {
            return worldId;
        }

        public BroadcastType getType() {
            return type;
        }

        public String getFileName() {
            return fileName;
        }

        public long getEndTimeMillis() {
            return endTimeMillis;
        }

        public enum BroadcastType {
            LIVE,
            FILE
        }
    }
}
