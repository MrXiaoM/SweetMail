package top.mrxiaom.sweetmail.utils.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Pattern;

import static top.mrxiaom.sweetmail.utils.MiniMessageConvert.legacyToMiniMessage;
import static top.mrxiaom.sweetmail.utils.MiniMessageConvert.miniMessageToLegacy;
import static top.mrxiaom.sweetmail.utils.StringHelper.split;

public class PaperInventoryFactory implements InventoryFactory {
    private final MiniMessage miniMessage;
    private String offsetFont = "mrxiaom:sweetmail";
    private final Pattern offsetPattern = Pattern.compile("<(offset|o|!offset|!o):(-?[0-9]+)>");
    public PaperInventoryFactory() {
        miniMessage = MiniMessage.builder()
                .editTags(it -> remove(it, "pride"))
                .postProcessor(it -> it.decoration(TextDecoration.ITALIC, false))
                .build();
        Offset.init();
    }

    // 由于 shadow relocation，只能复制一份，而不能与 MiniMessageConvert 共用一个方法
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

    public Component miniMessage(String text) {
        if (text == null) {
            return Component.empty();
        }
        return miniMessage.deserialize(legacyToMiniMessage(text));
    }

    @Override
    @SuppressWarnings({"deprecation"})
    public Inventory create(InventoryHolder owner, int size, String title) {
        try {
            StringBuilder sb = new StringBuilder();
            split(offsetPattern, title, result -> {
                if (result.isMatched) {
                    boolean withoutFont = result.result.group(1).startsWith("!");
                    int offset = Integer.parseInt(result.result.group(2));
                    String str = Offset.get(offset);
                    if (withoutFont) {
                        sb.append(str);
                    } else {
                        sb.append("<font:").append(offsetFont).append(">").append(str).append("</font>");
                    }
                } else {
                    sb.append(result.text);
                }
            });
            Component parsed = miniMessage(sb.toString());
            return Bukkit.createInventory(owner, size, parsed);
        } catch (LinkageError e) { // 1.16 以下的旧版本 Paper 服务端不支持这个接口
            String parsed = miniMessageToLegacy(title);
            return Bukkit.createInventory(owner, size, parsed);
        }
    }

    @Override
    public void setOffsetFont(String font) {
        this.offsetFont = font;
    }

    public static boolean test() {
        try {
            Bukkit.class.getDeclaredMethod("createInventory", InventoryHolder.class, InventoryType.class, Component.class);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
