package top.mrxiaom.sweetmail.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.MemoryConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabase extends AbstractSQLDatabase {
    HikariDataSource dataSource = null;
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

        createTables();
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void onDisable() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
