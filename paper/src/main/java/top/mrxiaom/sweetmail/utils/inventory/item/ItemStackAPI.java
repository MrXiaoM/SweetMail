package top.mrxiaom.sweetmail.utils.inventory.item;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface ItemStackAPI {
    boolean isTextUseComponent();
    Component getItemDisplayName(ItemStack item);
    void setItemDisplayName(ItemStack item, Component name);
    List<Component> getItemLore(ItemStack item);
    void setItemLore(ItemStack item, List<Component> lore);
}
