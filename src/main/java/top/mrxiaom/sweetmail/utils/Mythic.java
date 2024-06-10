package top.mrxiaom.sweetmail.utils;

import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.utils.mythic.Mythic5;

import java.util.Optional;

public class Mythic {
    public interface IMythic {
        Optional<ItemStack> getItem(String name);
    }
    private static IMythic proxy = null;
    protected static void load() {
        proxy = null;
        if (Util.isPresent("io.lumine.mythic.bukkit.MythicBukkit")) {
            proxy = new Mythic5();
        }
        // TODO: Mythic4
    }

    public static Optional<ItemStack> getItem(String name) {
        return Optional.ofNullable(proxy).flatMap(it -> it.getItem(name));
    }
}
