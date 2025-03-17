package top.mrxiaom.sweetmail.database;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.database.entry.MailCountInfo;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.database.impl.MySQLDatabase;
import top.mrxiaom.sweetmail.database.impl.SQLiteDatabase;
import top.mrxiaom.sweetmail.events.MailSentEvent;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;
import top.mrxiaom.sweetmail.utils.ListX;

import java.io.File;
import java.util.*;

public class MailDatabase extends AbstractPluginHolder implements IMailDatabase {
    private final File configFile;
    YamlConfiguration config;
    private final MySQLDatabase mysql;
    private final SQLiteDatabase sqlite;
    private final IMailDatabaseReloadable[] databases;
    IMailDatabaseReloadable database = null;
    private final Set<String> canUsePlayers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<UUID, MailCountInfo> cachedCountInfo = new HashMap<>();
    public MailDatabase(SweetMail plugin) {
        super(plugin);
        mysql = new MySQLDatabase(plugin);
        sqlite = new SQLiteDatabase(plugin);
        databases = new IMailDatabaseReloadable[] { mysql, sqlite };
        this.configFile = new File(plugin.getDataFolder(), "database.yml");
        register();
    }

    public boolean hasUnUsed(String player) {
        return canUsePlayers.contains(player);
    }

    public boolean ok() {
        return database != null && database.ok();
    }

    public String generateMailUUID() {
        // TODO: 增加检查，确保唯一性
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public void sendMail(Mail mail) {
        database.sendMail(mail);
        plugin.getScheduler().runNextTick((t_) -> {
            MailSentEvent event = new MailSentEvent(mail);
            Bukkit.getPluginManager().callEvent(event);
        });
    }

    @Override
    public ListX<MailWithStatus> getOutBox(String player, int page, int perPage) {
        if (player == null) {
            ListX<MailWithStatus> list = new ListX<>();
            list.setMaxCount(0);
            return list;
        }
        return database.getOutBox(player, page, perPage);
    }

    @Override
    public ListX<MailWithStatus> getInBox(boolean unread, String player, int page, int perPage) {
        if (player == null) {
            ListX<MailWithStatus> list = new ListX<>();
            list.setMaxCount(0);
            return list;
        }
        ListX<MailWithStatus> inBox = database.getInBox(unread, player, page, perPage);
        boolean flag = false;
        for (MailWithStatus mail : inBox) {
            if (!mail.used) {
                flag = true;
                break;
            }
        }
        if (flag) {
            canUsePlayers.add(player);
        }
        else {
            canUsePlayers.remove(player);
        }
        return inBox;
    }

    @Override
    public List<MailWithStatus> getInBoxUnused(String player) {
        if (player == null) return new ArrayList<>();
        List<MailWithStatus> inBox = database.getInBoxUnused(player);
        if (!inBox.isEmpty()) {
            canUsePlayers.add(player);
        } else {
            canUsePlayers.remove(player);
        }
        return inBox;
    }

    @Override
    public MailCountInfo getInBoxCount(String player) {
        if (player == null) return MailCountInfo.ZERO;
        return database.getInBoxCount(player);
    }

    @NotNull
    public MailCountInfo getInBoxCount(Player player) {
        return getInBoxCount(player, false);
    }
    @NotNull
    public MailCountInfo getInBoxCount(Player player, boolean refresh) {
        UUID uuid = player.getUniqueId();
        MailCountInfo cached = cachedCountInfo.get(uuid);
        if (cached != null) return cached;
        MailCountInfo info = getInBoxCount(plugin.getPlayerKey(player));
        cachedCountInfo.put(uuid, info);
        return info;
    }

    @Override
    public void markRead(String uuid, String receiver) {
        if (receiver == null) return;
        database.markRead(uuid, receiver);
    }

    @Override
    public void markAllRead(String receiver) {
        if (receiver == null) return;
        database.markAllRead(receiver);
    }

    @Override
    public void markUsed(List<String> uuidList, String receiver) {
        if (receiver == null) return;
        database.markUsed(uuidList, receiver);
    }

    @Override
    public void deleteMail(String uuid) {
        database.deleteMail(uuid);
    }

    public MailDatabase reload() {
        if (!configFile.exists()) {
            plugin.saveResource("database.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        String type = config.getString("database.type", "sqlite").toLowerCase();

        for (IMailDatabaseReloadable db : databases) db.onDisable();
        switch (type) {
            case "mysql":
                database = mysql;
                break;
            case "sqlite":
            default:
                database = sqlite;
                break;
        }
        database.reload(config);
        return this;
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.onDisable();
            database = null;
        }
    }
}
