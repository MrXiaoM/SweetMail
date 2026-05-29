package top.mrxiaom.sweetmail.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;
import top.mrxiaom.sweetmail.api.IAdventureHandler;
import top.mrxiaom.sweetmail.utils.adventure.DefaultAdventureHandler;

public class MiniMessageConvert {
    private static IAdventureHandler handler;
    private static JavaPlugin plugin;
    @Deprecated
    public static void init() {}
    public static void init(JavaPlugin plugin, IAdventureHandler handler) {
        MiniMessageConvert.plugin = plugin;
        MiniMessageConvert.handler = handler;
    }

    public static JavaPlugin plugin() {
        return plugin;
    }

    public static MiniMessage miniMessage() {
        return handler.miniMessage();
    }

    public static String miniMessage(Component component) {
        return handler.miniMessage(component);
    }

    public static Component miniMessage(String text) {
        return handler.miniMessage(text);
    }

    public static String miniMessageToLegacy(String text) {
        if (text == null) return "";
        Component component = handler.miniMessage(text);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static String legacyToMiniMessage(String legacy) {
        return handler.legacyToMiniMessage(legacy);
    }

    public static boolean isColorCode(char c) {
        return DefaultAdventureHandler.isColorCode(c);
    }

}
