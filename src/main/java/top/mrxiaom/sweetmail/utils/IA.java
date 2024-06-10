package top.mrxiaom.sweetmail.utils;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class IA {
    private static final Map<String, ItemStack> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static boolean isEnabled = false;
    protected static void init() {
        isEnabled = Util.isPresent("dev.lone.itemsadder.api.CustomStack");
    }
    public static Optional<ItemStack> get(String id) {
        if (!isEnabled) return Optional.empty();
        if (cache.containsKey(id)) return Optional.ofNullable(cache.get(id)).map(ItemStack::clone);
        CustomStack stack = CustomStack.getInstance(id);
        if (stack == null) return Optional.empty();
        ItemStack item = stack.getItemStack();
        if (item == null) return Optional.empty();
        item = item.clone();
        cache.put(id, item);
        return Optional.of(item);
    }
}
