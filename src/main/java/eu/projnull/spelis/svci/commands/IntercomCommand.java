package eu.projnull.spelis.svci.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.projnull.spelis.svci.commands.handlers.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IntercomCommand {
    private final List<Handler> handlers = new ArrayList<>();

    public IntercomCommand() {
        registerHandler(new InfoCommand());
        registerHandler(new StopCommand());
        registerHandler(new LiveCommand());
        registerHandler(new FileCommand());
        registerHandler(new AboutCommand());
    }

    public void registerHandler(Handler handler) {
        handlers.add(handler);
    }

    public void register(ReloadableRegistrarEvent<@NotNull Commands> commands) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("intercom").requires(cs->cs.getSender().hasPermission("svcintercom.broadcast"));

        for (Handler handler : handlers) {
            root.then(handler.buildCommand());
        }

        commands.registrar().register(root.build());
    }
}
