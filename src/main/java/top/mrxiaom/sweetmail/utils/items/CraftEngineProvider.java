package top.mrxiaom.sweetmail.utils.items;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.item.processor.ItemNameProcessor;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CraftEngineProvider implements ItemProvider {

    public static Key of(String namespacedId) {
        return Key.of(namespacedId);
    }

    @Override
    public ItemStack get(@Nullable Player player, String argument) {
        BukkitItemDefinition customItem = CraftEngineItems.byId(of(argument));
        if (customItem == null) throw new IllegalStateException("找不到 CE 物品 " + argument);
        if (player != null) {
            return customItem.buildBukkitItem(player);
        } else {
            return customItem.buildBukkitItem();
        }
    }

    @Nullable
    public static String getTranslationKey(@Nullable ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getAmount() < 1) {
            return null;
        }
        BukkitItemDefinition customItem = CraftEngineItems.byItemStack(item);
        if (customItem == null) {
            return null;
        }
        return customItem.translationKey();
    }

    @Nullable
    public static String getItemName(@Nullable ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getAmount() < 1) {
            return null;
        }
        BukkitItemDefinition customItem = CraftEngineItems.byItemStack(item);
        if (customItem == null) {
            return null;
        }
        for (ItemProcessor processor : customItem.processors()) {
            if (processor instanceof ItemNameProcessor) {
                return ((ItemNameProcessor) processor).itemName();
            }
        }
        return null;
    }
}
