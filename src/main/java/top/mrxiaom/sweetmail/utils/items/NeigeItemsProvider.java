package top.mrxiaom.sweetmail.utils.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import pers.neige.neigeitems.manager.ItemManager;

public class NeigeItemsProvider implements ItemProvider {
    @Override
    public ItemStack get(@Nullable Player player, String argument) {
        ItemStack result;
        if (argument.contains(";")) {
            String[] split = argument.split(";", 2);
            result = ItemManager.INSTANCE.getItemStack(split[0], player, split[1]);
        } else {
            result = ItemManager.INSTANCE.getItemStack(argument, player);
        }
        if (result == null) {
            throw new IllegalArgumentException("找不到 NeigeItems 物品 " + argument);
        }
        return result;
    }
}
