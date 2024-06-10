package top.mrxiaom.sweetmail.database.impl;

import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.database.IMailDatabase;
import top.mrxiaom.sweetmail.database.entry.Mail;

import java.util.List;

public class FileDatabase implements IMailDatabase {
    @Override
    public List<Mail> getOutBox(String player, int page, int perPage) {
        return null;
    }

    @Override
    public List<Mail> getInBox(boolean unread, String player, int page, int perPage) {
        return null;
    }

    public void reload(MemoryConfiguration config) {
        String type = config.getString("database.type", "file");
        if (!type.equalsIgnoreCase("file")) return;
    }

    @Override
    public void onDisable() {

    }
}
