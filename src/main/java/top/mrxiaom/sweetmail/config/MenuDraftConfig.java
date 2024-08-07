package top.mrxiaom.sweetmail.config;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
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
import org.bukkit.inventory.meta.SkullMeta;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.commands.CommandMain;
import top.mrxiaom.sweetmail.database.entry.AttachmentItem;
import top.mrxiaom.sweetmail.database.entry.IAttachment;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.gui.AbstractDraftGui;
import top.mrxiaom.sweetmail.gui.GuiIcon;
import top.mrxiaom.sweetmail.utils.ChatPrompter;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuDraftConfig extends AbstractMenuConfig<MenuDraftConfig.Gui> {
    Icon iconReceiver;
    String iconReceiverUnset;
    public String iconReceiverPromptTips;
    public String iconReceiverPromptCancel;
    Icon iconIcon;
    public String iconIconTitle;
    public String iconIconTitleCustom;
    Icon iconTitle;
    public String iconTitlePromptTips;
    public String iconTitlePromptCancel;
    Icon iconContent;
    Icon iconAdvanced;
    String iconAdvancedRedirectKey;
    Icon iconReset;
    Icon iconSend;
    Icon iconAttachment;

    public String messageNoReceivers;
    public String messageCantSendToYourself;
    public String messageSent;
    public boolean canSendToYourself;
    public MenuDraftConfig(SweetMail plugin) {
        super(plugin, "menus/draft.yml");
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
        canSendToYourself = cfg.getBoolean("can-send-to-yourself", false);
        messageNoReceivers = cfg.getString("messages.draft.no-receivers");
        messageCantSendToYourself = cfg.getString("messages.draft.cant-send-to-yourself");
        messageSent = cfg.getString("messages.draft.sent");
    }

    @Override
    protected void clearMainIcons() {
        iconReceiver = iconIcon = iconTitle = iconContent = iconAdvanced = iconReset = iconSend = iconAttachment = null;
        iconReceiverUnset = iconAdvancedRedirectKey
                = iconReceiverPromptTips = iconReceiverPromptCancel
                = iconTitlePromptTips = iconTitlePromptCancel
                = iconIconTitle = iconIconTitleCustom = null;
    }

    @Override
    protected void loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon) {
        switch (key) {
            case "接": {
                iconReceiver = loadedIcon;
                iconReceiverUnset = section.getString(key + ".unset", "&7未设置");
                iconReceiverPromptTips = section.getString(key + ".prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“邮件接收者”&b的值 &7(输入 &ccancel &7取消设置)");
                iconReceiverPromptCancel = section.getString(key + ".prompt-cancel", "cancel");
                break;
            }
            case "图": {
                iconIcon = loadedIcon;
                iconIconTitle = section.getString(key + ".title", "选择图标");
                iconIconTitleCustom = section.getString(key + ".title-custom", "选择图标 (可在物品栏选择)");
                break;
            }
            case "题": {
                iconTitle = loadedIcon;
                iconTitlePromptTips = section.getString(key + ".prompt-tips", "&7[&e&l邮件&7] &b请在聊天栏发送&e“邮件标题”&b的值 &7(输入 &ccancel &7取消设置)");
                iconTitlePromptCancel = section.getString(key + ".prompt-cancel", "cancel");
                break;
            }
            case "文": {
                iconContent = loadedIcon;
                break;
            }
            case "高": {
                iconAdvanced = loadedIcon;
                iconAdvancedRedirectKey = section.getString(key + ".redirect", "黑");
                break;
            }
            case "重": {
                iconReset = loadedIcon;
                break;
            }
            case "发": {
                iconSend = loadedIcon;
                break;
            }
            case "附": {
                iconAttachment = loadedIcon;
                break;
            }
        }
    }

    @Override
    public Inventory createInventory(Gui gui, Player target) {
        return Bukkit.createInventory(null, inventory.length, replace(PAPI.setPlaceholders(target, title), Pair.of("%title%", gui.getDraft().title)));
    }

    @Override
    protected ItemStack tryApplyMainIcon(Gui gui, String key, Player target, int iconIndex) {
        DraftManager manager = DraftManager.inst();
        DraftManager.Draft draft = manager.getDraft(target);
        switch (key) {
            case "接": {
                String receiver = draft.receiver.isEmpty() ? iconReceiverUnset : draft.receiver;
                ItemStack item = iconReceiver.generateIcon(
                        target,
                        Pair.of("%receiver%", receiver)
                );
                if (!draft.receiver.isEmpty() && item.getItemMeta() instanceof SkullMeta) {
                    OfflinePlayer owner = Util.getOfflinePlayer(draft.receiver).orElse(null);
                    SkullMeta meta = (SkullMeta) item.getItemMeta();
                    meta.setOwningPlayer(owner);
                    item.setItemMeta(meta);
                }
                return item;
            }
            case "图": {
                ItemStack item = ItemStackUtil.getItem(manager.getMailIcon(draft.iconKey));
                return iconIcon.generateIcon(
                        target, item,
                        Pair.of("%icon%", draft.iconKey)
                );
            }
            case "题": {
                return iconTitle.generateIcon(
                        target,
                        Pair.of("%title%", draft.title)
                );
            }
            case "文": {
                return iconContent.generateIcon(
                        target,
                        Pair.of("%content_size%", String.join("", draft.content).length())
                );
            }
            case "高": {
                if (target.hasPermission(CommandMain.PERM_ADMIN)) {
                    return iconAdvanced.generateIcon(target);
                } else {
                    Icon icon = otherIcon.get(iconAdvancedRedirectKey);
                    return icon == null ? null : icon.generateIcon(target);
                }
            }
            case "重": {
                return iconReset.generateIcon(target);
            }
            case "发": {
                return iconSend.generateIcon(target);
            }
            case "附": {
                if (iconIndex < draft.attachments.size()) {
                    IAttachment attachment = draft.attachments.get(iconIndex);
                    return attachment.generateDraftIcon(target);
                } else {
                    return iconAttachment.generateIcon(target);
                }
            }
        }
        return null;
    }

    public static MenuDraftConfig inst() {
        return get(MenuDraftConfig.class).orElseThrow(IllegalStateException::new);
    }

    public class Gui extends AbstractDraftGui {
        public Gui(SweetMail plugin, Player player) {
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
                            plugin.getGuiManager().openGui(MenuDraftAdvanceConfig.inst().new Gui(plugin, player));
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
                            String s = draft.advReceivers;
                            if (s.equalsIgnoreCase("current online")) {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    receivers.add(player.getName());
                                }
                            }
                            if (s.equalsIgnoreCase("current online bungeecord")) {
                                // TODO: 从代理端获取玩家列表
                            }
                            if (s.startsWith("last played in ")) {
                                Long timeRaw = Util.parseLong(s.substring(15)).orElse(null);
                                if (timeRaw != null) {
                                    long time = System.currentTimeMillis() - timeRaw;
                                    List<OfflinePlayer> players = Util.getOfflinePlayers();
                                    players.removeIf(it -> it == null || it.getName() == null || it.getLastPlayed() > time);
                                    for (OfflinePlayer player : players) {
                                        receivers.add(player.getName());
                                    }
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
}
