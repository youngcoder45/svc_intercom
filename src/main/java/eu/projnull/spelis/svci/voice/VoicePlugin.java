package eu.projnull.spelis.svci.voice;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import eu.projnull.spelis.svci.Intercom;

import java.util.UUID;


public class VoicePlugin implements VoicechatPlugin {
    private VoicechatServerApi voicechatServerApi;

    public VoicechatServerApi getVoicechatServerApi() {
        return voicechatServerApi;
    }

    @Override
    public String getPluginId() {
        return Intercom.PLUGIN_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        Intercom.LOGGER.info("Initialize Voice Chat plugin.");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicPacket);
        registration.registerEvent(VoicechatServerStartedEvent.class, e -> this.voicechatServerApi = e.getVoicechat());
    }

    private void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) return;

        if (!(senderConnection.getPlayer().getPlayer() instanceof org.bukkit.entity.Player player)) {
            return;
        }

        if (event.getPacket().getOpusEncodedData().length == 0) return;

        UUID worldId = player.getWorld().getUID();

        BroadcasterState.Broadcaster broadcaster = BroadcasterState.inst().getBroadcast(worldId);

        if (broadcaster == null) return;

        if (broadcaster.getType() != BroadcasterState.Broadcaster.BroadcastType.LIVE) return;

        if (!player.getUniqueId().equals(broadcaster.getPlayerId())) return;

        event.cancel();

        VoicechatServerApi api = event.getVoicechat();

        // Get all speakers in this world
        java.util.List<Speaker> speakers = SpeakerManager.inst().getSpeakers(worldId);
        
        if (speakers.isEmpty()) {
            // Fallback to old behavior if no speakers are defined
            for (org.bukkit.entity.Player online : player.getWorld().getPlayers()) {
                if (online.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }

                VoicechatConnection connection = api.getConnectionOf(online.getUniqueId());

                if (connection == null) continue;

                api.sendStaticSoundPacketTo(connection, event.getPacket().toStaticSoundPacket());
            }
            return;
        }

        // Use positional audio from speakers
        for (org.bukkit.entity.Player online : player.getWorld().getPlayers()) {
            if (online.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            VoicechatConnection connection = api.getConnectionOf(online.getUniqueId());
            if (connection == null) continue;

            // Find nearest speaker to the player
            Speaker nearestSpeaker = null;
            double nearestDistance = Double.MAX_VALUE;

            for (Speaker speaker : speakers) {
                if (speaker.isInRange(online.getLocation())) {
                    org.bukkit.Location speakerLoc = speaker.getLocation();
                    if (speakerLoc == null) continue;
                    
                    double distance = online.getLocation().distance(speakerLoc);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestSpeaker = speaker;
                    }
                }
            }

            // Only send audio if player is in range of at least one speaker
            if (nearestSpeaker != null) {
                org.bukkit.Location speakerLoc = nearestSpeaker.getLocation();
                
                // Create a locational sound packet from the speaker position
                de.maxhenkel.voicechat.api.Position position = api.createPosition(
                        speakerLoc.getX(),
                        speakerLoc.getY(),
                        speakerLoc.getZ()
                );

                api.sendLocationalSoundPacketTo(
                        connection,
                        event.getPacket().toLocationalSoundPacket(position)
                );
            }
        }
    }
}
