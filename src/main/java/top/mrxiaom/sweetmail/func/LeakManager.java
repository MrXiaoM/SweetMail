package top.mrxiaom.sweetmail.func;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Contract;
import top.mrxiaom.sweetmail.SweetMail;

public class LeakManager extends AbstractPluginHolder implements Listener {
    public LeakManager(SweetMail plugin) {
        super(plugin);
        registerEvents(this);
        register();
    }

    @Contract("null->false")
    private boolean isLeakItem(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR) && item.getAmount() > 0) {
            return NBT.get(item, nbt -> nbt.hasTag("SWEETMAIL_LEAK") && nbt.getBoolean("SWEETMAIL_LEAK"));
        }
        return false;
    }

    private void checkLeak(HumanEntity player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isLeakItem(item)) {
                inv.setItem(i, null);
            }
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e) {
        checkLeak(e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        checkLeak(e.getPlayer());
    }
}
