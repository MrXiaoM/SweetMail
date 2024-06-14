package top.mrxiaom.sweetmail.database;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.database.impl.FileDatabase;
import top.mrxiaom.sweetmail.database.impl.MySQLDatabase;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class MailDatabase extends AbstractPluginHolder {
    File configFile;
    YamlConfiguration config;
    MySQLDatabase mysql = new MySQLDatabase();
    FileDatabase file = new FileDatabase();
    IMailDatabase database = null;
    public MailDatabase(SweetMail plugin) {
        super(plugin);
        this.configFile = new File(plugin.getDataFolder(), "database.yml");
        register();
    }

    public String generateMailUUID() {
        // TODO: 增加检查，确保唯一性
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * @see IMailDatabase#sendMail(Mail)
     */
    public void sendMail(Mail mail) {
        database.sendMail(mail);
    }

    /**
     * @see IMailDatabase#getOutBox(String, int, int)
     */
    public List<MailWithStatus> getOutBox(String player, int page, int perPage) {
        return database.getOutBox(player, page, perPage);
    }

    /**
     * @see IMailDatabase#getInBox(boolean, String, int, int)
     */
    public List<MailWithStatus> getInBox(boolean unread, String player, int page, int perPage) {
        return database.getInBox(unread, player, page, perPage);
    }

    /**
     * @see IMailDatabase#markRead(String, String)
     */
    public void markRead(String uuid, String receiver) {
        database.markRead(uuid, receiver);
    }

    /**
     * @see IMailDatabase#markUsed(String, String)
     */
    public void markUsed(String uuid, String receiver) {
        database.markUsed(uuid, receiver);
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!configFile.exists()) {
            plugin.saveResource("database.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        String type = config.getString("database.type", "file");
        if (database != null) database.onDisable();
        if (type.equalsIgnoreCase("mysql")) {
            database = mysql;
        } else if (type.equalsIgnoreCase("file")) {
            database = file;
        } else {
            database = file;
        }
        database.reload(config);
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.onDisable();
            database = null;
        }
    }
}
