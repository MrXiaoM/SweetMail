package top.mrxiaom.sweetmail.utils.adventure.serializer.converter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;

public class ConvertTranslatable {
    public static BaseComponent convert(Component input) {
        if (input instanceof net.kyori.adventure.text.TranslatableComponent) {
            String key = ((net.kyori.adventure.text.TranslatableComponent) input).key();
            List<TranslationArgument> arguments = ((net.kyori.adventure.text.TranslatableComponent) input).arguments();
            List<Object> args = new ArrayList<>();
            for (TranslationArgument argument : arguments) {
                args.add(argument.value());
            }
            TranslatableComponent component = new TranslatableComponent(key, args.toArray());
            try {
                String fallback = ((net.kyori.adventure.text.TranslatableComponent) input).fallback();
                if (fallback != null) {
                    component.setFallback(fallback);
                }
            } catch (LinkageError ignored) {
            }
            return component;
        }
        return null;
    }
}
