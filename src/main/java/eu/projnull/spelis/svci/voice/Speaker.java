package eu.projnull.spelis.svci.voice;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Speaker {
    private final UUID worldId;
    private final double x;
    private final double y;
    private final double z;
    private final double range;
    private final String name;

    public Speaker(UUID worldId, double x, double y, double z, double range, String name) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.range = range;
        this.name = name;
    }

    public UUID getWorldId() {
        return worldId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getRange() {
        return range;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldId);
        if (world == null) return null;
        return new Location(world, x, y, z);
    }

    public boolean isInRange(Location playerLocation) {
        if (playerLocation == null || playerLocation.getWorld() == null) return false;
        if (!playerLocation.getWorld().getUID().equals(worldId)) return false;
        
        double dx = playerLocation.getX() - x;
        double dy = playerLocation.getY() - y;
        double dz = playerLocation.getZ() - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        return distance <= range;
    }

    @Override
    public String toString() {
        World world = Bukkit.getWorld(worldId);
        String worldName = world != null ? world.getName() : worldId.toString();
        return String.format("%s @ %s (%.1f, %.1f, %.1f) range: %.1f", name, worldName, x, y, z, range);
    }
}
