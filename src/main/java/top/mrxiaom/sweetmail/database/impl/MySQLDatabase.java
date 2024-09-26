package top.mrxiaom.sweetmail.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.Util;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabase extends AbstractSQLDatabase {
    HikariDataSource dataSource = null;
    public void reload(MemoryConfiguration config) {
        onDisable();

        if (!Util.isPresent("com.mysql.cj.jdbc.Driver")) {
            SweetMail.warn("预料中的错误: 未找到 MySQL JDBC: com.mysql.cj.jdbc.Driver");
            SweetMail.warn("正在卸载插件，请使用最新版 Spigot 或其衍生服务端");
            Bukkit.getPluginManager().disablePlugin(SweetMail.getInstance());
            return;
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setAutoCommit(true);
        hikariConfig.setMaxLifetime(120000L);
        hikariConfig.setIdleTimeout(10000L);
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

        createTables();
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    protected String insertStatusSentence() {
        return "INSERT INTO `" + TABLE_STATUS + "`(`uuid`,`receiver`,`read`,`used`) VALUES(?, ?, 0, 0) on duplicate key update `read`=0;";
    }

    @Override
    public void onDisable() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
