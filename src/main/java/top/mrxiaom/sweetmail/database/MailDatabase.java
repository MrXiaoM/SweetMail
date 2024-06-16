package top.mrxiaom.sweetmail.database;

import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.database.impl.MySQLDatabase;
import top.mrxiaom.sweetmail.database.impl.SQLiteDatabase;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class MailDatabase extends AbstractPluginHolder implements IMailDatabase {
    File configFile;
    YamlConfiguration config;
    MySQLDatabase mysql = new MySQLDatabase();
    SQLiteDatabase sqlite = new SQLiteDatabase();
    IMailDatabaseReloadable[] databases = new IMailDatabaseReloadable[] {
            mysql, sqlite
    };
    IMailDatabaseReloadable database = null;
    public MailDatabase(SweetMail plugin) {
        super(plugin);
        this.configFile = new File(plugin.getDataFolder(), "database.yml");
        register();
    }

    public String generateMailUUID() {
        // TODO: 增加检查，确保唯一性
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public void sendMail(Mail mail) {
        database.sendMail(mail);
    }

    @Override
    public List<MailWithStatus> getOutBox(String player, int page, int perPage) {
        return database.getOutBox(player, page, perPage);
    }

    @Override
    public List<MailWithStatus> getInBox(boolean unread, String player, int page, int perPage) {
        return database.getInBox(unread, player, page, perPage);
    }

    @Override
    public List<MailWithStatus> getInBoxUnused(String player) {
        return database.getInBoxUnused(player);
    }

    @Override
    public void markRead(String uuid, String receiver) {
        database.markRead(uuid, receiver);
    }

    @Override
    public void markAllRead(String receiver) {
        database.markAllRead(receiver);
    }

    @Override
    public void markUsed(List<String> uuidList, String receiver) {
        database.markUsed(uuidList, receiver);
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
