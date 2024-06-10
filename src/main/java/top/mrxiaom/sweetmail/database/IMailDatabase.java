package top.mrxiaom.sweetmail.database;

import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.database.entry.Mail;

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
    List<Mail> getOutBox(String player, int page, int perPage);

    /**
     * 拉取收件箱
     * @param unread true 为未读，false 为全部
     * @param player 玩家名
     * @param page 页码
     * @param perPage 每页数量
     * @return 邮件列表
     */
    List<Mail> getInBox(boolean unread, String player, int page, int perPage);

    /**
     * 将邮件标记为已读
     * @param uuid 邮件 UUID
     * @param receiver 接收邮件的玩家
     */
    void markRead(String uuid, String receiver);

    /**
     * 将邮件标记为已接收附件
     * @param uuid 邮件 UUID
     * @param receiver 接收邮件的玩家
     */
    void markUsed(String uuid, String receiver);

    void reload(MemoryConfiguration config);
    void onDisable();
}
