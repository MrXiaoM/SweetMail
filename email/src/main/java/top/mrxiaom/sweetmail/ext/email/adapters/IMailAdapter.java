package top.mrxiaom.sweetmail.ext.email.adapters;

import org.bukkit.OfflinePlayer;
import top.mrxiaom.sweetmail.database.entry.Mail;

import java.util.List;

public interface IMailAdapter {
    void sendMailNotice(Mail mail, List<OfflinePlayer> players);
}
