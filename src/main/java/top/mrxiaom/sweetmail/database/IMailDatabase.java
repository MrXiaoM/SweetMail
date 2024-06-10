package top.mrxiaom.sweetmail.database;

import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.database.entry.Mail;

import java.util.List;

public interface IMailDatabase {

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
    void reload(MemoryConfiguration config);
    void onDisable();
}
