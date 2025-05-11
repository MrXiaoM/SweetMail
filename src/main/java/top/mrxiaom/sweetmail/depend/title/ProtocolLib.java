package top.mrxiaom.sweetmail.depend.title;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;

public class ProtocolLib extends AbstractPluginHolder {
    ProtocolManager protocol;
    GsonComponentSerializer gson;
    public ProtocolLib(SweetMail plugin) {
        super(plugin);
        register();
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R1)) {
            gson = GsonComponentSerializer.gson();
        } else {
            gson = GsonComponentSerializer.colorDownsamplingGson();
        }
        protocol = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        protocol.removePacketListeners(plugin);
        if (config.getBoolean("dependencies.ProtocolLib", true)) {
            protocol.addPacketListener(new PacketAdapter(
                    new PacketAdapter.AdapterParameteters()
                            .plugin(plugin).serverSide().optionAsync()
                            .types(PacketType.Play.Server.OPEN_WINDOW)
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    ProtocolLib.this.onPacketSending(event);
                }
            });
        }
    }

    private void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();
        IGui gui = plugin.getGuiManager().getOpeningGui(player);
        if (gui != null) {
            Component title = gui.getTitle();
            if (title == null) return;
            try {
                String json = gson.serialize(title);
                StructureModifier<WrappedChatComponent> modifier = packet.getChatComponents();
                WrappedChatComponent component = modifier.readSafely(0);
                component.setJson(json);
                modifier.writeSafely(0, component);
            } catch (Throwable t) {
                warn("修改界面标题时出现异常，请向开发者报告该问题。若频繁出现此报错，请到 config.yml 关闭 dependencies.ProtocolLib", t);
            }
        }
    }

    @Override
    public void onDisable() {
        protocol.removePacketListeners(plugin);
    }
}
