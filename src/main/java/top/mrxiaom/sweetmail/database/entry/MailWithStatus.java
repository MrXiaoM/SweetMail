package top.mrxiaom.sweetmail.database.entry;

import top.mrxiaom.sweetmail.attachments.IAttachment;

import java.time.LocalDateTime;
import java.util.List;

public class MailWithStatus extends Mail {
    public LocalDateTime time;
    public boolean read = false;
    public boolean used = false;
    public MailWithStatus(String uuid, String sender, String senderDisplay, String icon, List<String> receivers, String title, List<String> content, List<IAttachment> attachments) {
        super(uuid, sender, senderDisplay, icon, receivers, title, content, attachments);
    }
}
