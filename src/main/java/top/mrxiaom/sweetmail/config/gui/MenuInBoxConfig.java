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
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.Messages;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.config.AbstractMenuConfig;
import top.mrxiaom.sweetmail.config.gui.entry.IconSlot;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.ListX;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

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
    public MenuInBoxConfig(SweetMail plugin) {
        super(plugin, "menus/inbox.yml");
    }

    public int getSlotsCount() {
        return slotsCount;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);

        titleAll = config.getString("title-all", "&0收件箱 全部 ( %page%/%max_page% 页)");
        titleAllOther = config.getString("title-all-other", "&0%target% 的收件箱 全部 ( %page%/%max_page% 页)");
        titleUnread = config.getString("title-unread", "&0收件箱 未读 ( %page%/%max_page% 页)");
        titleUnreadOther = config.getString("title-unread-other", "&0%target% 的收件箱 未读 ( %page%/%max_page% 页)");
        slotsCount = 0;
        for (char c : inventory) {
            if (c == '格') slotsCount++;
        }
    }

    @Override
    protected String getTitleText(Gui gui, Player player) {
        boolean other = !gui.target.equals(player.getName());
        String title = gui.unread
                ? (other ? titleUnreadOther : titleUnread)
                : (other ? titleAllOther : titleAll);
        int maxPage = gui.inBox.getMaxPage(getSlotsCount());
        return PAPI.setPlaceholders(player, replace(
                title,
                Pair.of("%target%", gui.target),
                Pair.of("%page%", gui.page),
                Pair.of("%max_page%", maxPage > 0 ? maxPage : "?")
        ));
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
                return iconAll.generateIcon(gui, target);
            case "读":
                return iconUnread.generateIcon(gui, target);
            case "发":
                return iconOut.generateIcon(gui, target);
            case "上":
                return iconPrevPage.generateIcon(gui, target);
            case "下":
                return iconNextPage.generateIcon(gui, target);
            case "领":
                String targetKey = plugin.getPlayerKey(target);
                if (plugin.getMailDatabase().hasUnUsed(targetKey)) {
                    return iconGetAll.generateIcon(gui, target);
                } else {
                    Icon icon = otherIcon.get(iconGetAllRedirect);
                    if (icon != null) {
                        return icon.generateIcon(gui, target);
                    }
                }
                break;
            case "格":
                ListX<MailWithStatus> inBox = gui.getInBox();
                if (iconIndex >= 0 && iconIndex < inBox.size()) {
                    MailWithStatus mail = inBox.get(iconIndex);
                    ItemStack icon = ItemStackUtil.resolveBundle(target, mail.generateIcon(gui.getPlayer()), mail.attachments);
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
                        return icon.generateIcon(gui, target);
                    }
                }
                break;
        }
        return null;
    }

    public static MenuInBoxConfig inst() {
        return instanceOf(MenuInBoxConfig.class);
    }

    public class Gui extends AbstractPluginHolder implements IGui {
        private final Player player;
        @NotNull
        private final String target;
        private boolean unread;
        private Inventory created;
        private int page = 1;
        ListX<MailWithStatus> inBox;
        private boolean refreshPlaceholdersCacheAfterClose = false;
        private boolean loading = false;
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

        public void refreshInbox() {
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
        }

        public void refreshInboxAndInv() {
            refreshInbox();
            applyIcons(this, created, player);
            Util.updateInventory(player);
        }

        @Override
        public void open() {
            loading = true;
            // 异步调用数据库，完成后再打开菜单
            refreshInboxAsync(() -> plugin.getGuiManager().openGui(this));
        }

        public void refreshInboxAsync() {
            refreshInboxAsync(null);
        }
        public void refreshInboxAsync(@Nullable Runnable post) {
            loading = true;
            plugin.getScheduler().runAsync((t1_) -> refreshInboxImpl(post));
        }

        private void refreshInboxImpl(@Nullable Runnable post) {
            loading = true;
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
            plugin.getScheduler().runNextTick((t2_) -> {
                if (created != null) {
                    created.clear();
                    applyIcons(this, created, player);
                    Util.updateInventory(player);
                }
                loading = false;
                if (post != null) {
                    post.run();
                }
            });
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public Inventory newInventory() {
            created = createInventory(this, player);
            applyIcons(this, created, player);
            return created;
        }

        @NotNull
        @Override
        public Inventory getInventory() {
            return created;
        }

        @Override
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
            event.setCancelled(true);
            if (loading) return;
            Character c = getSlotKey(slot);
            if (c != null) switch (String.valueOf(c)) {
                case "全": {
                    if (!click.isShiftClick() && click.isLeftClick()) {
                        if (!unread) return;
                        unread = false;
                        loading = true;
                        open();
                    }
                    return;
                }
                case "读": {
                    if (!click.isShiftClick()) {
                        if (click.isLeftClick()) {
                            if (unread) return;
                            unread = true;
                            loading = true;
                            open();
                            return;
                        }
                        if (click.isRightClick()) {
                            loading = true;
                            String targetKey;
                            if (plugin.isOnlineMode()) {
                                OfflinePlayer offline = Util.getOfflinePlayer(target).orElse(null);
                                targetKey = plugin.getPlayerKey(offline);
                            } else {
                                targetKey = target;
                            }
                            plugin.getMailDatabase().markAllRead(targetKey);
                            t(player, plugin.prefix() + Messages.InBox.read_all.str());
                            open();
                            return;
                        }
                    }
                    return;
                }
                case "发": {
                    if (!click.isShiftClick() && click.isLeftClick()) {
                        loading = true;
                        MenuOutBoxConfig.inst()
                                .new Gui(plugin, player, target)
                                .open();
                    }
                    return;
                }
                case "上": {
                    if (!click.isShiftClick() && click.isLeftClick()) {
                        if (page <= 1) return;
                        loading = true;
                        page--;
                        open();
                    }
                    return;
                }
                case "下": {
                    if (!click.isShiftClick() && click.isLeftClick()) {
                        int maxPage = inBox.getMaxPage(getSlotsCount());
                        if (maxPage > 0 && page >= maxPage) return;
                        loading = true;
                        page++;
                        open();
                    }
                    return;
                }
                case "领": {
                    if (!click.isShiftClick() && click.isLeftClick()) {
                        if (!player.getName().equals(target)) return; // 不可代领
                        if (inBox.isEmpty() || inBox.get(0).used) return;
                        loading = true;
                        plugin.getScheduler().runAsync((t_) -> {
                            // 需要等待“读取所有未使用附件的邮件”、“附件已使用”标记完成，再刷新菜单，故将这些逻辑都放在一个 runAsync 内
                            String targetKey;
                            if (plugin.isOnlineMode()) {
                                OfflinePlayer offline = Util.getOfflinePlayer(target).orElse(null);
                                targetKey = plugin.getPlayerKey(offline);
                            } else {
                                targetKey = target;
                            }
                            List<MailWithStatus> unused = plugin.getMailDatabase().getInBoxUnused(targetKey);
                            if (unused.isEmpty()) {
                                loading = false;
                                return;
                            }
                            List<String> dismiss = new ArrayList<>();
                            for (MailWithStatus mail : unused) {
                                if (mail.used) continue;
                                mail.used = true;
                                dismiss.add(mail.uuid);
                                if (mail.isOutdated()) {
                                    t(player, plugin.prefix() + Messages.InBox.attachments_outdated.str());
                                    continue;
                                }
                                plugin.getScheduler().runNextTick((t1_) -> useAttachments(mail));
                            }
                            plugin.getMailDatabase().markUsed(dismiss, targetKey);
                            refreshInboxAsync();
                        });
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
                    if (click.isLeftClick()) {
                        loading = true;
                        if (!mail.read) { // 只要是左键，无论何种方式，都标为已读
                            mail.read = true;
                            refreshPlaceholdersCacheAfterClose = true;
                            // 不需要等待标记已读完成，直接异步调用即可
                            plugin.getScheduler().runAsync((t_) -> plugin.getMailDatabase().markRead(mail.uuid, targetKey));
                        }
                        if (click.isShiftClick() && !mail.attachments.isEmpty() && !mail.used) {
                            // Shift+左键 领取附件
                            mail.used = true;
                            plugin.getScheduler().runAsync((t_) -> {
                                // 需要等待“附件已使用”标记完成，再刷新菜单，故将这些逻辑都放在一个 runAsync 内
                                refreshPlaceholdersCacheAfterClose = true;
                                plugin.getMailDatabase().markUsed(Lists.newArrayList(mail.uuid), targetKey);
                                if (mail.isOutdated()) {
                                    // 已过期 进行提示
                                    t(player, plugin.prefix() + Messages.InBox.attachments_outdated.str());
                                    loading = false;
                                } else {
                                    // 未过期 领取附件并刷新菜单
                                    loading = true;
                                    plugin.getScheduler().runNextTick((t1_) -> {
                                        useAttachments(mail);
                                        refreshInboxAsync();
                                    });
                                }
                            });
                            return;
                        }
                        // 左键 查看正文
                        plugin.getBookImpl().openBook(player, mail);
                        loading = false;
                        return;
                    }
                    if (click.isRightClick()) {
                        loading = true;
                        if (click.isShiftClick()) {
                            // Shift+右键 标记已读并刷新界面图标
                            plugin.getScheduler().runAsync((t_) -> {
                                // 需要等待“邮件已读”标记完成，再刷新菜单，故将这些逻辑都放在一个 runAsync 内
                                if (!mail.read) {
                                    mail.read = true;
                                    refreshPlaceholdersCacheAfterClose = true;
                                    plugin.getMailDatabase().markRead(mail.uuid, targetKey);
                                }
                                refreshInboxImpl(null);
                            });
                        } else {
                            // 右键 预览附件

                            // 不需要等待标记已读完成，直接异步调用即可
                            plugin.getScheduler().runAsync((t_) -> plugin.getMailDatabase().markRead(mail.uuid, targetKey));
                            // 打开附件浏览菜单
                            MenuViewAttachmentsConfig.inst()
                                    .new Gui(this, player, mail)
                                    .open();
                        }
                    }
                    return;
                }
                default: {
                    handleClick(player, click, c);
                    break;
                }
            }
        }

        @Override
        public void onClose(InventoryView view) {
            if (refreshPlaceholdersCacheAfterClose) {
                plugin.getScheduler().runAsync((t) -> {
                    plugin.getMailDatabase().getInBoxCount(player, true);
                });
            }
        }

        private void useAttachments(Mail mail) {
            try {
                for (IAttachment attachment : mail.attachments) {
                    if (attachment.isLegal()) {
                        attachment.use(player);
                        attachment.onClaimed(mail, player);
                    } else {
                        IAttachment.Internal.useIllegalDeny(player);
                    }
                }
                info("玩家 " + target + " 领取了 " + Util.getPlayerName(mail.sender) + " 的邮件 " + mail.title + " (" + mail.uuid + ") 的附件");
            } catch (Throwable t) {
                warn("玩家 " + target + " 领取 " + Util.getPlayerName(mail.sender) + " 的邮件 " + mail.title + " (" + mail.uuid + ") 的附件时出现一个错误", t);
                t(player, plugin.prefix() +  Messages.InBox.attachments_fail.str());
            }
        }
    }
}
