package top.mrxiaom.sweetmail.config.gui;

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
import org.bukkit.permissions.Permissible;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.commands.CommandMain;
import top.mrxiaom.sweetmail.attachments.AttachmentItem;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.config.AbstractMenuConfig;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.func.data.MailIcon;
import top.mrxiaom.sweetmail.gui.AbstractDraftGui;
import top.mrxiaom.sweetmail.gui.GuiIcon;
import top.mrxiaom.sweetmail.utils.ChatPrompter;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.util.*;

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
    public String messageNoMoney;
    public String messageMoneyFormat;
    public String messageOnlineNoPlayer;
    public String messageItemBanned;
    public boolean canSendToYourself;

    Map<String, Double> priceMap = new HashMap<>();
    public MenuDraftConfig(SweetMail plugin) {
        super(plugin, "menus/draft.yml");
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
        canSendToYourself = cfg.getBoolean("can-send-to-yourself", false);
        messageNoReceivers = cfg.getString("messages.draft.no-receivers", "");
        messageCantSendToYourself = cfg.getString("messages.draft.cant-send-to-yourself", "");
        messageSent = cfg.getString("messages.draft.sent", "");
        messageNoMoney = cfg.getString("messages.draft.no-money", "");
        messageMoneyFormat = cfg.getString("messages.draft.money-format", "");
        messageOnlineNoPlayer = cfg.getString("messages.draft.online.no-player", "");
        messageItemBanned = cfg.getString("messages.draft.attachments.item.banned", "");
        priceMap.clear();
        ConfigurationSection section = cfg.getConfigurationSection("price");
        if (section != null) for (String key : section.getKeys(false)) {
            double price = section.getDouble(key);
            priceMap.put(key, price > 0 ? price : 0);
        }
    }

    public double getPrice(Permissible permissible) {
        ArrayList<Map.Entry<String, Double>> list = Lists.newArrayList(priceMap.entrySet());
        list.sort(Comparator.comparingDouble(Map.Entry::getValue));
        for (Map.Entry<String, Double> entry : list) {
            if (permissible.hasPermission(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 0;
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
        Draft draft = manager.getDraft(target);
        switch (key) {
            case "接": {
                String receiver = draft.receiver.isEmpty() ? iconReceiverUnset : Util.getPlayerName(draft.receiver);
                ItemStack item = iconReceiver.generateIcon(
                        target,
                        Pair.of("%receiver%", receiver)
                );
                if (!draft.receiver.isEmpty() && item.getItemMeta() instanceof SkullMeta) {
                    OfflinePlayer owner = Util.getOfflinePlayerByNameOrUUID(draft.receiver).orElse(null);
                    if (owner != null) {
                        item.setItemMeta(ItemStackUtil.setSkullOwner(item.getItemMeta(), owner));
                    }
                }
                return item;
            }
            case "图": {
                MailIcon icon = manager.getMailIcon(draft.iconKey);
                String itemKey = icon == null ? draft.iconKey.substring(1) : icon.item;
                ItemStack item = ItemStackUtil.getItem(itemKey);
                String resolvedKey = draft.iconKey;
                if (icon != null && icon.display != null && !draft.iconKey.equals(icon.display)) {
                    resolvedKey = icon.display;
                } else if (resolvedKey.startsWith("!")) {
                    resolvedKey = resolvedKey.substring(1);
                }
                return iconIcon.generateIcon(
                        target, item,
                        Pair.of("%icon%", resolvedKey)
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
                return iconSend.generateIcon(
                        target,
                        Pair.of("%price%", String.format(messageMoneyFormat, getPrice(target)))
                );
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
            Inventory inv = createInventory(this, player);
            applyIcons(this, inv, player);
            return inv;
        }

        @Override
        @SuppressWarnings({"deprecation"})
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
            Character c = getSlotKey(slot);
            if (c == null) return;
            event.setCancelled(true);

            switch (String.valueOf(c)) {
                case "接": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        player.closeInventory();
                        ChatPrompter.prompt(
                                plugin, player,
                                iconReceiverPromptTips,
                                iconReceiverPromptCancel,
                                receiver -> {
                                    if (plugin.isOnlineMode()) {
                                        OfflinePlayer offline = Util.getOfflinePlayer(receiver).orElse(null);
                                        if (offline == null) {
                                            t(player, messageOnlineNoPlayer);
                                            reopen.run();
                                            return;
                                        }
                                        draft.receiver = offline.getUniqueId().toString();
                                    } else {
                                        draft.receiver = receiver;
                                    }
                                    draft.save();
                                    reopen.run();
                                }, reopen
                        );
                    }
                    return;
                }
                case "图": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        String title = player.hasPermission("sweetmail.icon.custom") ? iconIconTitleCustom : iconIconTitle;
                        plugin.getGuiManager().openGui(new GuiIcon(plugin, player, title));
                    }
                    return;
                }
                case "题": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        player.closeInventory();
                        ChatPrompter.prompt(
                                plugin, player,
                                iconTitlePromptTips,
                                iconTitlePromptCancel,
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
                                applyIcon(this, view, player, slot);
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
                                Util.openBook(player, item);
                            }
                        }
                    }
                    return;
                }
                case "高": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        if (player.hasPermission(CommandMain.PERM_ADMIN)) {
                            MenuDraftAdvanceConfig.inst()
                                    .new Gui(plugin, player)
                                    .open();
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
                        double price = getPrice(player);
                        if (!plugin.getEconomy().has(player, price)) {
                            t(player, plugin.prefix() + messageNoMoney.replace("%price%", String.format(messageMoneyFormat, price)));
                            return;
                        }
                        if (!canSendToYourself && draft.sender.equalsIgnoreCase(draft.receiver)) {
                            t(player, plugin.prefix() + messageCantSendToYourself);
                            return;
                        }
                        List<String> receivers = DraftManager.inst().generateReceivers(draft);
                        if (!canSendToYourself) receivers.remove(player.getName());
                        if (receivers.isEmpty()) {
                            t(player, plugin.prefix() + messageNoReceivers);
                            return;
                        }
                        plugin.getEconomy().withdrawPlayer(player, price);
                        String uuid = plugin.getMailDatabase().generateMailUUID();
                        Mail mail = draft.createMail(uuid, receivers);
                        plugin.getMailDatabase().sendMail(mail);
                        draft.reset();
                        draft.save();
                        player.closeInventory();
                        t(player, plugin.prefix() + messageSent);
                    }
                    return;
                }
                case "附": {
                    if (click.isLeftClick() && !click.isShiftClick()) {
                        int i = getKeyIndex(c, slot);
                        if (i < draft.attachments.size()) {
                            IAttachment attachment = draft.attachments.remove(i);
                            draft.save();
                            updateAttachmentSlots(view);
                            if (attachment != null) {
                                if (attachment.isLegal()) {
                                    attachment.use(player);
                                } else {
                                    IAttachment.Internal.inst().useIllegalDeny(player);
                                }
                            }
                        } else {
                            // 快速添加物品附件
                            if (cursor != null && !cursor.getType().equals(Material.AIR)) {
                                IAttachment attachment = AttachmentItem.build(cursor);
                                if (!attachment.isLegal()) {
                                    t(player, messageItemBanned);
                                    return;
                                }
                                event.setCursor(null);
                                draft.attachments.add(attachment);
                                draft.save();
                                updateAttachmentSlots(view);
                                return;
                            }
                            // TODO: 打开附件添加菜单
                            player.sendMessage("§a附件菜单正在制作中，敬请期待");
                        }
                    }
                    return;
                }
                default: {
                    handleClick(player, click, c);
                }
            }
        }

        private void updateAttachmentSlots(InventoryView view) {
            for (int k = 0; k < inventory.length; k++) {
                if (inventory[k] == '附') {
                    applyIcon(this, view, player, k);
                    player.updateInventory();
                }
            }
        }
    }
}
