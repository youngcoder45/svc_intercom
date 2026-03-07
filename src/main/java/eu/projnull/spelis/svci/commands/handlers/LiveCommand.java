package eu.projnull.spelis.svci.commands.handlers;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import eu.projnull.spelis.svci.Intercom;
import eu.projnull.spelis.svci.commands.Handler;
import eu.projnull.spelis.svci.commands.Helpers;
import eu.projnull.spelis.svci.voice.BroadcasterState;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LiveCommand implements Handler {

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
        return Commands.literal("live").requires(cs->cs.getSender().hasPermission("svcintercom.broadcast.start")).then(Commands.argument("player", ArgumentTypes.player()).then(Commands.argument("world", StringArgumentType.word()).suggests((ctx, builder) -> {
            Helpers.getAllWorldsSuggestion(builder);
            return builder.buildFuture();
        }).then(Commands.argument("duration", IntegerArgumentType.integer(1, 300)).executes(ctx -> {
            CommandSourceStack source = ctx.getSource();
            CommandSender sender = source.getSender();
            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
            World world = Bukkit.getWorld(StringArgumentType.getString(ctx, "world"));
            int seconds = ctx.getArgument("duration", int.class);

            if (world == null) {
                sender.sendMessage("§cWorld not found.");
                return 0;
            }
            if (seconds <= 0 || seconds > 300) {
                sender.sendMessage("§cInvalid duration (must be 1-300 seconds)");
                return 0;
            }

            if (BroadcasterState.inst().isBroadcastActive(world.getUID())) {
                sender.sendMessage("§cA broadcast is already active in this world.");
                return 0;
            }

            BroadcasterState.Broadcaster broadcaster = new BroadcasterState.Broadcaster(player.getUniqueId(), world.getUID(), BroadcasterState.Broadcaster.BroadcastType.LIVE, seconds * 1000L, null);

            BroadcasterState.inst().startBroadcast(broadcaster);
            sender.sendMessage("§aLive broadcast started for " + player.getName() + " in world " + world.getName());

            // Schedule auto-stop
            Bukkit.getScheduler().runTaskLaterAsynchronously(Intercom.getPlugin(Intercom.class), () -> BroadcasterState.inst().stopBroadcastWithMessage(world.getUID(), "§aThe broadcast has expired"), seconds * 20L);


            return 1;
        }))));
    }
}
