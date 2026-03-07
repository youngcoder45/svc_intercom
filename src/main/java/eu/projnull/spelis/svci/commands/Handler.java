package eu.projnull.spelis.svci.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface Handler {
    ArgumentBuilder<CommandSourceStack, ?> buildCommand();
}
