package top.mrxiaom.sweetmail.database;

import org.bukkit.configuration.MemoryConfiguration;

public interface IMailDatabaseReloadable extends IMailDatabase{
    void reload(MemoryConfiguration config);
    void onDisable();
}
