package top.mrxiaom.sweetmail.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.gui.GuiInBox;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

public class MenuInBoxConfig extends AbstractMenuConfig<GuiInBox> {
    String titleAll, titleAllOther, titleUnread, titleUnreadOther;
    Icon iconAll;
    Icon iconUnread;
    Icon iconPrevPage;
    Icon iconNextPage;
    Icon iconGetAll;
    String iconGetAllRedirect;
    public MenuInBoxConfig(SweetMail plugin) {
        super(plugin, "menus/inbox.yml");
        return;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        super.reloadConfig(cfg);
        titleAll = config.getString("title-all", "&0收件箱 全部 %page%/%max_page%");
        titleAllOther = config.getString("title-all-other", "&0%target% 的收件箱 全部 %page%/%max_page%");
        titleUnread = config.getString("title-unread", "&0收件箱 未读 %page%/%max_page%");
        titleUnreadOther = config.getString("title-unread-other", "&0%target% 的收件箱 未读 %page%/%max_page%");
    }

    public Inventory createInventory(GuiInBox gui, Player target, boolean unread, boolean other) {
        String title = unread
                ? (other ? titleUnreadOther : titleUnread)
                : (other ? titleAllOther : titleAll);
        return Bukkit.createInventory(null, inventory.length, PAPI.setPlaceholders(target, title));
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
        }
    }

    @Override
    protected ItemStack tryApplyMainIcon(GuiInBox gui, String key, Player target, int iconIndex) {
        switch (key) {
            case "全":
                return iconAll.generateIcon(target);
            case "读":
                return iconUnread.generateIcon(target);
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
                break;
        }
        return null;
    }

    public static MenuInBoxConfig inst() {
        return get(MenuInBoxConfig.class).orElseThrow(IllegalStateException::new);
    }
}
