package top.mrxiaom.sweetmail.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.impl.FileDatabase;
import top.mrxiaom.sweetmail.database.impl.MySQLDatabase;
import top.mrxiaom.sweetmail.func.AbstractPluginHolder;

import java.io.File;

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
        if (database != null) database.onDisable();
    }
}
