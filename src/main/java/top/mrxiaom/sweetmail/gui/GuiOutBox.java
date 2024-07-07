package top.mrxiaom.sweetmail.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.MenuOutBoxConfig;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.utils.ListX;

public class GuiOutBox extends AbstractPluginHolder implements IGui {
    Player player;
    MenuOutBoxConfig config;
    String target;
    int page = 1;
    ListX<MailWithStatus> outBox;
    public GuiOutBox(SweetMail plugin, Player player, String target) {
        super(plugin);
        this.player = player;
        this.config = MenuOutBoxConfig.inst();
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public ListX<MailWithStatus> getOutBox() {
        return outBox;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        outBox = plugin.getDatabase().getOutBox(target, page, config.getSlotsCount());
        Inventory inv = config.createInventory(player, !target.equals(player.getName()), page, outBox.getMaxPage(config.getSlotsCount()));
        config.applyIcons(this, inv, player);
        return inv;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
        Character c = config.getSlotKey(slot);
        if (c != null) switch (String.valueOf(c)) {
            case "全": {
                if (!click.isShiftClick() && click.isLeftClick()) {
                    plugin.getGuiManager().openGui(new GuiInBox(plugin, player, target, false));
                }
                return;
            }
            case "读": {
                if (!click.isShiftClick() && click.isLeftClick()) {
                    plugin.getGuiManager().openGui(new GuiInBox(plugin, player, target, true));
                }
                return;
            }
            case "发": {
                return;
            }
            case "上": {
                if (!click.isShiftClick() && click.isLeftClick()) {
                    if (page <= 1) return;
                    page--;
                    plugin.getGuiManager().openGui(this);
                }
                return;
            }
            case "下": {
                if (!click.isShiftClick() && click.isLeftClick()) {
                    if (page >= outBox.getMaxPage(config.getSlotsCount())) return;
                    page++;
                    plugin.getGuiManager().openGui(this);
                }
                return;
            }
            case "格": {
                if (!click.isShiftClick()) {
                    int i = config.getKeyIndex(c, slot);
                    if (i < 0 || i >= outBox.size()) return;
                    MailWithStatus mail = outBox.get(i);
                    if (click.isLeftClick()) {
                        player.openBook(mail.generateBook());
                        return;
                    }
                    if (click.isRightClick()) {
                        // TODO: 打开附件预览菜单

                        return;
                    }
                }
                return;
            }
        }
    }

}
