package top.mrxiaom.sweetmail.func.data;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.attachments.IAttachment;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.DraftManager;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Draft {
    public final String sender;
    public String receiver = "";
    public String iconKey = "default";
    public String title;
    public List<String> content = new ArrayList<>();
    public List<IAttachment> attachments = new ArrayList<>();
    public String advSenderDisplay = null;
    public String advReceivers = null;
    public final DraftManager manager;
    private Draft(DraftManager manager, String sender) {
        this.manager = manager;
        this.title = manager.defaultTitle();
        this.sender = sender;
    }

    public void reset() {
        receiver = "";
        iconKey = "default";
        title = manager.defaultTitle();
        content = new ArrayList<>();
        attachments = new ArrayList<>();
        advSenderDisplay = null;
        advReceivers = null;
        save();
    }

    public static Draft loadFromConfig(DraftManager manager, ConfigurationSection config, String sender) {
        Draft draft = new Draft(manager, sender);
        draft.receiver = config.getString("receiver", "");
        draft.iconKey = config.getString("icon_key", "default");
        draft.title = config.getString("title", manager.defaultTitle());
        draft.content = config.getStringList("content");
        List<IAttachment> attachments = new ArrayList<>();
        List<String> list = config.getStringList("attachments");
        for (String s : list) {
            IAttachment attachment = IAttachment.deserialize(s);
            if (attachment != null) {
                attachments.add(attachment);
            }
        }
        draft.attachments = attachments;
        draft.advSenderDisplay = config.getString("advance.sender_display", null);
        draft.advReceivers = config.getString("advance.receivers", null);
        return draft;
    }

    public static Draft load(DraftManager manager, String player) {
        File file = new File(manager.dataFolder, player + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Draft draft = loadFromConfig(manager, config, player);
        if (!file.exists()) draft.save();
        return draft;
    }

    public void saveToConfig(ConfigurationSection config) {
        config.set("sender", sender);
        config.set("receiver", receiver);
        config.set("icon_key", iconKey);
        config.set("title", title);
        config.set("content", content);
        List<String> attachmentsList = new ArrayList<>();
        for (IAttachment attachment : attachments) {
            attachmentsList.add(attachment.serialize());
        }
        config.set("attachments", attachmentsList);
        if (advSenderDisplay != null) config.set("advance.sender_display", advSenderDisplay);
        if (advReceivers != null) config.set("advance.receivers", advReceivers);
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        saveToConfig(config);
        try {
            File file = new File(manager.dataFolder, sender + ".yml");
            config.save(file);
        } catch (Throwable t) {
            SweetMail.warn(t);
        }
    }

    public List<String> advReceivers() {
        return generateReceivers(advReceivers);
    }

    public Mail createMail(String uuid, List<String> realReceivers) {
        String senderDisplay = advSenderDisplay == null ? "" : advSenderDisplay;
        MailIcon icon = DraftManager.inst().getMailIcon(iconKey);
        String iconKeyMail = icon == null ? iconKey.substring(1) : icon.item;
        return new Mail(uuid, sender, senderDisplay, iconKeyMail, realReceivers, title, content, attachments);
    }

    public static List<String> generateReceivers(String advReceivers) {
        boolean online = SweetMail.getInstance().isOnlineMode();
        List<String> receivers = new ArrayList<>();
        // TODO: 解析 advance receivers
        if (advReceivers.equalsIgnoreCase("current online")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                receivers.add(player.getName());
            }
        }
        if (advReceivers.equalsIgnoreCase("current online bungeecord")) {
            // TODO: 从代理端获取玩家列表
        }
        if (advReceivers.startsWith("last played in ")) {
            Long timeRaw = Util.parseLong(advReceivers.substring(15)).orElse(null);
            if (timeRaw != null) {
                long time = System.currentTimeMillis() - timeRaw;
                List<OfflinePlayer> players = Util.getOfflinePlayers();
                players.removeIf(it -> it == null || it.getName() == null || it.getLastPlayed() > time);
                for (OfflinePlayer player : players) {
                    receivers.add(online ? player.getUniqueId().toString() : player.getName());
                }
            }
        }
        if (advReceivers.startsWith("last played from ")) {
            String str = advReceivers.substring(17);
            String[] split = str.contains(" to ") ? str.split(" to ", 2) : new String[] {advReceivers};
            if (split.length == 2) {
                Long fromTime = Util.parseLong(split[0]).orElse(null);
                Long toTime = Util.parseLong(split[1]).orElse(null);
                if (fromTime != null && toTime != null) {
                    List<OfflinePlayer> players = Util.getOfflinePlayers();
                    players.removeIf(it -> {
                        if (it == null || it.getName() == null) return true;
                        long lastPlayed = it.getLastPlayed();
                        return lastPlayed < fromTime || lastPlayed >= toTime;
                    });
                    for (OfflinePlayer player : players) {
                        receivers.add(online ? player.getUniqueId().toString() : player.getName());
                    }
                }
            }
        }
        return receivers;
    }
}
