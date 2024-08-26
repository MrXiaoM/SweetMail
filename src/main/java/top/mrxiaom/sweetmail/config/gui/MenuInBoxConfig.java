package top.mrxiaom.sweetmail.config.gui;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
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
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.*;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.util.*;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuInBoxConfig extends AbstractMenuConfig<MenuInBoxConfig.Gui> {
    public static class IconSlot {
        public final Icon base;
        Map<String, List<String>> loreParts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        List<String> loreContent;
        List<String> attachmentFormat;
        List<String> attachmentBottomAvailable;
        List<String> attachmentBottomUnavailable;
        List<String> loreRead;
        List<String> loreUnread;
        String redirect;
        String receiverAndSoOn;
        private IconSlot(Icon base) {
            this.base = base;
        }

        @SafeVarargs
        public final List<String> getIconLore(Player target, MailWithStatus mail, Pair<String, Object>... replacements) {
            List<String> lore = new ArrayList<>();
            for (String key : loreContent) {
                List<String> list = loreParts.get(key);
                if (list != null && !list.isEmpty()) {
                    lore.addAll(list);
                } else {
                    switch (key) {
                        case "attachments":
                            for (IAttachment attachment : mail.attachments) {
                                lore.addAll(replace(attachmentFormat, Pair.of("%attachment%", attachment.toString())));
                            }
                            break;
                        case "bottom_attachments":
                            lore.addAll(mail.used ? attachmentBottomUnavailable : attachmentBottomAvailable);
                            break;
                        case "read":
                            lore.addAll(mail.read ? loreRead : loreUnread);
                            break;
                        default:
                            lore.add(key);
                            break;
                    }
                }
            }

            return PAPI.setPlaceholders(target, replace(lore, replacements));
        }

        @SafeVarargs
        public final ItemStack generateIcon(Player target, MailWithStatus mail, ItemStack icon, Pair<String, Object>... replacements) {
            if (base.display != null) {
                ItemStackUtil.setItemDisplayName(icon, PAPI.setPlaceholders(target, replace(base.display, replacements)));
            }
            List<String> lore = getIconLore(target, mail, replacements);

            if (!lore.isEmpty()) {
                ItemStackUtil.setItemLore(icon, lore);
            }
            if (base.glow) {
                ItemStackUtil.setGlow(icon);
            }
            return icon;
        }
    }
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

        titleAll = config.getString("title-all", "&0收件箱 全部 ( %page%/%max_page% 页)");
        titleAllOther = config.getString("title-all-other", "&0%target% 的收件箱 全部 ( %page%/%max_page% 页)");
        titleUnread = config.getString("title-unread", "&0收件箱 未读 ( %page%/%max_page% 页)");
        titleUnreadOther = config.getString("title-unread-other", "&0%target% 的收件箱 未读 ( %page%/%max_page% 页)");
        slotsCount = 0;
        for (char c : inventory) {
            if (c == '格') slotsCount++;
        }
    }

    public Inventory createInventory(Player target, boolean unread, boolean other, int page, int maxPage) {
        String title = unread
                ? (other ? titleUnreadOther : titleUnread)
                : (other ? titleAllOther : titleAll);
        return Bukkit.createInventory(null, inventory.length,
                ColorHelper.parseColor(PAPI.setPlaceholders(target, replace(
                        title,
                        Pair.of("%page%", page),
                        Pair.of("%max_page%", maxPage)
                )))
        );
    }

    @Override
    protected void clearMainIcons() {
        iconAll = iconUnread = iconPrevPage = iconNextPage = iconGetAll = null;
        iconGetAllRedirect = null;
    }

    @Override
    protected void loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon) {
        switch (key) {
            case "全":
                iconAll = loadedIcon;
                break;
            case "读":
                iconUnread = loadedIcon;
                break;
            case "发":
                iconOut = loadedIcon;
                break;
            case "上":
                iconPrevPage = loadedIcon;
                break;
            case "下":
                iconNextPage = loadedIcon;
                break;
            case "领":
                iconGetAll = loadedIcon;
                iconGetAllRedirect = section.getString(key + ".redirect");
                break;
            case "格":
                iconSlot = loadSlot(section, key, loadedIcon);
                break;
        }
    }

    protected static IconSlot loadSlot(ConfigurationSection section, String key, Icon base) {
        IconSlot icon = new IconSlot(base);
        ConfigurationSection section1 = section.getConfigurationSection(key + ".lore-parts");
        if (section1 != null) for (String k : section1.getKeys(false)) {
            icon.loreParts.put(k, section1.getStringList(k));
        }
        icon.loreContent = section.getStringList(key + ".lore-content");
        icon.attachmentFormat = section.getStringList(key + ".lore-format.attachment-item");
        icon.attachmentBottomAvailable = section.getStringList(key + ".lore-format.attachment.available");
        icon.attachmentBottomUnavailable = section.getStringList(key + ".lore-format.attachment.unavailable");
        icon.loreRead = section.getStringList(key + ".lore-format.read");
        icon.loreUnread = section.getStringList(key + ".lore-format.unread");
        icon.redirect = section.getString(key + ".redirect");
        icon.receiverAndSoOn = section.getString(key + ".lore-format.and-so-on", "");
        return icon;
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
                String targetKey = plugin.isOnlineMode() ? target.getUniqueId().toString() : target.getName();
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
                            : iconSlot.receiverAndSoOn
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
                    Icon icon = otherIcon.get(iconSlot.redirect);
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

        @Override
        public Inventory newInventory() {
            String targetKey;
            if (plugin.isOnlineMode()) {
                OfflinePlayer offline = Util.getOfflinePlayer(target).orElse(null);
                if (offline == null) targetKey = null;
                else targetKey = offline.getUniqueId().toString();
            } else {
                targetKey = target;
            }
            inBox = targetKey == null ? new ListX<>() : plugin.getMailDatabase().getInBox(unread, targetKey, page, getSlotsCount());
            Inventory inv = createInventory(player, unread, !target.equals(player.getName()), page, inBox.getMaxPage(getSlotsCount()));
            applyIcons(this, inv, player);
            return inv;
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
                    if (!click.isShiftClick() && click.isLeftClick()) {
                        if (unread) return;
                        unread = true;
                        plugin.getGuiManager().openGui(this);
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
                        if (page >= inBox.getMaxPage(getSlotsCount())) return;
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
                            if (offline == null) targetKey = null;
                            else targetKey = offline.getUniqueId().toString();
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
                        }
                        plugin.getMailDatabase().markUsed(dismiss, targetKey);
                        applyIcons(this, view, player);
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
                        if (offline == null) targetKey = null;
                        else targetKey = offline.getUniqueId().toString();
                    } else {
                        targetKey = target;
                    }
                    plugin.getMailDatabase().markRead(mail.uuid, targetKey);
                    if (click.isLeftClick()) {
                        if (click.isShiftClick()) { // 领取附件
                            if (!mail.attachments.isEmpty() && !mail.used) {
                                mail.used = true;
                                plugin.getMailDatabase().markUsed(Lists.newArrayList(mail.uuid), targetKey);
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
                                plugin.getMailDatabase().getInBoxUnused(targetKey);
                                applyIcons(this, view, player);
                            } else {
                                t(player, mail.attachments.size() + " " + mail.used);
                            }
                            return;
                        }
                        // 查看正文
                        Util.openBook(player, mail.generateBook());
                        return;
                    }
                    if (!click.isShiftClick() && click.isRightClick()) {
                        // 预览附件
                        MenuViewAttachmentsConfig.inst()
                                .new Gui(this, player, mail)
                                .open();
                        return;
                    }
                    return;
                }
            }
        }
    }
}
