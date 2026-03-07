package eu.projnull.spelis.svci.commands.handlers;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import eu.projnull.spelis.svci.commands.Handler;
import eu.projnull.spelis.svci.commands.Helpers;
import eu.projnull.spelis.svci.voice.BroadcasterState;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class InfoCommand implements Handler {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
        return Commands.literal("info").requires(cs->cs.getSender().hasPermission("svcintercom.broadcast.info")).then(Commands.argument("world", StringArgumentType.word()).suggests((ctx, builder) -> {
            Helpers.getActiveWorldsSuggestion(builder);
            return builder.buildFuture();
        }).executes(ctx -> {
            CommandSourceStack source = ctx.getSource();
            CommandSender sender = source.getSender();
            String worldName = ctx.getArgument("world", String.class);
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage("§cWorld not found.");
                return 0;
            }

            BroadcasterState.Broadcaster b = BroadcasterState.inst().getBroadcast(world.getUID());
            if (b == null) {
                sender.sendMessage(String.format("§cNo active broadcast in %s.", worldName));
                return 0;
            }

            String type = b.getType().name();
            String owner = b.getType() == BroadcasterState.Broadcaster.BroadcastType.LIVE ? Bukkit.getOfflinePlayer(b.getPlayerId()).getName() : b.getFileName();
            long secondsLeft = (b.getEndTimeMillis() - System.currentTimeMillis()) / 1000;

            sender.sendMessage("§eBroadcast info:");
            sender.sendMessage(" §7Type: §f" + type);
            sender.sendMessage(" §7Owner/File: §f" + owner);
            sender.sendMessage(" §7Time remaining: §f" + secondsLeft + "s");

            return 1;
        }).build());
    }
}
