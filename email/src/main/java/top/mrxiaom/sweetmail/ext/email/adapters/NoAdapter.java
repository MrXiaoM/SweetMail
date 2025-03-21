package top.mrxiaom.sweetmail.ext.email.adapters;

import org.bukkit.OfflinePlayer;
import top.mrxiaom.sweetmail.database.entry.Mail;

import java.util.List;

public class NoAdapter implements IMailAdapter {
    public static final NoAdapter INSTANCE = new NoAdapter();
    private NoAdapter() {}

    @Override
    public void sendMailNotice(Mail mail, List<OfflinePlayer> players) {
    }
}
