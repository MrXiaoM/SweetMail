package top.mrxiaom.sweetmail.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.IMailDatabase;
import top.mrxiaom.sweetmail.database.entry.Mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase implements IMailDatabase {
    HikariDataSource dataSource = null;
    String TABLE_BOX, TABLE_STATUS;
    public void reload(MemoryConfiguration config) {
        if (dataSource != null) dataSource.close();
        String type = config.getString("database.type", "file");
        if (!type.equalsIgnoreCase("mysql")) return;
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setAutoCommit(true);
        hikariConfig.setMaxLifetime(120000L);
        hikariConfig.setIdleTimeout(5000L);
        hikariConfig.setConnectionTimeout(5000L);
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setMaximumPoolSize(100);
        String host = config.getString("database.host", "localhost");
        int port = config.getInt("database.port", 3306);
        String user = config.getString("database.user", "root");
        String pass = config.getString("database.pass", "root");
        String database = config.getString("database.database", "minecraft");
        String prefix = config.getString("database.table_prefix", "sweetmail_");
        TABLE_BOX = prefix + "box";
        TABLE_STATUS = prefix + "status";
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&verifyServerCertificate=false&serverTimezone=UTC");
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(pass);
        dataSource = new HikariDataSource(hikariConfig);

        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE if NOT EXISTS `" + TABLE_BOX + "`(" +
                            "`uuid` VARCHAR(32) PRIMARY KEY," +
                            "`sender` VARCHAR(32)," +
                            "`data` MEDIUMBLOB," +
                            "`time` TIMESTAMP" +
                    ");" +
                    "CREATE TABLE if NOT EXISTS `" + TABLE_STATUS + "`(" +
                            "`uuid` VARCHAR(32)," +
                            "`receiver` VARCHAR(32)," +
                            "`read` TINYINT(1)," +
                            "`used` TINYINT(1)," +
                            "PRIMARY KEY(`uuid`, `receiver`)" +
                    ");");
            ps.execute();
        } catch (SQLException e) {
            SweetMail.warn(e);
        }
    }

    @Override
    public void sendMail(Mail mail) {
        try {
            Connection conn = dataSource.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO `" + TABLE_BOX + "`(`uuid`,`sender`,`data`,`time`) VALUES(?, ?, ?, NOW());")) {
                ps.setString(1, mail.uuid);
                ps.setString(2, mail.sender);
                byte[] bytes = mail.serialize().getBytes(StandardCharsets.UTF_8);
                try (InputStream in = new ByteArrayInputStream(bytes)) {
                    ps.setBinaryStream(3, in);
                    ps.execute();
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO `" + TABLE_STATUS + "`(`uuid`,`receiver`,`read`,`used`) VALUES(?, ?, 0, 0);")) {
                for (String receiver : mail.receivers) {
                    ps.setString(1, mail.uuid);
                    ps.setString(2, receiver);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            mail.noticeSent();
        } catch (SQLException | IOException e) {
            SweetMail.warn(e);
        }
    }

    @Override
    public List<Mail> getOutBox(String player, int page, int perPage) {
        List<Mail> mailList = new ArrayList<>();
        try {
            int offset = (page - 1) * perPage;
            Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM " +
                    "(`" + TABLE_STATUS + "` NATURAL JOIN (SELECT * FROM `" + TABLE_BOX + "` WHERE `sender` = ?) as A) " +
                    "LIMIT " + offset + ", " + perPage + " " +
                    "ORDER BY `time` DESC;");
            ps.setString(1, player);
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    mailList.add(resolveResult(result));
                }
            }
        } catch (SQLException | IOException e) {
            SweetMail.warn(e);
        }
        return mailList;
    }

    @Override
    public List<Mail> getInBox(boolean unread, String player, int page, int perPage) {
        List<Mail> mailList = new ArrayList<>();
        try {
            int offset = (page - 1) * perPage;
            Connection conn = dataSource.getConnection();
            String conditions = unread
                    ? "`receiver` = ? AND unread = 1"
                    : "`receiver` = ?";
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM " +
                    "(`" + TABLE_BOX + "` NATURAL JOIN (SELECT * FROM `" + TABLE_STATUS + "` WHERE " + conditions + ") as A) " +
                    "LIMIT " + offset + ", " + perPage + " " +
                    "ORDER BY `time` DESC;");
            ps.setString(1, player);
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    mailList.add(resolveResult(result));
                }
            }
        } catch (SQLException | IOException e) {
            SweetMail.warn(e);
        }
        return mailList;
    }

    private Mail resolveResult(ResultSet result) throws IOException, SQLException {
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
        Timestamp time = result.getTimestamp("time");
        Mail mail = Mail.deserialize(dataJson);
        mail.time = time.toLocalDateTime();
        mail.read = result.getInt("read") == 1;
        mail.used = result.getInt("used") == 1;
        return mail;
    }

    @Override
    public void markRead(String uuid, String receiver) {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE `" + TABLE_STATUS + "` " +
                    "SET `read` = 1" +
                    "WHERE `uuid` = ? AND `receiver` = ?;");
            ps.setString(1, uuid);
            ps.setString(2, receiver);
            ps.execute();
        } catch (SQLException e) {
            SweetMail.warn(e);
        }
    }

    @Override
    public void markUsed(String uuid, String receiver) {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE `" + TABLE_STATUS + "` " +
                    "SET `used` = 1" +
                    "WHERE `uuid` = ? AND `receiver` = ?;");
            ps.setString(1, uuid);
            ps.setString(2, receiver);
            ps.execute();
        } catch (SQLException e) {
            SweetMail.warn(e);
        }
    }

    @Override
    public void onDisable() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
