package eu.projnull.spelis.svci.commands.handlers;

import com.mojang.brigadier.builder.ArgumentBuilder;
import eu.projnull.spelis.svci.Intercom;
import eu.projnull.spelis.svci.commands.Handler;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class AboutCommand implements Handler {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
        return Commands.literal("about").executes(ctx -> { // <-- attach here
            CommandSourceStack source = ctx.getSource();
            CommandSender sender = source.getSender();

            Plugin plugin = JavaPlugin.getPlugin(Intercom.class);
            PluginDescriptionFile desc = plugin.getDescription();

            String pluginName = "§e" + desc.getName();
            String version = "§a" + desc.getVersion();

            String authors = String.join(", ", desc.getAuthors());
            String authorText = "§7By §b" + authors;

            sender.sendMessage(pluginName + " §fVersion §f" + version + "\n" + authorText);

            return 1;
        });
    }
}
