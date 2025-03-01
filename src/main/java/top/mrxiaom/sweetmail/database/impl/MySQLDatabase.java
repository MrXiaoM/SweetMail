package top.mrxiaom.sweetmail.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweetmail.SweetMail;
import top.mrxiaom.sweetmail.utils.Util;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabase extends AbstractSQLDatabase {
    private final SweetMail plugin;
    private HikariDataSource dataSource = null;
    private boolean ok = false;
    private IStatementSchema schema;
    public MySQLDatabase(SweetMail plugin) {
        this.plugin = plugin;
    }

    @Override
    protected IStatementSchema schema() {
        return schema;
    }

    @Nullable
    private String decideDriver(MemoryConfiguration config, int mysqlVersion) {
        String driverClass;
        if (mysqlVersion == 8) {
            driverClass = config.getString("database.driver", "com.mysql.cj.jdbc.Driver");
            if (!Util.isPresent(driverClass)) {
                plugin.warn("预料中的错误: 未找到 MySQL JDBC 8: " + driverClass);
                plugin.warn("正在卸载插件，请手动下载以下依赖，放到 plugins/SweetMail/libraries/ 文件夹，并重启服务器");
                plugin.warn("https://mirrors.huaweicloud.com/repository/maven/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar");
                Bukkit.getPluginManager().disablePlugin(plugin);
                return null;
            }
        } else {
            driverClass = config.getString("database.driver", "com.mysql.jdbc.Driver");
            if (!Util.isPresent(driverClass)) {
                plugin.warn("预料中的错误: 未找到 MySQL JDBC 5: " + driverClass);
                plugin.warn("正在卸载插件，请手动下载以下依赖，放到 plugins/SweetMail/libraries/ 文件夹，并重启服务器");
                plugin.warn("https://mirrors.huaweicloud.com/repository/maven/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar");
                Bukkit.getPluginManager().disablePlugin(plugin);
                return null;
            }
        }
        return checkDriver(driverClass);
    }

    @Override
    public void reload(MemoryConfiguration config) {
        onDisable();
        int mysqlVersion = config.getInt("database.mysql_version", 8);

        String driver = decideDriver(config, mysqlVersion);
        if (driver == null) return;
        if (mysqlVersion == 8) {
            schema = StatementSchemaWithAs.INSTANCE;
        } else {
            // TODO: 不使用 WITH AS 语法，重写数据库语句
            schema = StatementSchemaLegacy.INSTANCE;
            plugin.getLogger().warning("TODO: 本插件使用了 WITH AS 语句，暂不支持 MySQL 5.x");
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driver);
        plugin.getLogger().info("使用数据库驱动 " + hikariConfig.getDriverClassName());
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
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&verifyServerCertificate=false&serverTimezone=UTC");
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(pass);
        hikariConfig.setConnectionTestQuery("SELECT NOW();");

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
        return "INSERT INTO `" + TABLE_STATUS + "`(`uuid`,`receiver`,`read`,`used`) VALUES(?, ?, 0, " + used + ") on duplicate key update `read`=0, `used`=" + used + ";";
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
