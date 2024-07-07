package top.mrxiaom.sweetmail.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.entry.IAttachment;

import java.io.File;
import java.util.*;

public class DraftManager extends AbstractPluginHolder {
    public static class Draft {
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
            this.title = manager.defaultTitle;
            this.sender = sender;
        }

        public void reset() {
            receiver = "";
            iconKey = "default";
            title = manager.defaultTitle;
            content.clear();
            attachments.clear();
            advSenderDisplay = advReceivers = null;
        }

        public static Draft load(DraftManager manager, String player) {
            File file = new File(manager.dataFolder, player + ".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Draft draft = new Draft(manager, player);
            draft.receiver = config.getString("receiver", "");
            draft.receiver = config.getString("icon_key", "default");
            draft.title = config.getString("title", manager.defaultTitle);
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
            if (!file.exists()) draft.save();
            return draft;
        }

        public void save() {
            YamlConfiguration config = new YamlConfiguration();
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
            try {
                File file = new File(manager.dataFolder, sender + ".yml");
                config.save(file);
            } catch (Throwable t) {
                SweetMail.warn(t);
            }
        }
    }
    private final File dataFolder;
    private final Map<String, Draft> draftMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, String> mailIcons = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private String defaultTitle;
    private final Set<String> advReceiversBlackList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public DraftManager(SweetMail plugin) {
        super(plugin);
        dataFolder = new File(plugin.getDataFolder(), "draft");
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        draftMap.clear();
        mailIcons.clear();
        ConfigurationSection section = config.getConfigurationSection("preset-icons");
        if (section != null) for (String key : section.getKeys(false)) {
            mailIcons.put(key, section.getString(key));
        }
        if (!mailIcons.containsKey("default")) {
            warn("[config.yml] preset-icons 缺少 default");
        }
        advReceiversBlackList.clear();
        advReceiversBlackList.addAll(config.getStringList("blacklist-players"));
        defaultTitle = config.getString("default.title", "未命名邮件");
    }

    public Draft getDraft(Player player) {
        Draft draft = draftMap.get(player.getName());
        if (draft == null) {
            draft = Draft.load(this, player.getName());
            draftMap.put(player.getName(), draft);
        }
        return draft;
    }

    public String getMailIcon(String key) {
        if (key.startsWith("!")) return key.substring(1);
        String s = mailIcons.get(key);
        return s == null ? mailIcons.getOrDefault("default", "PAPER") : s;
    }

    public Map<String, String> getMailIcons() {
        return mailIcons;
    }

    public boolean isInAdvanceReceiversBlackList(String player) {
        return advReceiversBlackList.contains(player);
    }

    public static DraftManager inst() {
        return get(DraftManager.class).orElseThrow(IllegalStateException::new);
    }
}
