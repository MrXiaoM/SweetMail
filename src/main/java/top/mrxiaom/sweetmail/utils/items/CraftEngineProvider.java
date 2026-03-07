package top.mrxiaom.sweetmail.utils.items;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CraftEngineProvider implements ItemProvider {

    public static Key of(String namespacedId) {
        String[] strings = new String[]{"minecraft", namespacedId};
        int i = namespacedId.indexOf(':');
        if (i >= 0) {
            strings[1] = namespacedId.substring(i + 1);
            if (i >= 1) {
                strings[0] = namespacedId.substring(0, i);
            }
        }
        return new Key(strings[0], strings[1]);
    }

    @Override
    public ItemStack get(@Nullable Player player, String argument) {
        CustomItem<ItemStack> customItem = CraftEngineItems.byId(of(argument));
        if (customItem == null) throw new IllegalStateException("找不到 CE 物品 " + argument);
        if (player != null) {
            return customItem.buildItemStack(BukkitAdaptors.adapt(player));
        } else {
            return customItem.buildItemStack();
        }
    }

    @Nullable
    public static String getTranslationKey(@Nullable ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getAmount() < 1) {
            return null;
        }
        CustomItem<ItemStack> customItem = CraftEngineItems.byItemStack(item);
        if (customItem == null || customItem.isEmpty()) {
            return null;
        }
        return customItem.translationKey();
    }
}
