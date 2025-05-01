package top.mrxiaom.sweetmail.utils.items;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

@SuppressWarnings("unused")
public class CraftEngineProviderImpl {
    public static final BiFunction<Player, String, ItemStack> PROVIDER = CraftEngineProviderImpl::get;
    public static ItemStack get(Player player, String argument) {
        CustomItem<ItemStack> item;
        if (argument.contains(":")) {
            String[] split = argument.split(":", 2);
            item = CraftEngineItems.byId(Key.of(split[0], split[1]));
        } else {
            item = CraftEngineItems.byId(Key.withDefaultNamespace(argument));
        }
        if (item == null) throw new IllegalStateException("找不到 CE 物品 " + argument);
        return item.buildItemStack();
    }
}
