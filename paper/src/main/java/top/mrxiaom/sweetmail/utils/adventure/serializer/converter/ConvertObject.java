package top.mrxiaom.sweetmail.utils.adventure.serializer.converter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ObjectComponent;

import java.util.List;
import java.util.UUID;

public class ConvertObject {
    public static BaseComponent convert(Component input) {
        if (input instanceof net.kyori.adventure.text.ObjectComponent) {
            ObjectContents contents = ((net.kyori.adventure.text.ObjectComponent) input).contents();
            if (contents instanceof PlayerHeadObjectContents) {
                PlayerHeadObjectContents head = (PlayerHeadObjectContents) contents;
                String name = head.name();
                UUID uuid = head.id();
                boolean hat = head.hat();
                List<PlayerHeadObjectContents.ProfileProperty> props = head.profileProperties();
                net.md_5.bungee.api.chat.player.Property[] properties = new net.md_5.bungee.api.chat.player.Property[props.size()];
                for (int i = 0; i < props.size(); i++) {
                    String propName = props.get(i).name();
                    String propValue = props.get(i).value();
                    String propSign = props.get(i).signature();
                    if (propSign != null) {
                        properties[i] = new net.md_5.bungee.api.chat.player.Property(propName, propValue, propSign);
                    } else {
                        properties[i] = new net.md_5.bungee.api.chat.player.Property(propName, propValue);
                    }
                }
                net.md_5.bungee.api.chat.player.Profile profile = new net.md_5.bungee.api.chat.player.Profile(properties);
                profile.setName(name);
                profile.setUuid(uuid);
                return new ObjectComponent(new net.md_5.bungee.api.chat.objects.PlayerObject(profile, hat));
            }
            if (contents instanceof SpriteObjectContents) {
                SpriteObjectContents props = (SpriteObjectContents) contents;
                String atlas = props.atlas().asString();
                String sprite = props.sprite().asString();
                return new ObjectComponent(new net.md_5.bungee.api.chat.objects.SpriteObject(atlas, sprite));
            }
        }
        return null;
    }
}
