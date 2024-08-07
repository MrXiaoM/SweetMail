package top.mrxiaom.sweetmail.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.func.data.MailIcon;

import java.io.File;
import java.util.*;

public class DraftManager extends AbstractPluginHolder {

    public final File dataFolder;
    private final Map<String, Draft> draftMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, MailIcon> mailIcons = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private String defaultTitle;
    private final Set<String> advReceiversBlackList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public DraftManager(SweetMail plugin) {
        super(plugin);
        dataFolder = new File(plugin.getDataFolder(), "draft");
        register();
    }

    public String defaultTitle() {
        return defaultTitle;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        config.addDefault("preset-icons", null);
        draftMap.clear();
        mailIcons.clear();
        ConfigurationSection section = config.getConfigurationSection("preset-icons");
        if (section != null) for (String key : section.getKeys(false)) {
            String display = section.getString(key + ".display", key);
            String item = section.getString(key + ".item");
            if (item != null) {
                mailIcons.put(key, new MailIcon(display, item));
            }
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

    @Nullable
    public MailIcon getMailIcon(String key) {
        if (key.startsWith("!")) return null;
        MailIcon s = mailIcons.get(key);
        if (s == null) {
            s = mailIcons.get("default");
            if (s == null) {
                return new MailIcon(null, "PAPER");
            }
        }
        return s;
    }

    public Map<String, MailIcon> getMailIcons() {
        return mailIcons;
    }

    public boolean isInAdvanceReceiversBlackList(String player) {
        return advReceiversBlackList.contains(player);
    }

    public static DraftManager inst() {
        return get(DraftManager.class).orElseThrow(IllegalStateException::new);
    }
}
