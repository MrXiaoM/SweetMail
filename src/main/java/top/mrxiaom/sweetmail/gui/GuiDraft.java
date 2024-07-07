package top.mrxiaom.sweetmail.gui;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.commands.CommandMain;
import top.mrxiaom.sweetmail.database.entry.AttachmentItem;
import top.mrxiaom.sweetmail.database.entry.IAttachment;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.utils.ChatPrompter;

import java.util.ArrayList;
import java.util.List;

public class GuiDraft extends AbstractDraftGui {
    public GuiDraft(SweetMail plugin, Player player) {
        super(plugin, player);
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
            case "接": {
                if (click.isLeftClick() && !click.isShiftClick()) {
                    player.closeInventory();
                    ChatPrompter.prompt(
                            plugin, player,
                            config.iconReceiverPromptTips,
                            config.iconReceiverPromptCancel,
                            receiver -> {
                                draft.receiver = receiver;
                                draft.save();
                                reopen.run();
                            }, reopen
                    );
                }
                return;
            }
            case "图": {
                if (click.isLeftClick() && !click.isShiftClick()) {
                    String title = player.hasPermission("sweetmail.icon.custom") ? config.iconIconTitleCustom : config.iconIconTitle;
                    plugin.getGuiManager().openGui(new GuiIcon(plugin, player, title));
                }
                return;
            }
            case "题": {
                if (click.isLeftClick() && !click.isShiftClick()) {
                    player.closeInventory();
                    ChatPrompter.prompt(
                            plugin, player,
                            config.iconTitlePromptTips,
                            config.iconTitlePromptCancel,
                            title -> {
                                draft.title = title;
                                draft.save();
                                reopen.run();
                            }, reopen
                    );
                }
                return;
            }
            case "文": {
                if (!click.isShiftClick()) {
                    if (click.isLeftClick()) {
                        ItemMeta rawMeta = cursor != null ? cursor.getItemMeta() : null;
                        if (rawMeta instanceof BookMeta) {
                            BookMeta meta = (BookMeta) rawMeta;
                            draft.content = meta.getPages();
                            draft.save();
                            config.applyIcon(this, view, player, slot);
                            player.updateInventory();
                        }
                    }
                    if (click.isRightClick()) {
                        player.closeInventory();
                        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
                        ItemMeta rawMeta = item.getItemMeta();
                        if (rawMeta instanceof BookMeta) {
                            BookMeta meta = (BookMeta) rawMeta;
                            meta.setTitle(draft.title);
                            meta.setPages(draft.content.isEmpty() ? Lists.newArrayList("") : draft.content);
                            meta.setAuthor(player.getName());
                            item.setItemMeta(meta);
                            player.openBook(item);
                        }
                    }
                }
                return;
            }
            case "高": {
                if (click.isLeftClick() && !click.isShiftClick()) {
                    if (player.hasPermission(CommandMain.PERM_ADMIN)) {
                        // TODO: 打开高级设置菜单
                    }
                }
                return;
            }
            case "重": {
                if (click.isLeftClick() && !click.isShiftClick()) {
                    draft.reset();
                    draft.save();
                    reopen.run();
                }
                return;
            }
            case "发": {
                if (click.isLeftClick() && !click.isShiftClick()) {
                    String uuid = plugin.getDatabase().generateMailUUID();
                    String sender = draft.sender;
                    String senderDisplay = draft.advSenderDisplay == null ? "" : draft.advSenderDisplay;
                    String icon = DraftManager.inst().getMailIcon(draft.iconKey);
                    String title = draft.title;
                    List<String> content = draft.content;
                    List<IAttachment> attachments = draft.attachments;
                    if (!config.canSendToYourself && sender.equalsIgnoreCase(draft.receiver)) {
                        t(player, plugin.prefix() + config.messageCantSendToYourself);
                        return;
                    }
                    List<String> receivers = new ArrayList<>();
                    if (draft.advReceivers != null && !draft.advReceivers.isEmpty()) {
                        // TODO: 解析 advance receivers
                        if (draft.advReceivers.equalsIgnoreCase("current online")) {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                receivers.add(onlinePlayer.getName());
                            }
                        }
                    } else if (!draft.receiver.isEmpty()) {
                        receivers.add(draft.receiver);
                    }
                    receivers.removeIf(draft.manager::isInAdvanceReceiversBlackList);
                    if (receivers.isEmpty()) {
                        t(player, plugin.prefix() + config.messageNoReceivers);
                        return;
                    }
                    Mail mail = new Mail(uuid, sender, senderDisplay, icon, receivers, title, content, attachments);
                    plugin.getDatabase().sendMail(mail);
                    draft.reset();
                    draft.save();
                    player.closeInventory();
                    t(player, plugin.prefix() + config.messageSent);
                }
                return;
            }
            case "附": {
                if (click.isLeftClick() && !click.isShiftClick()) {
                    int i = config.getKeyIndex(c, slot);
                    if (i < draft.attachments.size()) {
                        IAttachment attachment = draft.attachments.remove(i);
                        draft.save();
                        updateAttachmentSlots(view);
                        if (attachment != null) {
                            attachment.use(player);
                        }
                    } else {
                        if (cursor != null && !cursor.getType().isAir()) {
                            IAttachment attachment = new AttachmentItem(cursor);
                            event.setCursor(null);
                            draft.attachments.add(attachment);
                            draft.save();
                            updateAttachmentSlots(view);
                            return;
                        }
                        // TODO: 打开附件添加菜单
                    }
                }
                return;
            }
            default: {
                config.handleClick(player, click, c);
            }
        }
    }

    private void updateAttachmentSlots(InventoryView view) {
        for (int k = 0; k < config.inventory.length; k++) {
            if (config.inventory[k] == '附') {
                config.applyIcon(this, view, player, k);
                player.updateInventory();
            }
        }
    }
}
