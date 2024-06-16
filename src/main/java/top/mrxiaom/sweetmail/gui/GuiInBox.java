package top.mrxiaom.sweetmail.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.MenuInBoxConfig;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;

public class GuiInBox extends AbstractPluginHolder implements IGui {
    Player player;
    MenuInBoxConfig config;
    String target;
    boolean unread;
    public GuiInBox(SweetMail plugin, Player player, String target, boolean unread) {
        super(plugin);
        this.player = player;
        this.config = MenuInBoxConfig.inst();
        this.target = target;
        this.unread = unread;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        Inventory inv = config.createInventory(this, player, unread, !target.equals(player.getName()));
        config.applyIcons(this, inv, player);
        return inv;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onDrag(InventoryView view, InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onClose(InventoryView view) {

    }
}
