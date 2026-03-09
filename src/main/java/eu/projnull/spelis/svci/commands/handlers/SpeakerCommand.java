package eu.projnull.spelis.svci.commands.handlers;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import eu.projnull.spelis.svci.commands.Handler;
import eu.projnull.spelis.svci.commands.Helpers;
import eu.projnull.spelis.svci.voice.Speaker;
import eu.projnull.spelis.svci.voice.SpeakerManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class SpeakerCommand implements Handler {

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
        return Commands.literal("speaker")
                .requires(cs -> cs.getSender().hasPermission("svcintercom.speaker"))
                .then(buildAddCommand())
                .then(buildRemoveCommand())
                .then(buildListCommand());
    }

    private ArgumentBuilder<CommandSourceStack, ?> buildAddCommand() {
        return Commands.literal("add")
                .requires(cs -> cs.getSender().hasPermission("svcintercom.speaker.add"))
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("world", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    Helpers.getAllWorldsSuggestion(builder);
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .then(Commands.argument("range", DoubleArgumentType.doubleArg(1.0, 1000.0))
                                                                .executes(ctx -> {
                                                                    CommandSender sender = ctx.getSource().getSender();
                                                                    String name = StringArgumentType.getString(ctx, "name");
                                                                    String worldName = StringArgumentType.getString(ctx, "world");
                                                                    double x = DoubleArgumentType.getDouble(ctx, "x");
                                                                    double y = DoubleArgumentType.getDouble(ctx, "y");
                                                                    double z = DoubleArgumentType.getDouble(ctx, "z");
                                                                    double range = DoubleArgumentType.getDouble(ctx, "range");

                                                                    World world = Bukkit.getWorld(worldName);
                                                                    if (world == null) {
                                                                        sender.sendMessage("§cWorld not found: " + worldName);
                                                                        return 0;
                                                                    }

                                                                    UUID worldId = world.getUID();
                                                                    if (SpeakerManager.inst().speakerExists(worldId, name)) {
                                                                        sender.sendMessage("§cA speaker with that name already exists in this world");
                                                                        return 0;
                                                                    }

                                                                    Speaker speaker = new Speaker(worldId, x, y, z, range, name);
                                                                    SpeakerManager.inst().addSpeaker(speaker);

                                                                    sender.sendMessage("§aAdded speaker: §f" + speaker.toString());
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                        // Add shorthand for current location (player only)
                        .then(Commands.argument("range", DoubleArgumentType.doubleArg(1.0, 1000.0))
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    if (!(sender instanceof Player player)) {
                                        sender.sendMessage("§cYou must be a player to use this command without coordinates");
                                        return 0;
                                    }

                                    String name = StringArgumentType.getString(ctx, "name");
                                    double range = DoubleArgumentType.getDouble(ctx, "range");

                                    Location loc = player.getLocation();
                                    World world = loc.getWorld();
                                    UUID worldId = world.getUID();

                                    if (SpeakerManager.inst().speakerExists(worldId, name)) {
                                        sender.sendMessage("§cA speaker with that name already exists in this world");
                                        return 0;
                                    }

                                    Speaker speaker = new Speaker(worldId, loc.getX(), loc.getY(), loc.getZ(), range, name);
                                    SpeakerManager.inst().addSpeaker(speaker);

                                    sender.sendMessage("§aAdded speaker: §f" + speaker.toString());
                                    return 1;
                                })
                        )
                );
    }

    private ArgumentBuilder<CommandSourceStack, ?> buildRemoveCommand() {
        return Commands.literal("remove")
                .requires(cs -> cs.getSender().hasPermission("svcintercom.speaker.remove"))
                .then(Commands.argument("world", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            Helpers.getAllWorldsSuggestion(builder);
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    String worldName = StringArgumentType.getString(ctx, "world");
                                    World world = Bukkit.getWorld(worldName);
                                    if (world != null) {
                                        List<Speaker> speakers = SpeakerManager.inst().getSpeakers(world.getUID());
                                        for (Speaker speaker : speakers) {
                                            builder.suggest(speaker.getName());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    String worldName = StringArgumentType.getString(ctx, "world");
                                    String name = StringArgumentType.getString(ctx, "name");

                                    World world = Bukkit.getWorld(worldName);
                                    if (world == null) {
                                        sender.sendMessage("§cWorld not found: " + worldName);
                                        return 0;
                                    }

                                    boolean removed = SpeakerManager.inst().removeSpeaker(world.getUID(), name);
                                    if (removed) {
                                        sender.sendMessage("§aRemoved speaker: §f" + name + " §afrom §f" + worldName);
                                        return 1;
                                    } else {
                                        sender.sendMessage("§cSpeaker not found: " + name);
                                        return 0;
                                    }
                                })
                        )
                );
    }

    private ArgumentBuilder<CommandSourceStack, ?> buildListCommand() {
        return Commands.literal("list")
                .requires(cs -> cs.getSender().hasPermission("svcintercom.speaker.list"))
                .then(Commands.argument("world", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            Helpers.getAllWorldsSuggestion(builder);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            String worldName = StringArgumentType.getString(ctx, "world");

                            World world = Bukkit.getWorld(worldName);
                            if (world == null) {
                                sender.sendMessage("§cWorld not found: " + worldName);
                                return 0;
                            }

                            List<Speaker> speakers = SpeakerManager.inst().getSpeakers(world.getUID());
                            if (speakers.isEmpty()) {
                                sender.sendMessage("§cNo speakers in world: " + worldName);
                                return 0;
                            }

                            sender.sendMessage("§aSpeakers in §f" + worldName + "§a:");
                            for (Speaker speaker : speakers) {
                                sender.sendMessage("  §f- " + speaker.toString());
                            }
                            return 1;
                        })
                )
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("§cYou must specify a world or be a player");
                        return 0;
                    }

                    World world = player.getWorld();
                    List<Speaker> speakers = SpeakerManager.inst().getSpeakers(world.getUID());
                    if (speakers.isEmpty()) {
                        sender.sendMessage("§cNo speakers in your current world");
                        return 0;
                    }

                    sender.sendMessage("§aSpeakers in §f" + world.getName() + "§a:");
                    for (Speaker speaker : speakers) {
                        sender.sendMessage("  §f- " + speaker.toString());
                    }
                    return 1;
                });
    }
}
