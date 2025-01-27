package org.nautilusmc.nautilusmanager.sql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.nautilusmc.nautilusmanager.NautilusManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class SQL {

    public static boolean SQL_ENABLED = false;
    protected static int SQL_UPDATE_TIME;

    private static final BasicDataSource DATABASE = new BasicDataSource();

    public static void init() {
        FileConfiguration config = NautilusManager.INSTANCE.getConfig();

        DATABASE.setUrl("jdbc:%s://%s:%s@%s:%d/%s".formatted(
                config.getString("sql.protocol"),
                config.getString("sql.username"),
                config.getString("sql.password"),
                config.getString("sql.host"),
                config.getInt("sql.port"),
                config.getString("sql.database")));
        DATABASE.setMaxWaitMillis(2000); // just error out if it can't connect

        if (DATABASE.isClosed()) {
            Bukkit.getLogger().log(Level.WARNING, "Can't connect to SQL server! Data will not be saved.");
        } else {
            SQL_ENABLED = true;
        }

        SQL_UPDATE_TIME = config.getInt("sql.update_interval");
    }

    public static Connection getConnection() throws SQLException {
        return DATABASE.getConnection();
    }
}
