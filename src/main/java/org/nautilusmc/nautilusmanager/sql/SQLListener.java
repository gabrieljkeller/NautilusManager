package org.nautilusmc.nautilusmanager.sql;

import net.minecraft.stats.Stat;
import org.bukkit.Bukkit;
import org.nautilusmc.nautilusmanager.NautilusManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public abstract class SQLListener {

    private final String table;

    public SQLListener(String table) {
        this.table = table;

        Bukkit.getScheduler().runTaskTimerAsynchronously(NautilusManager.INSTANCE, () -> {
            if (!SQL.SQL_ENABLED) return;

            try {
                Connection conn = SQL.getConnection();
                Statement statement = conn.createStatement();

                updateSQL(statement.executeQuery("SELECT * FROM "+table));

                conn.close();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error updating SQL database", e);
            }
        }, 0, SQL.SQL_UPDATE_TIME * 20L);
    }

    public abstract void updateSQL(ResultSet results) throws SQLException;

    public void deleteSQL(UUID uuid) {
        deleteSQL(uuid.toString());
    }

    public void setSQL(UUID uuid, Map<String, Object> values) {
        setSQL(uuid.toString(), values);
    }

    public void deleteSQL(String uuid) {
        if (!SQL.SQL_ENABLED) return;

        try {
            Connection conn = SQL.getConnection();
            Statement statement = conn.createStatement();
            statement.executeUpdate("DELETE FROM "+table+" WHERE uuid='" + uuid + "'");
            conn.close();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error deleting from SQL database", e);
        }
    }

    public void setSQL(String uuid, Map<String, Object> values) {
        if (!SQL.SQL_ENABLED) return;

        StringBuilder command = new StringBuilder("INSERT INTO "+table+" (uuid,");

        for (String key : values.keySet()) command.append(key).append(",");
        command.deleteCharAt(command.length() - 1);

        command.append(") VALUES ('").append(uuid).append("',");

        for (Object value : values.values()) {
            if (value instanceof String) command.append("'").append(value).append("',");
            else command.append(value).append(",");
        }
        command.deleteCharAt(command.length() - 1);

        command.append(") ON DUPLICATE KEY UPDATE ");
        for (Map.Entry<String, Object> value : values.entrySet()) {
            command.append(value.getKey()).append("=");

            if (value.getValue() instanceof String) command.append("'").append(value.getValue()).append("',");
            else command.append(value.getValue()).append(",");
        }
        command.deleteCharAt(command.length() - 1);

        try {
            Connection conn = SQL.getConnection();
            Statement statement = conn.createStatement();
            statement.executeUpdate(command.toString());
            conn.close();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error inserting into SQL database", e);
        }
    }
}
