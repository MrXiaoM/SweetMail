package top.mrxiaom.sweetmail.utils.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.Map;

public interface ItemProvider {
    ItemStack get(@Nullable Player player, String argument);

    static void loadBuiltIn(Map<String, ItemProvider> map) {
        map.put("itemsadder-", new ItemsAdderProvider());
        map.put("mythic-", new MythicProvider());
        map.put("head-base64-", new HeadBase64Provider());
        try {
            String major = System.getProperty("java.version").split("\\.")[0];
            int javaVersion = Util.parseInt(major).orElse(0);
            if (javaVersion >= 21) {
                map.put("craftengine-", new CraftEngineProvider());
            }
        } catch (Throwable ignored) {
        }
    }
}
