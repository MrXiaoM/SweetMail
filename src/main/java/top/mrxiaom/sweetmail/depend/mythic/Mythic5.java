package top.mrxiaom.sweetmail.depend.mythic;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.depend.Mythic;

import java.util.Optional;

public class Mythic5 implements Mythic.IMythic {
    private final MythicBukkit inst = MythicBukkit.inst();
    public Optional<ItemStack> getItem(String name) {
        return inst.getItemManager().getItem(name).map(it -> it.generateItemStack(1)).map(BukkitAdapter::adapt);
    }
}
