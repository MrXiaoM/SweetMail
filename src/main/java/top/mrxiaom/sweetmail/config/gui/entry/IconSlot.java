package top.mrxiaom.sweetmail.config.gui.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.config.AbstractMenuConfig;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.comp.PAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class IconSlot {
    public final AbstractMenuConfig.Icon base;
    Map<String, List<String>> loreParts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    List<String> loreContent;
    List<String> attachmentFormat;
    List<String> attachmentBottomAvailable;
    List<String> attachmentBottomUnavailable;
    List<String> loreRead;
    List<String> loreUnread;
    String redirect;
    String receiverAndSoOn;
    List<String> attachmentAndSoOnLore;
    int attachmentAndSoOnCount;

    public IconSlot(AbstractMenuConfig.Icon base) {
        this.base = base;
    }

    public String getReceiverAndSoOnMessage() {
        return receiverAndSoOn;
    }

    public String getRedirect() {
        return redirect;
    }

    @SafeVarargs
    public final List<String> getIconLore(Player target, MailWithStatus mail, Pair<String, Object>... replacements) {
        List<String> lore = new ArrayList<>();
        for (String key : loreContent) {
            List<String> list = loreParts.get(key);
            if (list != null && !list.isEmpty()) {
                lore.addAll(list);
            } else {
                int attachmentCount = 0;
                switch (key) {
                    case "attachments":
                        for (IAttachment attachment : mail.attachments) {
                            lore.addAll(replace(attachmentFormat, Pair.of("%attachment%", attachment.toString())));
                            if (attachmentAndSoOnCount > 0 && ++attachmentCount > attachmentAndSoOnCount) {
                                lore.addAll(replace(attachmentAndSoOnLore, Pair.of("%count%", mail.attachments.size())));
                                break;
                            }
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

    public static IconSlot loadSlot(ConfigurationSection section, String key, AbstractMenuConfig.Icon base) {
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
        icon.attachmentAndSoOnLore = section.getStringList(key + ".lore-format.attachment-and-so-on.lore");
        icon.attachmentAndSoOnCount = section.getInt(key + ".lore-format.attachment-and-so-on.max-count", 7);
        return icon;
    }
}
