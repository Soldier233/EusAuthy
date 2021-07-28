package in.e23.eusauthy.sql;

import in.e23.eusauthy.EusAuthy;
import in.e23.eusauthy.object.PlayerData;
import lombok.Cleanup;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.UUID;

public class SQLite implements DataInterface {

    public static class Api {
        public Connection getConnection() throws SQLException {
            SQLiteConfig config = new SQLiteConfig();
            config.setSharedCache(true);
            config.enableRecursiveTriggers(true);
            SQLiteDataSource ds = new SQLiteDataSource(config);
            String url = System.getProperty("user.dir");
            ds.setUrl("jdbc:sqlite:" + url + "/plugins/EusAuthy/" + "EusAuthy.db");
            return ds.getConnection();
        }
    }

    public void createTable() throws SQLException {
        String sql = "create TABLE IF NOT EXISTS EusAuthy(uuid String, secretKey String); ";
        Statement stat = null;
        stat = connection.createStatement();
        try {
            stat.executeUpdate(sql);
            stat.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS EusAuthy_Index ON EusAuthy(uuid);");
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void dropTable(Connection con) throws SQLException {
        String sql = "drop table EusAuthy; ";
        Statement stat = null;
        try {
            stat = con.createStatement();
            stat.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean insert(String uuid, String secretKey) throws SQLException {
        String sql = "insert into EusAuthy (uuid, secretKey) values(?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int idx = 1;
            pstmt.setString(idx++, uuid);
            pstmt.setString(idx++, secretKey);
            pstmt.executeUpdate();
        }
        return true;
    }

    public boolean delete(String uuid) throws SQLException {
        try {
            String sql = "delete from EusAuthy where uuid = ?";
            PreparedStatement pst = null;
            pst = connection.prepareStatement(sql);
            int idx = 1;
            pst.setString(idx++, uuid);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String selectSecretKey(String uuid) throws SQLException {
        String sql = "select secretKey from EusAuthy where uuid = ?";
        PreparedStatement pst = null;
        ResultSet rs = null;
        String secretKey = null;
        try {
            pst = connection.prepareStatement(sql);
            int idx = 1;
            pst.setString(idx++, uuid);
            rs = pst.executeQuery();
            if (rs.next()) {
                secretKey = rs.getString("secretKey");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return secretKey;
    }

    private static SQLite instance = new SQLite();

    public static SQLite getInstance() {
        return instance;
    }

    private SQLite.Api api;
    private Connection connection;

    public SQLite() {
        api = new SQLite.Api();
        try {
            connection = api.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public boolean insertPlayer(PlayerData data) {
        try {
            return insert(data.getUuid().toString(), data.getSecretKey());
        } catch (SQLException e) {
            EusAuthy.plugin.getLogger().warning("EusAuthy 数据库错误");
            return false;
        }
    }

    @Override
    public String getSecretKey(UUID uuid) {
        String secretKey;
        try {
            secretKey = selectSecretKey(uuid.toString());
        } catch (SQLException e) {
            EusAuthy.plugin.getLogger().warning("EusAuthy 数据库错误");
            return null;
        }
        return secretKey;
    }

    @Override
    public boolean isPlayerRegistered(UUID uuid) {
        boolean result = false;
        try {
            if (selectSecretKey( uuid.toString()) != null) {
                result = true;
            }
        } catch (SQLException e) {
            EusAuthy.plugin.getLogger().warning("EusAuthy 数据库错误");
            result = false;
        }
        return result;
    }

    @Override
    public boolean deletePlayer(UUID uuid) {
        boolean result = false;
        try {
            result = delete(uuid.toString());
        } catch (SQLException e) {
            EusAuthy.plugin.getLogger().warning("EusAuthy 数据库错误");
        }
        return result;
    }

    @Override
    public void release() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
