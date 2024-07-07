package top.mrxiaom.sweetmail.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.MenuInBoxConfig;
import top.mrxiaom.sweetmail.database.entry.IAttachment;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.utils.ListX;

import java.util.ArrayList;
import java.util.List;

public class GuiInBox extends AbstractPluginHolder implements IGui {
    Player player;
    MenuInBoxConfig config;
    String target;
    boolean unread;
    int page = 1;
    ListX<MailWithStatus> inBox;
    public GuiInBox(SweetMail plugin, Player player, String target, boolean unread) {
        super(plugin);
        this.player = player;
        this.config = MenuInBoxConfig.inst();
        this.target = target;
        this.unread = unread;
    }

    public String getTarget() {
        return target;
    }

    public ListX<MailWithStatus> getInBox() {
        return inBox;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        inBox = plugin.getDatabase().getInBox(unread, target, page, config.getSlotsCount());
        Inventory inv = config.createInventory(player, unread, !target.equals(player.getName()), page, inBox.getMaxPage(config.getSlotsCount()));
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
                    if (!unread) return;
                    unread = false;
                    plugin.getGuiManager().openGui(this);
                }
                return;
            }
            case "读": {
                if (!click.isShiftClick() && click.isLeftClick()) {
                    if (unread) return;
                    unread = true;
                    plugin.getGuiManager().openGui(this);
                }
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
                    if (page >= inBox.getMaxPage(config.getSlotsCount())) return;
                    page++;
                    plugin.getGuiManager().openGui(this);
                }
                return;
            }
            case "领": {
                if (!click.isShiftClick() && click.isLeftClick()) {
                    if (!player.getName().equals(target)) return; // 不可代领
                    if (inBox.isEmpty() || inBox.get(0).used) return;
                    List<MailWithStatus> unused = plugin.getDatabase().getInBoxUnused(target);
                    if (unused.isEmpty()) return;
                    List<String> dismiss = new ArrayList<>();
                    for (MailWithStatus mail : unused) {
                        if (mail.used) continue;
                        dismiss.add(mail.uuid);
                        try {
                            for (IAttachment attachment : mail.attachments) {
                                attachment.use(player);
                            }
                        } catch (Throwable t) {
                            warn("玩家 " + target + " 领取 " + mail.sender + " 邮件 " + mail.uuid + " 的附件时出现一个错误", t);
                            t(player, plugin.prefix() + config.messageFail);
                        }
                    }
                    plugin.getDatabase().markUsed(dismiss, target);
                }
                return;
            }
            case "格": {
                if (!click.isShiftClick()) {
                    int i = config.getKeyIndex(c, slot);
                    if (i < 0 || i >= inBox.size()) return;
                    MailWithStatus mail = inBox.get(i);
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
