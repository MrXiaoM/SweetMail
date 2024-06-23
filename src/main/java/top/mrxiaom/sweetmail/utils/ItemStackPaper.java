package top.mrxiaom.sweetmail.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static top.mrxiaom.sweetmail.utils.ColorHelper.parseColor;
import static top.mrxiaom.sweetmail.utils.Util.split;

public class ItemStackPaper {
    private static final Pattern translatePattern = Pattern.compile("<translate:(.*?)>");
    
    static Component parse(String s) {
        TextComponent.Builder builder = Component.text();
        split(translatePattern, parseColor(s), regexResult -> {
            if (!regexResult.isMatched) {
                builder.append(LegacyComponentSerializer.legacySection().deserialize(regexResult.text));
            } else {
                TranslatableComponent translatable = Component.translatable(regexResult.result.group(1));
                builder.append(translatable);
            }
        });
        return builder.build().asComponent();
    }
    
    static List<Component> parse(List<String> list) {
        List<Component> components = new ArrayList<>();
        for (String s : list) {
            components.add(parse(s));
        }
        return components;
    }
    
    public static void setItemDisplayName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(parse(name));
        item.setItemMeta(meta);
    }
    
    public static void setItemLore(ItemStack item, List<String> lore) {
        List<Component> list = parse(lore);
        ItemMeta meta = item.getItemMeta();
        meta.lore(list);
        item.setItemMeta(meta);
    }
}
