package top.mrxiaom.sweetmail.ext.email.adapters;

import me.lagbug.emailer.global.EmailAddress;
import me.lagbug.emailer.global.EmailTemplate;
import me.lagbug.emailer.global.enums.EmailType;
import me.lagbug.emailer.spigot.Emailer;
import me.lagbug.emailer.spigot.utils.Email;
import me.lagbug.emailer.spigot.utils.PlayerDataCache;
import org.bukkit.OfflinePlayer;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.ext.email.EmailerTemplate;
import top.mrxiaom.sweetmail.ext.email.SweetMailNotice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdapterEmailer implements IMailAdapter {
    private final Emailer emailer = Emailer.getPlugin(Emailer.class);
    private final SweetMailNotice plugin;
    public AdapterEmailer(SweetMailNotice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sendMailNotice(Mail mail, List<OfflinePlayer> players) {
        EmailerTemplate template = plugin.getEmailerTemplate();
        if (template == null) {
            plugin.getLogger().warning("邮件模板配置异常");
            return;
        }
        Map<String, String> verified = PlayerDataCache.getVerifiedData();
        List<EmailAddress> recipients = new ArrayList<>();
        for (OfflinePlayer p : players) {
            if (verified.containsKey(p.getUniqueId().toString())) {
                recipients.add(new EmailAddress(verified.get(p.getUniqueId().toString())));
            }
        }
        if (recipients.isEmpty()) return;
        EmailAddress[] arr = recipients.toArray(new EmailAddress[0]);

        String subject = template.getSubject(mail);
        String html = template.save(plugin, mail);
        if (html == null) return;

        Email email = new Email(emailer.getSession(), emailer.getEmail(), arr);
        email.setTemplate(new EmailTemplate("SweetMail/" + mail.uuid, subject, EmailType.HTML, html, new ArrayList<>()));
        email.send();
    }
}
