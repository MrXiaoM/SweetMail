package top.mrxiaom.sweetmail.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.MenuDraftAdvanceConfig;

public class GuiDraftAdvance extends AbstractDraftGui {
    protected MenuDraftAdvanceConfig config;
    public GuiDraftAdvance(SweetMail plugin, Player player) {
        super(plugin, player);
        config = MenuDraftAdvanceConfig.inst();
    }


    @Override
    public Inventory newInventory() {
        Inventory inv = config.createInventory(this, player);
        config.applyIcons(this, inv, player);
        return inv;
    }

    @Override
    @SuppressWarnings({"deprecation"})
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        Character c = config.getSlotKey(slot);
        if (c == null) return;
        event.setCancelled(true);

        switch (String.valueOf(c)) {
            case "返": {
                if (click.isLeftClick() && !click.isShiftClick()) {
                    plugin.getGuiManager().openGui(new GuiDraft(plugin, player));
                }
                return;
            }
            default: {
                config.handleClick(player, click, c);
            }
        }
    }
}
