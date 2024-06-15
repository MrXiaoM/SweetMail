package top.mrxiaom.sweetmail.config;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.PAPI;

public class MenuInBoxConfig extends AbstractMenuConfig {
    String titleAll, titleAllOther, titleUnread, titleUnreadOther;

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

    public Inventory createInventory(Player target, boolean unread, boolean other) {
        String title = unread
                ? (other ? titleUnreadOther : titleUnread)
                : (other ? titleAllOther : titleAll);
        return Bukkit.createInventory(null, inventory.length, PAPI.setPlaceholders(target, title));
    }

    @Override
    protected void clearMainIcons() {

    }

    @Override
    protected void loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon) {

    }

    @Override
    protected ItemStack tryApplyMainIcon(String key, Player target, int iconIndex) {
        return null;
    }

    public static MenuInBoxConfig inst() {
        return get(MenuInBoxConfig.class).orElseThrow(IllegalStateException::new);
    }
}
