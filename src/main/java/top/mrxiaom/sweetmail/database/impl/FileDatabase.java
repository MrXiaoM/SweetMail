package top.mrxiaom.sweetmail.database.impl;

import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.database.IMailDatabase;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;

import java.util.List;

public class FileDatabase implements IMailDatabase {
    @Override
    public void sendMail(Mail mail) {
        
    }

    @Override
    public List<MailWithStatus> getOutBox(String player, int page, int perPage) {
        return null;
    }

    @Override
    public List<MailWithStatus> getInBox(boolean unread, String player, int page, int perPage) {
        return null;
    }

    @Override
    public List<MailWithStatus> getInBoxUnused(String player) {
        return null;
    }

    @Override
    public void markRead(String uuid, String receiver) {

    }

    @Override
    public void markAllRead(String receiver) {

    }

    @Override
    public void markUsed(List<String> uuidList, String receiver) {

    }

    public void reload(MemoryConfiguration config) {
        String type = config.getString("database.type", "file");
        if (!type.equalsIgnoreCase("file")) return;
    }

    @Override
    public void onDisable() {

    }
}
