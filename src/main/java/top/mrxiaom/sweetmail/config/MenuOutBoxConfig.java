package top.mrxiaom.sweetmail.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.entry.IAttachment;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.gui.GuiOutBox;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.ListX;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class MenuOutBoxConfig extends AbstractMenuConfig<GuiOutBox> {
    public static class IconSlot {
        public final Icon base;
        Map<String, List<String>> loreParts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        List<String> loreContent;
        List<String> attachmentFormat;
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
                    if (key.equals("attachments")) {
                        for (IAttachment attachment : mail.attachments) {
                            lore.addAll(replace(attachmentFormat, Pair.of("%attachment%", attachment.toString())));
                        }
                    } else {
                        lore.add(key);
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

        titleOther = config.getString("title-all-other", "&0%target% 的发件箱 %page%/%max_page%");
        slotsCount = 0;
        for (char c : inventory) {
            if (c == '格') slotsCount++;
        }
    }

    public Inventory createInventory(Player target, boolean other, int page, int maxPage) {
        String title = other ? this.titleOther : this.title;
        return Bukkit.createInventory(null, inventory.length,
                PAPI.setPlaceholders(target, replace(
                        title,
                        Pair.of("%page%", page),
                        Pair.of("%max_page%", maxPage)
                ))
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

    IconSlot loadSlot(ConfigurationSection section, String key, Icon base) {
        IconSlot icon = new IconSlot(base);
        ConfigurationSection section1 = section.getConfigurationSection(key + ".lore-parts");
        if (section1 != null) for (String k : section1.getKeys(false)) {
            icon.loreParts.put(k, section1.getStringList(k));
        }
        icon.loreContent = section.getStringList(key + ".lore-content");
        icon.attachmentFormat = section.getStringList(key + ".lore-format.attachment-item");
        icon.redirect = section.getString(key + ".redirect");
        icon.receiverAndSoOn = section.getString(key + ".lore-format.and-so-on");
        return icon;
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
