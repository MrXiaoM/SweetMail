package top.mrxiaom.sweetmail.utils.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

public class CraftEngineProvider implements ItemProvider {
    BiFunction<Player, String, ItemStack> impl;
    @SuppressWarnings("unchecked")
    public CraftEngineProvider() throws Throwable {
        Class<?> type = Class.forName(getClass().getName() + "Impl");
        Field field = type.getDeclaredField("PROVIDER");
        impl = (BiFunction<Player, String, ItemStack>) field.get(null);
    }
    @Override
    public ItemStack get(@Nullable Player player, String argument) {
        return impl.apply(player, argument);
    }
}
