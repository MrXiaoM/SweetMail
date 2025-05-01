package top.mrxiaom.sweetmail.utils.items;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.utils.SkullsUtil;

public class HeadBase64Provider implements ItemProvider {
    private final ItemStack headItem;
    @SuppressWarnings({"deprecation"})
    public HeadBase64Provider() {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            headItem = new ItemStack(Material.PLAYER_HEAD, 1);
        } else {
            headItem = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
        }
    }
    @Override
    public ItemStack get(@Nullable Player player, String base64) {
        ItemStack item = headItem.clone();
        ItemMeta meta = SkullsUtil.setSkullBase64(item.getItemMeta(), base64);
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
