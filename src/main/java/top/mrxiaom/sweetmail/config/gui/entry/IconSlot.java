package top.mrxiaom.sweetmail.config.gui.entry;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.config.AbstractMenuConfig;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.depend.PAPI;
import top.mrxiaom.sweetmail.utils.ItemStackUtil;
import top.mrxiaom.sweetmail.utils.Pair;
import top.mrxiaom.sweetmail.utils.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static top.mrxiaom.sweetmail.utils.Pair.replace;

public class IconSlot {
    public final AbstractMenuConfig.Icon base;
    private final Map<String, List<String>> loreParts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private List<String> loreContent;
    private List<String> attachmentFormat;
    private List<String> attachmentBottomAvailable, attachmentBottomUnavailable;
    private DateTimeFormatter formatter;
    private List<String> attachmentOutdateTime, attachmentOutdateInfinite;
    private List<String> loreRead, loreUnread;
    private String redirect;
    private String receiverAndSoOn;
    private List<String> attachmentAndSoOnLore;
    private int attachmentAndSoOnCount;

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
                for (String s : list) {
                    if (s.startsWith("!!")) {
                        if (target.hasPermission("sweetmail.admin")) {
                            lore.add(s.substring(2));
                        }
                    } else {
                        lore.add(s);
                    }
                }
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
                    case "attachments_outdate":
                        if (!mail.attachments.isEmpty()) {
                            if (mail.outdateTime > 0) {
                                LocalDateTime time = Util.fromTimestamp(mail.outdateTime);
                                lore.addAll(replace(attachmentOutdateTime, Pair.of("%time%", time.format(formatter))));
                            } else {
                                lore.addAll(attachmentOutdateInfinite);
                            }
                        }
                        break;
                    case "bottom_attachments":
                        lore.addAll(mail.used ? attachmentBottomUnavailable : attachmentBottomAvailable);
                        break;
                    case "read":
                        lore.addAll(mail.read ? loreRead : loreUnread);
                        break;
                    case "content": {
                        for (String page : mail.getContent(target)) {
                            lore.addAll(Arrays.asList(page.split("\n")));
                        }
                        break;
                    }
                    default: {
                        if (key.startsWith("content;")) {
                            String prefix = key.substring(8);
                            for (String page : mail.getContent(target)) {
                                for (String s : page.split("\n")) {
                                    lore.add(prefix + s);
                                }
                            }
                            break;
                        }
                        if (key.startsWith("short_content;")) {
                            String[] args = key.substring(14).split(";");
                            String prefix = args[0];
                            Integer num = args.length >= 2 ? Util.parseInt(args[1]).orElse(null) : null;
                            String ellipsis = args.length >= 3 ? args[2] : "...";
                            if (num != null) {
                                int total = 0;
                                for (String page : mail.getContent(target)) {
                                    int length = page.length();
                                    boolean end;
                                    String text;
                                    if (total + length >= num) {
                                        text = page.substring(0, num - total) + ellipsis;
                                        end = true;
                                    } else {
                                        text = page;
                                        end = false;
                                    }
                                    total += length;
                                    for (String s : text.split("\n")) {
                                        lore.add(prefix + s);
                                    }
                                    if (end) break;
                                }
                            }
                            break;
                        }
                        lore.add(key);
                        break;
                    }
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
        return Util.modify(icon, nbt -> nbt.setBoolean(ItemStackUtil.FLAG, true));
    }

    public static IconSlot loadSlot(ConfigurationSection section, String key, AbstractMenuConfig.Icon base) {
        IconSlot icon = new IconSlot(base);
        ConfigurationSection section1 = section.getConfigurationSection(key + ".lore-parts");
        if (section1 != null) for (String k : section1.getKeys(false)) {
            icon.loreParts.put(k, section1.getStringList(k));
        }
        icon.loreContent = section.getStringList(key + ".lore-content");
        String outdateTimeFormat = section.getString(key + ".lore-format.attachments_outdate.format", "yyyy年MM月dd日 HH:mm:ss");
        try {
            icon.formatter = DateTimeFormatter.ofPattern(outdateTimeFormat);
        } catch (Throwable t) {
            icon.formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        }
        icon.attachmentOutdateTime = section.getStringList(key + ".lore-format.attachments_outdate.time");
        icon.attachmentOutdateInfinite = section.getStringList(key + ".lore-format.attachments_outdate.infinite");
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
