package top.mrxiaom.sweetmail.database.impl;

import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.utils.ListX;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static top.mrxiaom.sweetmail.database.impl.AbstractSQLDatabase.resolveResult;

public class StatementSchemaLegacy implements IStatementSchema {
    public static final StatementSchemaLegacy INSTANCE = new StatementSchemaLegacy();
    private StatementSchemaLegacy() {}

    @Override
    public void getOutBox(Connection conn, String tableBox, ListX<MailWithStatus> mailList, String player, int page, int perPage) throws SQLException, IOException {
        int offset = (page - 1) * perPage;
        // TODO: 不使用 WITH AS 语法，重写数据库语句
        try (PreparedStatement ps = conn.prepareStatement(
                "WITH join_result AS (" +
                "  SELECT * FROM `" + tableBox + "` WHERE `sender` = ?" +
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
    }

    @Override
    public void getInBox(Connection conn, String tableBox, String tableStatus, ListX<MailWithStatus> mailList, boolean unread, String player, int page, int perPage) throws SQLException, IOException {
        int offset = (page - 1) * perPage;
        String conditions = unread
                ? "`receiver` = ? AND (`read` = 0 OR `used` = 0)"
                : "`receiver` = ?";
        // TODO: 不使用 WITH AS 语法，重写数据库语句
        try (PreparedStatement ps = conn.prepareStatement(
                "WITH join_result AS (" +
                "  SELECT A.`uuid`, `sender`, `data`, `time`, `receiver`, `read`, `used` FROM (" +
                "    `" + tableBox + "` AS A" +
                "    JOIN" +
                "    (SELECT * FROM `" + tableStatus + "` WHERE " + conditions + ") AS B" +
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
    }

    @Override
    public void getInBoxUnused(Connection conn, String tableBox, String tableStatus, List<MailWithStatus> mailList, String player) throws SQLException, IOException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM (" +
                "  `" + tableBox + "` as A" +
                "  JOIN" +
                "  (SELECT * FROM `" + tableStatus + "` WHERE `receiver` = ? AND `used` = 0) as B" +
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
    }
}
