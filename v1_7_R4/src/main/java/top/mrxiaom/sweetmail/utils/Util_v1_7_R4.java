package top.mrxiaom.sweetmail.utils;

import net.minecraft.server.v1_7_R4.PacketPlayOutSetSlot;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Util_v1_7_R4 {
    public static void openBook(JavaPlugin plugin, Player player, ItemStack book) {
        if (!(player instanceof CraftPlayer)) return;
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PlayerConnection p = craftPlayer.getHandle().playerConnection;
        BukkitScheduler scheduler = Bukkit.getScheduler();
        net.minecraft.server.v1_7_R4.ItemStack bookItem = CraftItemStack.asNMSCopy(book);
        int slot = 36 + player.getInventory().getHeldItemSlot();

        // 1.7.10 没有 MC|BOpen 包，只能使用假物品这一策略了
        scheduler.runTask(plugin, () -> p.sendPacket(new PacketPlayOutSetSlot(0, slot, bookItem)));
    }
}
