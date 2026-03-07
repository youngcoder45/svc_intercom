package eu.projnull.spelis.svci;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import eu.projnull.spelis.svci.commands.IntercomCommand;
import eu.projnull.spelis.svci.misc.BroadcastHudTask;
import eu.projnull.spelis.svci.voice.VoicePlugin;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public final class Intercom extends JavaPlugin {

    public static final String PLUGIN_ID = "SVCIntercom";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_ID);

    private final BroadcastHudTask hudTask = new BroadcastHudTask();

    @Nullable
    private VoicePlugin voicechatPlugin;

    @Override
    public void onEnable() {
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            voicechatPlugin = new VoicePlugin();
            service.registerPlugin(voicechatPlugin);
            LOGGER.info("Successfully registered intercom plugin");
        } else {
            LOGGER.info("Failed to register intercom plugin");
        }

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> new IntercomCommand().register(commands));

        hudTask.start(this);
    }

    @Override
    public void onDisable() {
        if (voicechatPlugin != null) {
            getServer().getServicesManager().unregister(voicechatPlugin);
            LOGGER.info("Successfully unregistered intercom plugin");
        }
        hudTask.stop();
    }

    @Nullable
    public VoicePlugin getVoicechatPlugin() {
        return voicechatPlugin;
    }
}
