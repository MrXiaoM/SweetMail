package top.mrxiaom.sweetmail.database.impl;

import top.mrxiaom.sweetmail.database.entry.MailWithStatus;
import top.mrxiaom.sweetmail.utils.ListX;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IStatementSchema {
    void getOutBox(Connection conn, String tableBox, ListX<MailWithStatus> mailList, String player, int page, int perPage) throws SQLException, IOException;
    void getInBox(Connection conn, String tableBox, String tableStatus, ListX<MailWithStatus> mailList, boolean unread, String player, int page, int perPage) throws SQLException, IOException;
    void getInBoxUnused(Connection conn, String tableBox, String tableStatus, List<MailWithStatus> mailList, String player) throws SQLException, IOException;
}
