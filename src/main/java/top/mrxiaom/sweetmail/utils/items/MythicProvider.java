package top.mrxiaom.sweetmail.utils.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.depend.Mythic;

public class MythicProvider implements ItemProvider {
    @Override
    public ItemStack get(@Nullable Player player, String argument) {
        return Mythic.getItem(argument).orElseThrow(
                () -> new IllegalStateException("找不到 Mythic 物品 " + argument)
        );
    }
}
