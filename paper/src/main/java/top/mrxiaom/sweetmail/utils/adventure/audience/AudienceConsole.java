package top.mrxiaom.sweetmail.utils.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.utils.MiniMessageConvert;
import top.mrxiaom.sweetmail.utils.adventure.serializer.BungeeComponentSerializer;
import top.mrxiaom.sweetmail.utils.adventure.serializer.legacy.LegacyComponentSerializer;

import java.util.logging.Level;

public class AudienceConsole implements Audience {
    private static boolean SUPPORT_BUNGEE = true;
    public static final Audience INSTANCE = new AudienceConsole();
    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private AudienceConsole() {}

    @Override
    public void sendMessage(@NotNull Component message) {
        if (SUPPORT_BUNGEE) {
            try {
                BaseComponent components = BungeeComponentSerializer.serialize(message);
                console.spigot().sendMessage(components);
                return;
            } catch (LinkageError e) {
                MiniMessageConvert.plugin().getLogger().log(Level.WARNING,
                        "尝试通过 BungeeCord Chat Component 发送消息时出现兼容性问题", e);
                SUPPORT_BUNGEE = false;
            }
        }
        console.sendMessage(legacy.serialize(message));
    }
}
