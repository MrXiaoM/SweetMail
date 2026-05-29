package top.mrxiaom.sweetmail.utils.adventure.serializer.converter;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.KeybindComponent;

public class ConvertKeybind {
    public static BaseComponent convert(Component input) {
        if (input instanceof net.kyori.adventure.text.KeybindComponent) {
            String keybind = ((net.kyori.adventure.text.KeybindComponent) input).keybind();
            return new KeybindComponent(keybind);
        }
        return null;
    }
}
