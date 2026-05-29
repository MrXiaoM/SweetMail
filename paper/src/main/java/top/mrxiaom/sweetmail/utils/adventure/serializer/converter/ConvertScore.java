package top.mrxiaom.sweetmail.utils.adventure.serializer.converter;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ScoreComponent;

public class ConvertScore {
    @SuppressWarnings({"deprecation"})
    public static BaseComponent convert(Component input) {
        if (input instanceof net.kyori.adventure.text.ScoreComponent) {
            String name = ((net.kyori.adventure.text.ScoreComponent) input).name();
            String objective = ((net.kyori.adventure.text.ScoreComponent) input).objective();
            String value = ((net.kyori.adventure.text.ScoreComponent) input).value();
            if (value != null) {
                return new ScoreComponent(name, objective, value);
            } else {
                return new ScoreComponent(name, objective);
            }
        }
        return null;
    }
}
