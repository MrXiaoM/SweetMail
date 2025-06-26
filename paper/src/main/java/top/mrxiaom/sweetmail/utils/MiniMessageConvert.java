package top.mrxiaom.sweetmail.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.lang.reflect.Field;
import java.util.List;

public class MiniMessageConvert {
    private static MiniMessage miniMessage;
    public static void init() {
        miniMessage = MiniMessage.builder()
                .editTags(it -> remove(it, "pride"))
                .postProcessor(it -> it.decoration(TextDecoration.ITALIC, false))
                .build();
    }

    @SuppressWarnings({"unchecked", "CallToPrintStackTrace"})
    private static void remove(TagResolver.Builder builder, String... tags) {
        Class<?> type = builder.getClass();
        try {
            Field field = type.getDeclaredField("resolvers");
            field.setAccessible(true);
            List<TagResolver> list = (List<TagResolver>) field.get(builder);
            list.removeIf(it -> {
                for (String tag : tags) {
                    if (it.has(tag)) return true;
                }
                return false;
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static MiniMessage miniMessage() {
        return miniMessage;
    }

    public static String miniMessage(Component component) {
        return component == null
                ? ""
                : miniMessage.serialize(component);
    }

    public static Component miniMessage(String text) {
        if (text == null) {
            return Component.empty();
        }
        return miniMessage.deserialize(legacyToMiniMessage(text));
    }

    public static String miniMessageToLegacy(String text) {
        if (text == null) return "";
        Component component = miniMessage.deserialize(legacyToMiniMessage(text));
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static String legacyToMiniMessage(String legacy) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = legacy.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isColorCode(chars[i])) {
                stringBuilder.append(chars[i]);
                continue;
            }
            if (i + 1 >= chars.length) {
                stringBuilder.append(chars[i]);
                continue;
            }
            switch (Character.toLowerCase(chars[i+1])) {
                case '0': stringBuilder.append("<black>"); break;
                case '1': stringBuilder.append("<dark_blue>"); break;
                case '2': stringBuilder.append("<dark_green>"); break;
                case '3': stringBuilder.append("<dark_aqua>"); break;
                case '4': stringBuilder.append("<dark_red>"); break;
                case '5': stringBuilder.append("<dark_purple>"); break;
                case '6': stringBuilder.append("<gold>"); break;
                case '7': stringBuilder.append("<gray>"); break;
                case '8': stringBuilder.append("<dark_gray>"); break;
                case '9': stringBuilder.append("<blue>"); break;
                case 'a': stringBuilder.append("<green>"); break;
                case 'b': stringBuilder.append("<aqua>"); break;
                case 'c': stringBuilder.append("<red>"); break;
                case 'd': stringBuilder.append("<light_purple>"); break;
                case 'e': stringBuilder.append("<yellow>"); break;
                case 'f': stringBuilder.append("<white>"); break;
                case 'r': stringBuilder.append("<reset><!i>"); break;
                case 'l': stringBuilder.append("<b>"); break;
                case 'm': stringBuilder.append("<st>"); break;
                case 'o': stringBuilder.append("<i>"); break;
                case 'n': stringBuilder.append("<u>"); break;
                case 'k': stringBuilder.append("<obf>"); break;
                case 'x': {
                    if (i + 13 >= chars.length
                            || !isColorCode(chars[i+2])
                            || !isColorCode(chars[i+4])
                            || !isColorCode(chars[i+6])
                            || !isColorCode(chars[i+8])
                            || !isColorCode(chars[i+10])
                            || !isColorCode(chars[i+12])) {
                        stringBuilder.append(chars[i]);
                        continue;
                    }
                    stringBuilder
                            .append("<#")
                            .append(chars[i+3])
                            .append(chars[i+5])
                            .append(chars[i+7])
                            .append(chars[i+9])
                            .append(chars[i+11])
                            .append(chars[i+13])
                            .append(">");
                    i += 12;
                    break;
                }
                default: {
                    stringBuilder.append(chars[i]);
                    continue;
                }
            }
            i++;
        }
        return stringBuilder.toString();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isColorCode(char c) {
        return c == '§' || c == '&';
    }

}
