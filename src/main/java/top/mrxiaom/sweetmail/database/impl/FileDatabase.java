package top.mrxiaom.sweetmail.database.impl;

import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.database.IMailDatabase;

public class FileDatabase implements IMailDatabase {
    public void reload(MemoryConfiguration config) {
        String type = config.getString("database.type", "file");
        if (!type.equalsIgnoreCase("file")) return;
    }

    @Override
    public void onDisable() {

    }
}
