package top.mrxiaom.sweetmail.database;

import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;

import java.util.List;

public interface IMailDatabaseReloadable extends IMailDatabase{
    void reload(MemoryConfiguration config);
    void onDisable();
}
