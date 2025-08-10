package top.mrxiaom.sweetmail.utils.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.depend.Mythic;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.Map;

public interface ItemProvider {
    ItemStack get(@Nullable Player player, String argument);

    static void loadBuiltIn(Map<String, ItemProvider> map) {
        try {
            Class.forName("dev.lone.itemsadder.api.CustomStack");
            map.put("itemsadder-", new ItemsAdderProvider());
        } catch (Throwable ignored) {
        }
        try {
            if (Mythic.isAvailable()) {
                map.put("mythic-", new MythicProvider());
            }
        } catch (Throwable ignored) {
        }
        map.put("head-base64-", new HeadBase64Provider());
        try {
            Class.forName("net.momirealms.craftengine.bukkit.api.CraftEngineItems");
            String major = System.getProperty("java.version").split("\\.")[0];
            int javaVersion = Util.parseInt(major).orElse(0);
            if (javaVersion >= 21) {
                map.put("craftengine-", new CraftEngineProvider());
            }
        } catch (Throwable ignored) {
        }
        try {
            Class.forName("pers.neige.neigeitems.manager.ItemManager");
            map.put("neigeitems-", new NeigeItemsProvider());
        } catch (Throwable ignored) {
        }
    }
}
