package eu.projnull.spelis.svci.misc;

import eu.projnull.spelis.svci.voice.BroadcasterState;
import eu.projnull.spelis.svci.voice.BroadcasterState.Broadcaster;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public final class BroadcastHudTask {

    private BukkitTask task;

    public void start(JavaPlugin plugin) {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        BroadcasterState state = BroadcasterState.inst();

        for (World world : Bukkit.getWorlds()) {
            Broadcaster broadcaster = state.getBroadcast(world.getUID());
            if (broadcaster == null) continue;

            Component message = buildMessage(broadcaster);
            for (Player player : world.getPlayers()) {
                player.sendActionBar(message);
            }
        }
    }

    private Component buildMessage(Broadcaster broadcaster) {
        long remaining = broadcaster.getEndTimeMillis() - System.currentTimeMillis();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60;

        String timer = String.format("%d:%02d remaining", minutes, seconds);

        return switch (broadcaster.getType()) {
            case LIVE -> Component.text("🔴 LIVE broadcast", NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text(" - " + timer, NamedTextColor.GRAY));
            case FILE -> Component.text("📢 " + broadcaster.getFileName(), NamedTextColor.GOLD, TextDecoration.BOLD)
                    .append(Component.text(" - " + timer, NamedTextColor.GRAY));
        };
    }
}
