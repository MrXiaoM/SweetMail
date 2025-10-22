package top.mrxiaom.sweetmail.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.Util;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteDatabase extends AbstractSQLDatabase {
    private final SweetMail plugin;
    private HikariDataSource dataSource = null;
    private boolean ok;
    private IStatementSchema schema;
    public SQLiteDatabase(SweetMail plugin) {
        this.plugin = plugin;
    }

    @Override
    protected IStatementSchema schema() {
        return schema;
    }

    private String decideDriver(MemoryConfiguration config) {
        String driver = config.getString("database.driver", "org.sqlite.JDBC");
        if (!Util.isPresent(driver)) {
            plugin.warn("预料中的错误: 未找到 SQLite JDBC: " + driver);
            plugin.warn("正在卸载插件，请在 database.yml 添加以下内容，并重启服务器");
            plugin.warn("extra-libraries:");
            plugin.warn("- 'org.xerial:sqlite-jdbc:3.49.0.0'");
            plugin.warn("- 'org.slf4j:slf4j-api:1.7.36'");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return null;
        }
        return checkDriver(driver);
    }

    @Override
    public void reload(MemoryConfiguration config) {
        onDisable();

        String driver = decideDriver(config);
        if (driver == null) return;

        schema = StatementSchemaWithAs.INSTANCE;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driver);
        hikariConfig.setAutoCommit(true);
        hikariConfig.setMaxLifetime(120000L);
        hikariConfig.setIdleTimeout(10000L);
        hikariConfig.setConnectionTimeout(5000L);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setJdbcUrl("jdbc:sqlite:plugins/" + plugin.getDescription().getName() + "/mail.db");
        hikariConfig.setConnectionTestQuery("SELECT CURRENT_TIMESTAMP;");

        String prefix = config.getString("database.table_prefix", "sweetmail_");
        TABLE_BOX = prefix + "box";
        TABLE_STATUS = prefix + "status";
        dataSource = new HikariDataSource(hikariConfig);

        createTables();
        ok = true;
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public boolean ok() {
        return ok;
    }

    @Override
    protected String insertStatusSentence(boolean attachments) {
        int used = attachments ? 0 : 1;
        return "INSERT OR REPLACE INTO `" + TABLE_STATUS + "`(`uuid`,`receiver`,`read`,`used`) VALUES(?, ?, 0, " + used + ");";
    }

    @Override
    public void onDisable() {
        ok = false;
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

}
