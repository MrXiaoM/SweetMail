package top.mrxiaom.sweetmail.utils.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ItemProvider {
    ItemStack get(@Nullable Player player, String argument);

    static void loadBuiltIn(Map<String, ItemProvider> map) {
        map.put("itemsadder-", new ItemsAdderProvider());
        map.put("mythic-", new MythicProvider());
        map.put("head-base64-", new HeadBase64Provider());
    }
}
