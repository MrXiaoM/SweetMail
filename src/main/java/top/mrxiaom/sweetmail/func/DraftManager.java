package top.mrxiaom.sweetmail.func;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.func.data.MailIcon;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.File;
import java.util.*;

import static top.mrxiaom.sweetmail.utils.Util.mkdirs;

public class DraftManager extends AbstractPluginHolder {

    public final File dataFolder;
    private final Map<String, Draft> draftMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, MailIcon> mailIcons = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private String defaultTitle;
    private final Set<String> advReceiversBlackList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final List<String> allPlayers = new ArrayList<>();
    private WrappedTask bungeeTimer;
    public DraftManager(SweetMail plugin) {
        super(plugin);
        dataFolder = new File(plugin.getDataFolder(), "draft");
        register();
    }

    @Override
    public void onDisable() {
        if (bungeeTimer != null) {
            allPlayers.clear();
            bungeeTimer.cancel();
            bungeeTimer = null;
        }
    }

    public void receiveBungee(ByteArrayDataInput in) {
        if (bungeeTimer == null) return;
        try {
            if (in.readUTF().equals("ALL")) {
                allPlayers.clear();
                Collections.addAll(allPlayers, in.readUTF().split(", "));
            }
        } catch (Throwable t) {
            warn(t);
        }
    }

    public List<String> getAllPlayers() {
        return Collections.unmodifiableList(allPlayers);
    }

    public String defaultTitle() {
        return defaultTitle;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        if (!dataFolder.exists()) {
            mkdirs(dataFolder);
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

        onDisable();
        if (Bukkit.spigot().getConfig().getBoolean("settings.bungeecord", false)) {
            bungeeTimer = plugin.getScheduler().runTimer(() -> {
                Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                if (player == null) return;
                ByteArrayDataOutput out = Util.newDataOutput();
                out.writeUTF("PlayerList");
                out.writeUTF("ALL");
                player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            }, Bukkit.getOnlinePlayers().isEmpty() ? 200L : 10L, 60L);
        }
    }

    public Draft getDraft(Player player) {
        String key = plugin.getPlayerKey(player);
        Draft draft = draftMap.get(key);
        if (draft == null) {
            draft = Draft.load(this, key);
            draftMap.put(key, draft);
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

    public List<String> generateReceivers(Draft draft) {
        List<String> receivers = new ArrayList<>();
        if (draft.advReceivers != null && !draft.advReceivers.isEmpty()) {
            receivers.addAll(draft.advReceivers());
            receivers.removeIf(draft.manager::isInAdvanceReceiversBlackList);
        } else if (!draft.receiver.isEmpty()) {
            receivers.add(draft.receiver);
        }
        return receivers;
    }

    public static DraftManager inst() {
        return instanceOf(DraftManager.class);
    }
}
