package top.mrxiaom.sweetmail.database;

import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.utils.ListX;

import java.util.List;

public interface IMailDatabase {
    /**
     * 发送邮件
     * @param mail 邮件实例
     */
    void sendMail(Mail mail);

    /**
     * 拉取发件箱
     * @param player 玩家
     * @param page 页码
     * @param perPage 每页数量
     * @return 邮件列表
     */
    List<MailWithStatus> getOutBox(String player, int page, int perPage);

    /**
     * 拉取收件箱
     * @param unread true 为未读，false 为全部
     * @param player 玩家名
     * @param page 页码
     * @param perPage 每页数量
     * @return 邮件列表
     */
    ListX<MailWithStatus> getInBox(boolean unread, String player, int page, int perPage);

    List<MailWithStatus> getInBoxUnused(String player);

    /**
     * 将邮件标记为已读
     * @param uuid 邮件 UUID
     * @param receiver 接收邮件的玩家
     */
    void markRead(String uuid, String receiver);

    /**
     * 全部标为已读
     * @param receiver 接收邮件的玩家
     */
    void markAllRead(String receiver);

    /**
     * 将邮件标记为已接收附件
     * @param uuidList 邮件 UUID 列表
     * @param receiver 接收邮件的玩家
     */
    void markUsed(List<String> uuidList, String receiver);
}
