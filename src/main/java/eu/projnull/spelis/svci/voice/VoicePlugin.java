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

        for (org.bukkit.entity.Player online : player.getWorld().getPlayers()) {
            if (online.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            VoicechatConnection connection = api.getConnectionOf(online.getUniqueId());

            if (connection == null) continue;

            api.sendStaticSoundPacketTo(connection, event.getPacket().toStaticSoundPacket());
        }
    }
}
