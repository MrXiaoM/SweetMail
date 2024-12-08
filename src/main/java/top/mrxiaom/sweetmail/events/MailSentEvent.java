package top.mrxiaom.sweetmail.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.database.entry.Mail;

/**
 * 任意邮件发送后触发该事件
 */
public class MailSentEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    private final Mail mail;
    public MailSentEvent(Mail mail) {
        this.mail = mail;
    }

    public Mail getMail() {
        return mail;
    }
}
