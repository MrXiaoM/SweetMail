package top.mrxiaom.sweetmail.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.func.data.Draft;

/**
 * 玩家在草稿界面点击发送邮件，发送成功后触发该事件。<br/>
 * 设置了自定义发件人名称的邮件（系统邮件）不会触发该事件。
 */
public class PlayerMailSentEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    private final Draft cloneDraft;
    private final Mail mail;
    public PlayerMailSentEvent(@NotNull Player who, Draft cloneDraft, Mail mail) {
        super(who);
        this.cloneDraft = cloneDraft;
        this.mail = mail;
    }

    public Draft getCloneDraft() {
        return cloneDraft;
    }

    public Mail getMail() {
        return mail;
    }
}
