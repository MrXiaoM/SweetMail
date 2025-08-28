package top.mrxiaom.sweetmail.func;

import com.google.common.collect.Lists;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.data.Draft;
import top.mrxiaom.sweetmail.func.data.TimedDraft;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static top.mrxiaom.sweetmail.utils.Util.toTimestamp;

public class TimerManager extends AbstractPluginHolder {
    private final File file;
    private final Map<String, TimedDraft> queue = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd-HHmmss");
    public TimerManager(SweetMail plugin) {
        super(plugin);
        file = new File(plugin.getDataFolder(), "timed_draft.yml");
        plugin.getScheduler().runTimer(this::everySecond, 20L, 20L);
        register();
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        YamlConfiguration config = Util.load(file);
        ConfigurationSection queueSection = config.getConfigurationSection("queue");
        if (queueSection != null) for (String id : queueSection.getKeys(false)) {
            TimedDraft temp = TimedDraft.loadFromConfig(queueSection, id);
            if (temp != null) {
                queue.put(id, temp);
            }
        }
    }

    private String generateId() {
        String base = LocalDateTime.now().format(formatter);
        String id;
        int i = 0;
        do {
            id = base + "-" + ++i;
        } while (queue.containsKey(id));
        return id;
    }

    public List<String> getQueueIds() {
        return Lists.newArrayList(queue.keySet());
    }

    public TimedDraft getQueue(String id) {
        return queue.get(id);
    }

    public boolean cancelQueue(String id) {
        return queue.remove(id) != null;
    }

    public String sendInTime(Draft draft, long timestamp) {
        String id = generateId();
        TimedDraft temp = TimedDraft.createFromDraft(id, draft, timestamp);
        queue.put(id, temp);
        save();
        return id;
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection queueSection = config.createSection("queue");
        for (TimedDraft temp : queue.values()) {
            temp.saveToConfig(queueSection);
        }
        try {
            Util.save(config, file);
        } catch (IOException e) {
            warn(e);
        }
    }

    private void everySecond() {
        if (queue.isEmpty()) return;
        plugin.getScheduler().runAsync((t_) -> {
            boolean flag = false;
            long time = toTimestamp(LocalDateTime.now());
            DraftManager manager = DraftManager.inst();
            for (TimedDraft temp : Lists.newArrayList(queue.values())) {
                if (temp.isOutOfTime(time)) {
                    cancelQueue(temp.id);
                    flag = true;
                    List<String> receivers = manager.generateReceivers(temp.draft);
                    if (receivers.isEmpty()) {
                        warn("定时邮件 " + temp.id + " (by " + temp.senderName + "#" + temp.senderUUID + ") 发送失败: 接收者列表为空");
                        continue;
                    }
                    String uuid = plugin.getMailDatabase().generateMailUUID();
                    Mail mail = temp.draft.createMail(uuid, receivers);
                    plugin.getMailDatabase().sendMail(mail);
                }
            }
            if (flag) save();
        });
    }

    public static TimerManager inst() {
        return instanceOf(TimerManager.class);
    }
}
