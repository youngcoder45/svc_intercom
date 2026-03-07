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

public class StopCommand implements Handler {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
        return Commands.literal("stop").requires(cs->cs.getSender().hasPermission("svcintercom.broadcast.stop"))
                .then(Commands.argument("world", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            Helpers.getActiveWorldsSuggestion(builder);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> { // <-- attach here
                            CommandSourceStack source = ctx.getSource();
                            CommandSender sender = source.getSender();
                            String worldName = StringArgumentType.getString(ctx, "world");

                            World world = Bukkit.getWorld(worldName);
                            if (world == null) {
                                sender.sendMessage("§cWorld not found.");
                                return 0;
                            }

                            if (!BroadcasterState.inst().isBroadcastActive(world.getUID())) {
                                sender.sendMessage("§cNo active broadcast in this world.");
                                return 0;
                            }

                            BroadcasterState.inst().stopBroadcastWithMessage(world.getUID(), "§aThe broadcast has been stopped.");
                            sender.sendMessage("§aBroadcast stopped in " + world.getName());
                            return 1;
                        })
                );
    }
}
