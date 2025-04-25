package top.mrxiaom.sweetmail.database.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.IMailDatabaseReloadable;
import top.mrxiaom.sweetmail.database.entry.MailCountInfo;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.utils.ListX;
import top.mrxiaom.sweetmail.utils.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static top.mrxiaom.sweetmail.func.AbstractPluginHolder.t;

public abstract class AbstractSQLDatabase implements IMailDatabaseReloadable {
    protected String TABLE_BOX, TABLE_STATUS;
    protected abstract Connection getConnection() throws SQLException;
    protected abstract IStatementSchema schema();

    protected static String checkDriver(String driver) {
        return Util.isPresent(driver) ? driver : null;
    }

    protected void createTables() {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE if NOT EXISTS `" + TABLE_BOX + "`(" +
                            "`uuid` VARCHAR(32) PRIMARY KEY," +
                            "`sender` VARCHAR(32)," +
                            "`data` MEDIUMBLOB," +
                            "`time` TIMESTAMP" +
                    ");")) {
                ps.execute();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE if NOT EXISTS `" + TABLE_STATUS + "`(" +
                            "`uuid` VARCHAR(32)," +
                            "`receiver` VARCHAR(32)," +
                            "`read` TINYINT(1) DEFAULT 0," +
                            "`used` TINYINT(1) DEFAULT 0," +
                            "PRIMARY KEY(`uuid`, `receiver`)" +
                    ");")) {
                ps.execute();
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    protected abstract String insertStatusSentence(boolean attachments);

    @Override
    public void sendMail(Mail mail) {
        SweetMail.getInstance().getScheduler().runAsync((t_) -> {
            try (Connection conn = getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO `" + TABLE_BOX + "`(`uuid`,`sender`,`data`,`time`) VALUES(?, ?, ?, ?);"
                )) {
                    ps.setString(1, mail.uuid);
                    ps.setString(2, mail.sender);
                    byte[] bytes = mail.serialize().getBytes(StandardCharsets.UTF_8);
                    ps.setBytes(3, bytes);
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    ps.execute();
                }
                try (PreparedStatement ps = conn.prepareStatement(insertStatusSentence(!mail.attachments.isEmpty()))) {
                    for (String receiver : new HashSet<>(mail.receivers)) {
                        ps.setString(1, mail.uuid);
                        ps.setString(2, receiver);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                mail.noticeSent();
            } catch (SQLException e) {
                handleException(e);
            }
        });
    }

    @Override
    public ListX<MailWithStatus> getOutBox(String player, int page, int perPage) {
        ListX<MailWithStatus> mailList = new ListX<>();
        try (Connection conn = getConnection()) {
            schema().getOutBox(conn, TABLE_BOX, mailList, player, page, perPage);
        } catch (SQLException | IOException e) {
            handleException(e);
        }
        return mailList;
    }

    @Override
    public ListX<MailWithStatus> getInBox(boolean unread, String player, int page, int perPage) {
        ListX<MailWithStatus> mailList = new ListX<>();
        try (Connection conn = getConnection()) {
            schema().getInBox(conn, TABLE_BOX, TABLE_STATUS, mailList, unread, player, page, perPage);
        } catch (SQLException | IOException e) {
            handleException(e);
        }
        return mailList;
    }

    @Override
    public List<MailWithStatus> getInBoxUnused(String player) {
        List<MailWithStatus> mailList = new ArrayList<>();
        try (Connection conn = getConnection()) {
            schema().getInBoxUnused(conn, TABLE_BOX, TABLE_STATUS, mailList, player);
        } catch (SQLException | IOException e) {
            handleException(e);
        }
        return mailList;
    }


    public MailCountInfo getInBoxCount(String player) {
        try (Connection conn = getConnection()) {
            int totalCount, unreadCount, usedCount;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM `" + TABLE_STATUS + "` WHERE `receiver`=?"
            )) {
                ps.setString(1, player);
                try (ResultSet result = ps.executeQuery()) {
                    if (result.next()) {
                        totalCount = result.getInt(1);
                    } else {
                        totalCount = 0;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM `" + TABLE_STATUS + "` WHERE `receiver`=? AND (`read` = 0 OR `used` = 0)"
            )) {
                ps.setString(1, player);
                try (ResultSet result = ps.executeQuery()) {
                    if (result.next()) {
                        unreadCount = result.getInt(1);
                    } else {
                        unreadCount = 0;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM `" + TABLE_STATUS + "` WHERE `receiver`=? AND `used`=1"
            )) {
                ps.setString(1, player);
                try (ResultSet result = ps.executeQuery()) {
                    if (result.next()) {
                        usedCount = result.getInt(1);
                    } else {
                        usedCount = 0;
                    }
                }
            }
            return new MailCountInfo(unreadCount, usedCount, totalCount);
        } catch (SQLException e) {
            handleException(e);
        }
        return MailCountInfo.ZERO;
    }

    public static MailWithStatus resolveResult(ResultSet result, boolean outbox) throws IOException, SQLException {
        String dataJson;
        try (InputStream in = result.getBinaryStream("data")) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024 * 10];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                dataJson = new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
        }
        LocalDateTime time = result.getTimestamp("time").toLocalDateTime();
        boolean read = outbox || result.getInt("read") == 1;
        boolean used = outbox || result.getInt("used") == 1;
        return Mail.deserialize(dataJson, time, read, used);
    }

    @Override
    public void markRead(String uuid, String receiver) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE `" + TABLE_STATUS + "` " +
                    "SET `read` = 1 " +
                    "WHERE `uuid` = ? AND `receiver` = ?;")) {
                ps.setString(1, uuid);
                ps.setString(2, receiver);
                ps.execute();
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void markAllRead(String receiver) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE `" + TABLE_STATUS + "` " +
                    "SET `read` = 1 " +
                    "WHERE `receiver` = ?;")) {
                ps.setString(1, receiver);
                ps.execute();
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void markUsed(List<String> uuidList, String receiver) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE `" + TABLE_STATUS + "` " +
                    "SET `used` = 1, `read` = 1 " +
                    "WHERE `uuid` = ? AND `receiver` = ?;")) {
                for (String uuid : uuidList) {
                    ps.setString(1, uuid);
                    ps.setString(2, receiver);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void deleteMail(String uuid) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM `" + TABLE_BOX + "` WHERE `uuid`=?;"
            )) {
                ps.setString(1, uuid);
                ps.execute();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM `" + TABLE_STATUS + "` WHERE `uuid`=?;"
            )) {
                ps.setString(1, uuid);
                ps.execute();
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public void handleException(Exception e) {
        String message = e.getMessage();
        SweetMail plugin = SweetMail.getInstance();
        if (e instanceof SQLException && message.contains("[SQLITE_ERROR]") && message.contains("near ")) {
            ConsoleCommandSender sender = Bukkit.getConsoleSender();
            t(sender, "&7[&d&lSweetMail&7]&c " + message);
            t(sender, "&7[&d&lSweetMail&7]&e 服务器自带的 SQLite JDBC 版本过低，不支持执行插件需要的查询语句。");
            t(sender, "&7[&d&lSweetMail&7]&e 这个问题通常只会在低版本服务端（如 1.8）出现。");
            t(sender, "&7[&d&lSweetMail&7]&e 请从以下链接下载新版本，替换掉服务端默认的 SQLite JDBC");
            t(sender, "&7[&d&lSweetMail&7]&b https://mirrors.huaweicloud.com/repository/maven/org/xerial/sqlite-jdbc/3.49.0.0/sqlite-jdbc-3.49.0.0.jar");
            t(sender, "&7[&d&lSweetMail&7]&e 如果不会操作，请使用 MySQL 代替");
        } else {
            plugin.warn("执行数据库语句时出现异常", e);
        }
    }
}
