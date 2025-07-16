package top.mrxiaom.sweetmail.utils.inventory.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PaperItemStack implements ItemStackAPI {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public PaperItemStack() throws Throwable {
        ItemMeta.class.getDeclaredMethod("displayName", Component.class);
        ItemStack item = new ItemStack(Material.PAPER);
        getItemDisplayName(item);
    }

    @Override
    public boolean isTextUseComponent() {
        return true;
    }

    @Override
    public Component getItemDisplayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.displayName();
        }
        return null;
    }

    @Override
    public void setItemDisplayName(ItemStack item, Component name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            item.setItemMeta(meta);
        }
    }

    @Override
    public List<Component> getItemLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            return meta.lore();
        }
        return new ArrayList<>();
    }

    @Override
    public void setItemLore(ItemStack item, List<Component> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.lore(lore);
            item.setItemMeta(meta);
        }
    }
}
