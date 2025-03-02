package top.mrxiaom.sweetmail.config.gui;

import com.google.common.collect.Lists;
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
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.config.AbstractMenuConfig;
import top.mrxiaom.sweetmail.config.gui.entry.IconSlot;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.*;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.sweetmail.config.gui.entry.IconSlot.loadSlot;
import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuInBoxConfig extends AbstractMenuConfig<MenuInBoxConfig.Gui> {
    String titleAll, titleAllOther, titleUnread, titleUnreadOther;
    Icon iconAll;
    Icon iconUnread;
    Icon iconOut;
    Icon iconPrevPage;
    Icon iconNextPage;
    Icon iconGetAll;
    String iconGetAllRedirect;
    IconSlot iconSlot;
    int slotsCount;
    public String messageFail;
    public String messageOutdated;
    public String messageReadAll;
    public MenuInBoxConfig(SweetMail plugin) {
        super(plugin, "menus/inbox.yml");
    }

    public int getSlotsCount() {
        return slotsCount;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
        messageFail = cfg.getString("messages.inbox.attachments-fail", "");
        messageOutdated = cfg.getString("messages.inbox.attachments-outdated", "");
        messageReadAll = cfg.getString("messages.inbox.read-all", "");

        titleAll = config.getString("title-all", "&0收件箱 全部 ( %page%/%max_page% 页)");
        titleAllOther = config.getString("title-all-other", "&0%target% 的收件箱 全部 ( %page%/%max_page% 页)");
        titleUnread = config.getString("title-unread", "&0收件箱 未读 ( %page%/%max_page% 页)");
        titleUnreadOther = config.getString("title-unread-other", "&0%target% 的收件箱 未读 ( %page%/%max_page% 页)");
        slotsCount = 0;
        for (char c : inventory) {
            if (c == '格') slotsCount++;
        }
    }

    public Inventory createInventory(Gui gui, Player player, String target, boolean unread, boolean other, int page, int maxPage) {
        String title = unread
                ? (other ? titleUnreadOther : titleUnread)
                : (other ? titleAllOther : titleAll);
        return plugin.getInventoryFactory().create(gui, inventory.length,
                ColorHelper.parseColor(PAPI.setPlaceholders(player, replace(
                        title,
                        Pair.of("%target%", target),
                        Pair.of("%page%", page),
                        Pair.of("%max_page%", maxPage > 0 ? maxPage : "?")
                )))
        );
    }

    @Override
    protected void clearMainIcons() {
        iconAll = iconUnread = iconPrevPage = iconNextPage = iconGetAll = null;
        iconGetAllRedirect = null;
    }

    @Override
    protected boolean loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon) {
        switch (key) {
            case "全":
                iconAll = loadedIcon;
                return true;
            case "读":
                iconUnread = loadedIcon;
                return true;
            case "发":
                iconOut = loadedIcon;
                return true;
            case "上":
                iconPrevPage = loadedIcon;
                return true;
            case "下":
                iconNextPage = loadedIcon;
                return true;
            case "领":
                iconGetAll = loadedIcon;
                iconGetAllRedirect = section.getString(key + ".redirect");
                return true;
            case "格":
                iconSlot = loadSlot(section, key, loadedIcon);
                return true;
        }
        return false;
    }


    @Override
    protected ItemStack tryApplyMainIcon(Gui gui, String key, Player target, int iconIndex) {
        switch (key) {
            case "全":
                return iconAll.generateIcon(target);
            case "读":
                return iconUnread.generateIcon(target);
            case "发":
                return iconOut.generateIcon(target);
            case "上":
                return iconPrevPage.generateIcon(target);
            case "下":
                return iconNextPage.generateIcon(target);
            case "领":
                String targetKey = plugin.getPlayerKey(target);
                if (plugin.getMailDatabase().hasUnUsed(targetKey)) {
                    return iconGetAll.generateIcon(target);
                } else {
                    Icon icon = otherIcon.get(iconGetAllRedirect);
                    if (icon != null) {
                        return icon.generateIcon(target);
                    }
                }
                break;
            case "格":
                ListX<MailWithStatus> inBox = gui.getInBox();
                if (iconIndex >= 0 && iconIndex < inBox.size()) {
                    MailWithStatus mail = inBox.get(iconIndex);
                    ItemStack icon = ItemStackUtil.resolveBundle(target, mail.generateIcon(), mail.attachments);
                    String sender = mail.senderDisplay.trim().isEmpty()
                            ? Util.getPlayerName(mail.sender) : mail.senderDisplay;
                    String receiver = mail.receivers.size() == 1
                            ? Util.getPlayerName(mail.receivers.get(0))
                            : iconSlot.getReceiverAndSoOnMessage()
                            .replace("%player%", gui.getTarget())
                            .replace("%count%", String.valueOf(mail.receivers.size()));
                    return iconSlot.generateIcon(target, mail, icon,
                            Pair.of("%title%", mail.title),
                            Pair.of("%sender%", sender),
                            Pair.of("%pages%", String.valueOf(mail.content.size())),
                            Pair.of("%count%", String.join("", mail.content).length()),
                            Pair.of("%receiver%", receiver),
                            Pair.of("%receiver%", mail),
                            Pair.of("%time%", plugin.text().toString(mail.time))
                    );
                } else {
                    Icon icon = otherIcon.get(iconSlot.getRedirect());
                    if (icon != null) {
                        return icon.generateIcon(target);
                    }
                }
                break;
        }
        return null;
    }

    public static MenuInBoxConfig inst() {
        return get(MenuInBoxConfig.class).orElseThrow(IllegalStateException::new);
    }

    public class Gui extends AbstractPluginHolder implements IGui {
        private final Player player;
        @NotNull
        private final String target;
        private boolean unread;
        private Inventory created;
        int page = 1;
        ListX<MailWithStatus> inBox;
        public Gui(SweetMail plugin, Player player, @NotNull String target, boolean unread) {
            super(plugin);
            this.player = player;
            this.target = target;
            this.unread = unread;
        }

        @NotNull
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

        @NotNull
        @Override
        public Inventory getInventory() {
            return created;
        }

        @Override
        public Inventory newInventory() {
            String targetKey;
            if (plugin.isOnlineMode()) {
                OfflinePlayer offline = Util.getOfflinePlayer(target).orElse(null);
                targetKey = plugin.getPlayerKey(offline);
            } else {
                targetKey = target;
            }
            inBox = targetKey != null
                    ? plugin.getMailDatabase().getInBox(unread, targetKey, page, getSlotsCount())
                    : new ListX<>(-1);
            boolean other = !target.equals(player.getName());
            int maxPage = inBox.getMaxPage(getSlotsCount());
            created = createInventory(this, player, target, unread, other, page, maxPage);
            applyIcons(this, created, player);
            return created;
        }

        @Override
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
            event.setCancelled(true);
            Character c = getSlotKey(slot);
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
                    if (!click.isShiftClick()) {
                        if (click.isLeftClick()) {
                            if (unread) return;
                            unread = true;
                            plugin.getGuiManager().openGui(this);
                            return;
                        }
                        if (click.isRightClick()) {
                            String targetKey;
                            if (plugin.isOnlineMode()) {
                                OfflinePlayer offline = Util.getOfflinePlayer(target).orElse(null);
                                targetKey = plugin.getPlayerKey(offline);
                            } else {
                                targetKey = target;
                            }
                            plugin.getMailDatabase().markAllRead(targetKey);
                            t(player, plugin.prefix() + messageReadAll);
                            return;
                        }
                    }
                    return;
                }
                case "发": {
                    if (!click.isShiftClick() && click.isLeftClick()) {
                        MenuOutBoxConfig.inst()
                                .new Gui(plugin, player, target)
                                .open();
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
                        int maxPage = inBox.getMaxPage(getSlotsCount());
                        if (maxPage > 0 && page >= maxPage) return;
                        page++;
                        plugin.getGuiManager().openGui(this);
                    }
                    return;
                }
                case "领": {
                    if (!click.isShiftClick() && click.isLeftClick()) {
                        if (!player.getName().equals(target)) return; // 不可代领
                        if (inBox.isEmpty() || inBox.get(0).used) return;
                        String targetKey;
                        if (plugin.isOnlineMode()) {
                            OfflinePlayer offline = Util.getOfflinePlayer(target).orElse(null);
                            targetKey = plugin.getPlayerKey(offline);
                        } else {
                            targetKey = target;
                        }
                        List<MailWithStatus> unused = plugin.getMailDatabase().getInBoxUnused(targetKey);
                        if (unused.isEmpty()) return;
                        List<String> dismiss = new ArrayList<>();
                        for (MailWithStatus mail : unused) {
                            if (mail.used) continue;
                            mail.used = true;
                            dismiss.add(mail.uuid);
                            if (mail.isOutdated()) {
                                t(player, plugin.prefix() + messageOutdated);
                                continue;
                            }
                            plugin.getScheduler().runNextTick((t_) -> {
                                try {
                                    for (IAttachment attachment : mail.attachments) {
                                        if (attachment.isLegal()) {
                                            attachment.use(player);
                                        } else {
                                            IAttachment.Internal.useIllegalDeny(player);
                                        }
                                    }
                                } catch (Throwable t) {
                                    warn("玩家 " + target + " 领取 " + Util.getPlayerName(mail.sender) + " 邮件 " + mail.uuid + " 的附件时出现一个错误", t);
                                    t(player, plugin.prefix() + messageFail);
                                }
                            });
                        }
                        plugin.getMailDatabase().markUsed(dismiss, targetKey);
                        open();
                    }
                    return;
                }
                case "格": {
                    int i = getKeyIndex(c, slot);
                    if (i < 0 || i >= inBox.size()) return;
                    MailWithStatus mail = inBox.get(i);
                    String targetKey;
                    if (plugin.isOnlineMode()) {
                        OfflinePlayer offline = Util.getOfflinePlayer(target).orElse(null);
                        targetKey = plugin.getPlayerKey(offline);
                    } else {
                        targetKey = target;
                    }
                    plugin.getMailDatabase().markRead(mail.uuid, targetKey);
                    if (click.isLeftClick()) {
                        if (click.isShiftClick() && !mail.attachments.isEmpty() && !mail.used) {
                            // Shift+左键 领取附件
                            mail.used = true;
                            plugin.getMailDatabase().markUsed(Lists.newArrayList(mail.uuid), targetKey);
                            if (mail.isOutdated()) {
                                t(player, plugin.prefix() + messageOutdated);
                                return;
                            }
                            plugin.getScheduler().runNextTick((t_) -> {
                                try {
                                    for (IAttachment attachment : mail.attachments) {
                                        if (attachment.isLegal()) {
                                            attachment.use(player);
                                        } else {
                                            IAttachment.Internal.useIllegalDeny(player);
                                        }
                                    }
                                } catch (Throwable t) {
                                    warn("玩家 " + target + " 领取 " + Util.getPlayerName(mail.sender) + " 邮件 " + mail.uuid + " 的附件时出现一个错误", t);
                                    t(player, plugin.prefix() + messageFail);
                                }
                            });
                            plugin.getMailDatabase().getInBoxUnused(targetKey);
                            applyIcons(this, view, player);
                            return;
                        }
                        // 左键 查看正文
                        plugin.getBookImpl().openBook(player, mail);
                        return;
                    }
                    if (click.isRightClick()) {
                        if (click.isShiftClick()) { // Shift+右键 标记已读并刷新界面图标
                            plugin.getMailDatabase().getInBoxUnused(targetKey);
                            applyIcons(this, view, player);
                            return;
                        }
                        // 右键 预览附件
                        MenuViewAttachmentsConfig.inst()
                                .new Gui(this, player, mail)
                                .open();
                        return;
                    }
                    return;
                }
                default: {
                    handleClick(player, click, c);
                    break;
                }
            }
        }
    }
}
