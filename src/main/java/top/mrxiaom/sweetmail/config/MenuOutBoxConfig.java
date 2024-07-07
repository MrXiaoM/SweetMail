package top.mrxiaom.sweetmail.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.gui.GuiOutBox;
import top.mrxiaom.sweetmail.utils.ColorHelper;
import top.mrxiaom.sweetmail.utils.ListX;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.comp.PAPI;


import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuOutBoxConfig extends AbstractMenuConfig<GuiOutBox> {
    String title, titleOther;
    Icon iconAll;
    Icon iconUnread;
    Icon iconOut;
    Icon iconPrevPage;
    Icon iconNextPage;
    Icon iconGetAll;
    String iconGetAllRedirect;
    MenuInBoxConfig.IconSlot iconSlot;
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

    public Inventory createInventory(Player target, boolean other, int page, int maxPage) {
        String title = other ? this.titleOther : this.title;
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
                iconSlot = MenuInBoxConfig.loadSlot(section, key, loadedIcon);
                break;
        }
    }

    @Override
    protected ItemStack tryApplyMainIcon(GuiOutBox gui, String key, Player target, int iconIndex) {
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
                if (plugin.getDatabase().hasUnUsed(target.getName())) {
                    return iconGetAll.generateIcon(target);
                } else {
                    Icon icon = otherIcon.get(iconGetAllRedirect);
                    if (icon != null) {
                        return icon.generateIcon(target);
                    }
                }
                break;
            case "格":
                ListX<MailWithStatus> inBox = gui.getOutBox();
                if (iconIndex >= 0 && iconIndex < inBox.size()) {
                    MailWithStatus mail = inBox.get(iconIndex);
                    ItemStack icon = mail.generateIcon();
                    String sender = mail.senderDisplay.trim().isEmpty()
                            ? mail.sender : mail.senderDisplay;
                    String receiver = mail.receivers.size() == 1
                            ? mail.receivers.get(0)
                            : iconSlot.receiverAndSoOn
                            .replace("%player%", gui.getTarget())
                            .replace("%count%", String.valueOf(mail.receivers.size()));
                    return iconSlot.generateIcon(target, mail, icon,
                            Pair.of("%title%", mail.title),
                            Pair.of("%sender%", sender),
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

    public static MenuOutBoxConfig inst() {
        return get(MenuOutBoxConfig.class).orElseThrow(IllegalStateException::new);
    }
}
