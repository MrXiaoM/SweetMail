package top.mrxiaom.sweetmail.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.database.IMailDatabase;
import top.mrxiaom.sweetmail.utils.Util;

import java.sql.Connection;

public class MySQLDatabase implements IMailDatabase {
    HikariDataSource dataSource = null;
    String prefix;
    @Nullable
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (Throwable t) {
            SweetMail.getInstance().getLogger().warning(Util.stackTraceToString(t));
            return null;
        }
    }
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
        prefix = config.getString("database.table_prefix", "sweetmail_");
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&verifyServerCertificate=false&serverTimezone=UTC");
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(pass);
        dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void onDisable() {

    }
}
