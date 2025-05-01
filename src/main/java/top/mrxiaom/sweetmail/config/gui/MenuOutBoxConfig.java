package top.mrxiaom.sweetmail.config.gui;

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
import top.mrxiaom.sweetmail.Messages;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.config.AbstractMenuConfig;
import top.mrxiaom.sweetmail.config.gui.entry.IconSlot;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.gui.IGui;
import top.mrxiaom.sweetmail.utils.*;
import top.mrxiaom.sweetmail.depend.PAPI;

import static top.mrxiaom.sweetmail.config.gui.entry.IconSlot.loadSlot;
import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuOutBoxConfig extends AbstractMenuConfig<MenuOutBoxConfig.Gui> {
    String title, titleOther;
    Icon iconAll;
    Icon iconUnread;
    Icon iconOut;
    Icon iconPrevPage;
    Icon iconNextPage;
    Icon iconGetAll;
    String iconGetAllRedirect;
    IconSlot iconSlot;
    int slotsCount;
    public MenuOutBoxConfig(SweetMail plugin) {
        super(plugin, "menus/outbox.yml");
    }

    public int getSlotsCount() {
        return slotsCount;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);

        title = config.getString("title", "&0发件箱 ( %page%/%max_page% 页)");
        titleOther = config.getString("title-other", "&0%target% 的发件箱 ( %page%/%max_page% 页)");
        slotsCount = 0;
        for (char c : inventory) {
            if (c == '格') slotsCount++;
        }
    }

    public Inventory createInventory(Gui gui, Player player, String target, boolean other, int page, int maxPage) {
        String title = other ? this.titleOther : this.title;
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
                ListX<MailWithStatus> inBox = gui.getOutBox();
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

    public static MenuOutBoxConfig inst() {
        return instanceOf(MenuOutBoxConfig.class);
    }


    public class Gui extends AbstractPluginHolder implements IGui {
        private final Player player;
        @NotNull
        private final String target;
        private Inventory created;
        int page = 1;
        ListX<MailWithStatus> outBox;
        public Gui(SweetMail plugin, Player player, @NotNull String target) {
            super(plugin);
            this.player = player;
            this.target = target;
        }

        @NotNull
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

        @NotNull
        @Override
        public Inventory getInventory() {
            return created;
        }

        @Override
        public Inventory newInventory() {
            String targetKey;
            if (target.equalsIgnoreCase("#Server#")) {
                targetKey = "#Server#";
            } else if (plugin.isOnlineMode()) {
                OfflinePlayer offline = Util.getOfflinePlayer(target).orElse(null);
                targetKey = plugin.getPlayerKey(offline);
            } else {
                targetKey = target;
            }
            outBox = targetKey != null
                    ? plugin.getMailDatabase().getOutBox(targetKey, page, getSlotsCount())
                    : new ListX<>(-1);
            boolean other = !target.equals(player.getName());
            int maxPage = outBox.getMaxPage(getSlotsCount());
            created = createInventory(this, player, target, other, page, maxPage);
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
                        MenuInBoxConfig.inst()
                                .new Gui(plugin, player, target, false)
                                .open();
                    }
                    return;
                }
                case "读": {
                    if (!click.isShiftClick() && click.isLeftClick()) {
                        MenuInBoxConfig.inst()
                                .new Gui(plugin, player, target, true)
                                .open();
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
                        int maxPage = outBox.getMaxPage(getSlotsCount());
                        if (maxPage > 0 && page >= maxPage) return;
                        page++;
                        plugin.getGuiManager().openGui(this);
                    }
                    return;
                }
                case "格": {
                    if (!click.isShiftClick()) {
                        int i = getKeyIndex(c, slot);
                        if (i < 0 || i >= outBox.size()) return;
                        MailWithStatus mail = outBox.get(i);
                        if (click.isLeftClick()) {
                            plugin.getBookImpl().openBook(player, mail);
                            return;
                        }
                        if (click.isRightClick()) {
                            MenuViewAttachmentsConfig.inst()
                                    .new Gui(this, player, mail)
                                    .open();
                            return;
                        }
                        if (click.equals(ClickType.DROP) && player.hasPermission("sweetmail.admin")) {
                            player.closeInventory();
                            plugin.getMailDatabase().deleteMail(mail.uuid);
                            String sender = mail.senderDisplay.trim().isEmpty()
                                    ? Util.getPlayerName(mail.sender) : mail.senderDisplay;
                            t(player, plugin.prefix() + Messages.OutBox.deleted.str()
                                    .replace("%player%", sender)
                                    .replace("%title%", mail.title)
                                    .replace("%uuid%", mail.uuid));
                            return;
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
    }
}
