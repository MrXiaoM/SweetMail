package top.mrxiaom.sweetmail.database;

import org.bukkit.configuration.MemoryConfiguration;

public interface IMailDatabase {
    void reload(MemoryConfiguration config);
    void onDisable();
}
