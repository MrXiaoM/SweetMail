package top.mrxiaom.sweetmail.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;

public interface IGui {
    Player getPlayer();

    Inventory newInventory();

    void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event);

    default void onDrag(InventoryView view, InventoryDragEvent event) {
        event.setCancelled(true);
    }

    default void onClose(InventoryView view) {

    }

    default void open() {
        SweetMail.getInstance().getGuiManager().openGui(this);
    }
}
