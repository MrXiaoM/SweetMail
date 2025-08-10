package top.mrxiaom.sweetmail.depend;

import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.depend.mythic.Mythic4;
import top.mrxiaom.sweetmail.depend.mythic.Mythic5;
import top.mrxiaom.sweetmail.utils.Util;

import java.util.Optional;

public class Mythic {
    public interface IMythic {
        Optional<ItemStack> getItem(String name);
    }
    private static IMythic proxy = null;
    public static void load() {
        proxy = null;
        if (Util.isPresent("io.lumine.mythic.bukkit.MythicBukkit")) {
            proxy = new Mythic5();
        }
        if (Util.isPresent("io.lumine.xikage.mythicmobs.MythicMobs")) {
            proxy = new Mythic4();
        }
    }

    public static Optional<ItemStack> getItem(String name) {
        return Optional.ofNullable(proxy).flatMap(it -> it.getItem(name));
    }

    public static boolean isAvailable() {
        return proxy != null;
    }
}
