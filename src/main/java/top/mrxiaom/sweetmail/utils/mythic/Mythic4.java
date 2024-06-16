package top.mrxiaom.sweetmail.utils.mythic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.utils.comp.Mythic;

import java.util.Optional;

public class Mythic4 implements Mythic.IMythic {
    MythicMobs inst = MythicMobs.inst();
    @Override
    public Optional<ItemStack> getItem(String name) {
        return inst.getItemManager().getItem(name).map(it -> it.generateItemStack(1)).map(BukkitAdapter::adapt);
    }
}
