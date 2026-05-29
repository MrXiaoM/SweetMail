package top.mrxiaom.sweetmail.utils.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.utils.MiniMessageConvert;
import top.mrxiaom.sweetmail.utils.adventure.serializer.BungeeComponentSerializer;
import top.mrxiaom.sweetmail.utils.adventure.serializer.legacy.LegacyComponentSerializer;

import java.util.logging.Level;

public class AudiencePlayer implements Audience {
    private static boolean SUPPORT_BUNGEE = true;
    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    private final Player player;
    public AudiencePlayer(Player player) {
        this.player = player;
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        if (SUPPORT_BUNGEE) {
            try {
                BaseComponent components = BungeeComponentSerializer.serialize(message);
                player.spigot().sendMessage(components);
                return;
            } catch (LinkageError e) {
                MiniMessageConvert.plugin().getLogger().log(Level.WARNING,
                        "尝试通过 BungeeCord Chat Component 发送消息时出现兼容性问题", e);
                SUPPORT_BUNGEE = false;
            }
        }
        player.sendMessage(legacy.serialize(message));
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        if (SUPPORT_BUNGEE) {
            try {
                BaseComponent components = BungeeComponentSerializer.serialize(message);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
            } catch (LinkageError e) {
                MiniMessageConvert.plugin().getLogger().log(Level.WARNING,
                        "尝试通过 BungeeCord Chat Component 发送消息时出现兼容性问题", e);
                SUPPORT_BUNGEE = false;
            }
        }
        try {
            BaseComponent[] component = TextComponent.fromLegacyText(legacy.serialize(message));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
        } catch (LinkageError ignored) {
            player.sendMessage(legacy.serialize(message));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void showTitle(@NotNull Title title) {
        String strTitle = legacy.serialize(title.title());
        String strSubtitle = legacy.serialize(title.subtitle());
        Title.Times times = title.times();
        if (times != null) {
            int fadeIn = (int) (times.fadeIn().toMillis() / 50.0);
            int stay = (int) (times.stay().toMillis() / 50.0);
            int fadeOut = (int) (times.fadeOut().toMillis() / 50.0);
            player.sendTitle(strTitle, strSubtitle, fadeIn, stay, fadeOut);
        } else {
            player.sendTitle(strTitle, strSubtitle);
        }
    }

    @Override
    public void clearTitle() {
        player.resetTitle();
    }

    @Override
    public void resetTitle() {
        player.resetTitle();
    }

    @Override
    public void playSound(@NotNull Sound sound, @NotNull Sound.Emitter emitter) {
        playSound(sound);
    }

    @Override
    public void playSound(@NotNull Sound sound) {
        Location loc = player.getLocation();
        player.playSound(loc, sound.name().asString(), sound.volume(), sound.pitch());
    }

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        Location loc = new Location(player.getWorld(), x, y, z);
        player.playSound(loc, sound.name().asString(), sound.volume(), sound.pitch());
    }

    @Override
    public void stopSound(@NotNull Sound sound) {
        player.stopSound(sound.name().asString());
    }

    @Override
    public void stopSound(@NotNull SoundStop stop) {
        SoundCategory category = from(stop.source());
        if (category != null) {
            Key sound = stop.sound();
            if (sound != null) {
                player.stopSound(sound.asString(), category);
            } else {
                try {
                    player.stopSound(category);
                } catch (LinkageError ignored) {
                }
            }
        } else {
            Key sound = stop.sound();
            if (sound != null) {
                player.stopSound(sound.asString());
            } else {
                try {
                    player.stopAllSounds();
                } catch (LinkageError ignored) {
                }
            }
        }
    }

    private static SoundCategory from(Sound.Source source) {
        if (source == null) return null;
        String name = source.name().toUpperCase();
        for (SoundCategory category : SoundCategory.values()) {
            if (category.name().toUpperCase().startsWith(name)) {
                return category;
            }
        }
        return null;
    }
}
