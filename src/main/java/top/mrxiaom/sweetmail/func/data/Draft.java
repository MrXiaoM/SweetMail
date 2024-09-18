package top.mrxiaom.sweetmail.func.data;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.sweetmail.IMail;
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
    public int outdateDays = 0;
    public Long lastEditTime = null;
    public final DraftManager manager;
    public Draft(DraftManager manager, String sender) {
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
        outdateDays = 0;
        lastEditTime = null;
        save();
    }

    public Draft deepClone() {
        Draft draft = new Draft(manager, sender);
        draft.receiver = receiver;
        draft.iconKey = iconKey;
        draft.title = title;
        if (!content.isEmpty()) draft.content.addAll(content);
        if (!attachments.isEmpty()) for (IAttachment attachment : attachments) {
            String serialize = attachment.serialize();
            IAttachment deserialize = IAttachment.deserialize(serialize);
            draft.attachments.add(deserialize);
        }
        draft.advSenderDisplay = advSenderDisplay;
        draft.advReceivers = advReceivers;
        draft.outdateDays = outdateDays;
        draft.lastEditTime = lastEditTime;
        return draft;
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
        draft.outdateDays = config.getInt("advance.outdate_days", 0);
        draft.lastEditTime = config.contains("last-edit") ? config.getLong("last-edit") : null;
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
        config.set("advance.outdate_days", outdateDays);
        if (lastEditTime != null) config.set("last-edit", lastEditTime);
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
        String sender = senderDisplay.isEmpty() ? this.sender : IMail.SERVER_SENDER;
        long outdateTime;
        if (outdateDays > 0) {
            outdateTime = System.currentTimeMillis() + outdateDays * 1000L * 3600L * 24L;
        } else {
            outdateTime = 0;
        }
        return new Mail(uuid, sender, senderDisplay, iconKeyMail, realReceivers, title, content, attachments, outdateTime);
    }

    /**
     * 解析 advance receivers
     */
    public static List<String> generateReceivers(String advReceivers) {
        boolean online = SweetMail.getInstance().isOnlineMode();
        List<String> receivers = new ArrayList<>();
        if (advReceivers.equalsIgnoreCase("current online")) {
            // 在线玩家列表
            for (Player player : Bukkit.getOnlinePlayers()) {
                receivers.add(player.getName());
            }
        }
        if (advReceivers.equalsIgnoreCase("current online bungeecord")) {
            // 从代理端获取在线玩家列表
            receivers.addAll(DraftManager.inst().getAllPlayers());
        }
        if (advReceivers.startsWith("last played in ")) {
            // 从什么时间到现在，上过线的玩家
            Long timeRaw = Util.parseLong(advReceivers.substring(15)).orElse(null);
            if (timeRaw != null) {
                List<OfflinePlayer> players = Util.getOfflinePlayers();
                players.removeIf(it -> it == null || it.getName() == null || it.getLastPlayed() > timeRaw);
                for (OfflinePlayer player : players) {
                    receivers.add(online ? player.getUniqueId().toString() : player.getName());
                }
            }
        }
        if (advReceivers.startsWith("last played from ")) {
            // 在某段时间区间内，上过线的玩家
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
