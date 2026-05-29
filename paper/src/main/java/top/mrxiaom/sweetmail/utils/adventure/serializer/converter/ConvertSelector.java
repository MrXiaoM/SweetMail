package top.mrxiaom.sweetmail.utils.adventure.serializer.converter;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.SelectorComponent;
import top.mrxiaom.sweetmail.utils.adventure.serializer.BungeeComponentSerializer;

public class ConvertSelector {
    public static BaseComponent convert(Component input) {
        if (input instanceof net.kyori.adventure.text.SelectorComponent) {
            String selector = ((net.kyori.adventure.text.SelectorComponent) input).pattern();
            Component separator = ((net.kyori.adventure.text.SelectorComponent) input).separator();
            if (separator != null) {
                return new SelectorComponent(selector, BungeeComponentSerializer.serialize(separator));
            } else {
                return new SelectorComponent(selector);
            }
        }
        return null;
    }
}
