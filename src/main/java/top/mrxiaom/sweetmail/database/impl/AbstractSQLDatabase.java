package top.mrxiaom.sweetmail.database.impl;

import org.bukkit.Bukkit;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.IMailDatabaseReloadable;
import top.mrxiaom.sweetmail.database.entry.Mail;
import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.utils.ListX;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class AbstractSQLDatabase implements IMailDatabaseReloadable {
    protected String TABLE_BOX, TABLE_STATUS;
    protected abstract Connection getConnection() throws SQLException;

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
            SweetMail.warn(e);
        }
    }

    protected abstract String insertStatusSentence();

    @Override
    public void sendMail(Mail mail) {
        Bukkit.getScheduler().runTaskAsynchronously(SweetMail.getInstance(), () -> {
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
                try (PreparedStatement ps = conn.prepareStatement(insertStatusSentence())) {
                    for (String receiver : new HashSet<>(mail.receivers)) {
                        ps.setString(1, mail.uuid);
                        ps.setString(2, receiver);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                mail.noticeSent();
            } catch (SQLException e) {
                SweetMail.warn(e);
            }
        });
    }

    @Override
    public ListX<MailWithStatus> getOutBox(String player, int page, int perPage) {
        ListX<MailWithStatus> mailList = new ListX<>();
        try (Connection conn = getConnection()) {
            int offset = (page - 1) * perPage;
            try (PreparedStatement ps = conn.prepareStatement("WITH join_result AS (" +
                    "  SELECT * FROM `" + TABLE_BOX + "` WHERE `sender` = ?" +
                    ")" +
                    "SELECT * FROM (join_result JOIN (SELECT count(*) AS 'mail_count' FROM join_result) AS C) " +
                    "ORDER BY `time` DESC " +
                    "LIMIT " + offset + ", " + perPage + ";"
            )) {
                ps.setString(1, player);
                try (ResultSet result = ps.executeQuery()) {
                    while (result.next()) {
                        mailList.add(resolveResult(result, true));
                    }
                }
            }
        } catch (SQLException | IOException e) {
            SweetMail.warn(e);
        }
        return mailList;
    }

    @Override
    public ListX<MailWithStatus> getInBox(boolean unread, String player, int page, int perPage) {
        ListX<MailWithStatus> mailList = new ListX<>();
        try (Connection conn = getConnection()) {
            int offset = (page - 1) * perPage;
            String conditions = unread
                    ? "`receiver` = ? AND (`read` = 0 OR `used` = 0)"
                    : "`receiver` = ?";
            try (PreparedStatement ps = conn.prepareStatement("WITH join_result AS (" +
                    "  SELECT A.`uuid`, `sender`, `data`, `time`, `receiver`, `read`, `used` FROM (" +
                    "    `" + TABLE_BOX + "` AS A" +
                    "    JOIN" +
                    "    (SELECT * FROM `" + TABLE_STATUS + "` WHERE " + conditions + ") AS B" +
                    "    ON A.`uuid` = B.`uuid`" +
                    "  )" +
                    ")" +
                    "SELECT * FROM (join_result JOIN (SELECT count(*) AS 'mail_count' FROM join_result) AS C) " +
                    "ORDER BY `used` ASC, `time` DESC " +
                    "LIMIT " + offset + ", " + perPage + ";"
            )) {
                ps.setString(1, player);
                try (ResultSet result = ps.executeQuery()) {
                    while (result.next()) {
                        mailList.add(resolveResult(result, false));
                        if (mailList.getMaxCount() == 0) {
                            mailList.setMaxCount(result.getInt("mail_count"));
                        }
                    }
                }
            }
        } catch (SQLException | IOException e) {
            SweetMail.warn(e);
        }
        return mailList;
    }

    @Override
    public List<MailWithStatus> getInBoxUnused(String player) {
        List<MailWithStatus> mailList = new ArrayList<>();
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM (" +
                    "  `" + TABLE_BOX + "` as A" +
                    "  JOIN" +
                    "  (SELECT * FROM `" + TABLE_STATUS + "` WHERE `receiver` = ? AND `used` = 0) as B" +
                    "  ON A.`uuid` = B.`uuid`" +
                    ") " +
                    "ORDER BY `time` DESC;")) {
                ps.setString(1, player);
                try (ResultSet result = ps.executeQuery()) {
                    while (result.next()) {
                        mailList.add(resolveResult(result, false));
                    }
                }
            }
        } catch (SQLException | IOException e) {
            SweetMail.warn(e);
        }
        return mailList;
    }

    private MailWithStatus resolveResult(ResultSet result, boolean outbox) throws IOException, SQLException {
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
            SweetMail.warn(e);
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
            SweetMail.warn(e);
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
            SweetMail.warn(e);
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
            SweetMail.warn(e);
        }
    }
}
