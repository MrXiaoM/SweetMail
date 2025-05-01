package top.mrxiaom.sweetmail.utils.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.depend.ItemsAdder;

public class ItemsAdderProvider implements ItemProvider {
    @Override
    public ItemStack get(@Nullable Player player, String argument) {
        return ItemsAdder.get(argument).orElseThrow(
                () -> new IllegalStateException("找不到 IA 物品 " + argument)
        );
    }
}
