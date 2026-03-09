package eu.projnull.spelis.svci.commands.handlers;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import eu.projnull.spelis.svci.Intercom;
import eu.projnull.spelis.svci.commands.Handler;
import eu.projnull.spelis.svci.commands.Helpers;
import eu.projnull.spelis.svci.misc.OggDecoder;
import eu.projnull.spelis.svci.voice.BroadcasterState;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class FileCommand implements Handler {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
        return Commands.literal("file").requires(cs->cs.getSender().hasPermission("svcintercom.broadcast.start")).then(Commands.argument("filename", StringArgumentType.string()).suggests((ctx, builder) -> {
            Helpers.getAllSoundsSuggestion(builder);
            return builder.buildFuture();
        }).then(Commands.argument("world", StringArgumentType.word()).suggests((ctx, builder) -> {
            Helpers.getAllWorldsSuggestion(builder);
            return builder.buildFuture();
        }).executes(ctx -> {
            CommandSourceStack source = ctx.getSource();
            CommandSender sender = source.getSender();
            String filename = StringArgumentType.getString(ctx, "filename");
            World world = Bukkit.getWorld(StringArgumentType.getString(ctx, "world"));

            if (world == null) {
                sender.sendMessage("§cWorld not found.");
                return 0;
            }

            if (BroadcasterState.inst().isBroadcastActive(world.getUID())) {
                sender.sendMessage("§cA broadcast is already active in this world.");
                return 0;
            }

            File soundFile = new File(Intercom.getPlugin(Intercom.class).getDataFolder(), "sounds/" + filename);
            if (!soundFile.exists()) {
                Intercom.LOGGER.warn("File not found: {}", soundFile.getAbsoluteFile());
                sender.sendMessage("§cFile not found.");
                return 0;
            }

            Bukkit.getScheduler().runTaskAsynchronously(Intercom.getPlugin(Intercom.class), () -> {
                short[] pcm;
                try {
                    pcm = OggDecoder.decode(soundFile);
                } catch (Exception e) {
                    sender.sendMessage("§cFailed to decode file.");
                    Intercom.LOGGER.warn("File not found: {}", e.getMessage());
                    return;
                }

                long durationMillis = pcm.length / 48L;

                Bukkit.getScheduler().runTask(Intercom.getPlugin(Intercom.class), () -> {
                    if (BroadcasterState.inst().isBroadcastActive(world.getUID())) {
                        sender.sendMessage("§cA broadcast is already active!");
                        return;
                    }

                    VoicechatServerApi api = Objects.requireNonNull(Intercom.getPlugin(Intercom.class).getVoicechatPlugin()).getVoicechatServerApi();
                    if (api == null) {
                        sender.sendMessage("§cSimple Voice Chat API not available.");
                        return;
                    }

                    de.maxhenkel.voicechat.api.ServerLevel level = api.fromServerLevel(world);

                    BroadcasterState.Broadcaster broadcaster = new BroadcasterState.Broadcaster(null, world.getUID(), BroadcasterState.Broadcaster.BroadcastType.FILE, durationMillis, filename);
                    BroadcasterState.inst().startBroadcast(broadcaster);

                    // Get all speakers in this world
                    java.util.List<eu.projnull.spelis.svci.voice.Speaker> speakers = eu.projnull.spelis.svci.voice.SpeakerManager.inst().getSpeakers(world.getUID());
                    
                    if (speakers.isEmpty()) {
                        // Fallback to old behavior if no speakers are defined
                        for (Player p : world.getPlayers()) {
                            VoicechatConnection connection = api.getConnectionOf(p.getUniqueId());
                            if (connection == null) continue;

                            StaticAudioChannel channel = api.createStaticAudioChannel(UUID.randomUUID(), level, connection);
                            if (channel == null) continue;

                            AudioPlayer audioPlayer = api.createAudioPlayer(channel, api.createEncoder(), pcm);
                            audioPlayer.startPlaying();
                        }
                        sender.sendMessage("§aFile broadcast started: §f" + filename + " §ain §f" + world.getName() + " §7(no speakers, using global audio)");
                    } else {
                        // Use positional audio from speakers
                        for (eu.projnull.spelis.svci.voice.Speaker speaker : speakers) {
                            org.bukkit.Location speakerLoc = speaker.getLocation();
                            if (speakerLoc == null) continue;

                            de.maxhenkel.voicechat.api.Position position = api.createPosition(
                                    speakerLoc.getX(),
                                    speakerLoc.getY(),
                                    speakerLoc.getZ()
                            );

                            de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel channel = 
                                    api.createLocationalAudioChannel(UUID.randomUUID(), level, position);
                            
                            if (channel == null) continue;

                            channel.setDistance((float) speaker.getRange());

                            AudioPlayer audioPlayer = api.createAudioPlayer(channel, api.createEncoder(), pcm);
                            audioPlayer.startPlaying();
                        }
                        sender.sendMessage("§aFile broadcast started: §f" + filename + " §ain §f" + world.getName() + " §7(" + speakers.size() + " speakers)");
                    }

                    Bukkit.getScheduler().runTaskLaterAsynchronously(Intercom.getPlugin(Intercom.class), () -> BroadcasterState.inst().stopBroadcastWithMessage(world.getUID(), "§aThe broadcast has ended."), durationMillis / 50L);
                });
            });

            return 1;
        })));
    }
}
