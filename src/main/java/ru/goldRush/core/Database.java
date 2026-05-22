package ru.goldRush.core;

import ru.goldRush.GoldRush;
import java.io.File;
import java.sql.*;
import java.util.*;

public class Database {
    private Connection conn;

    public Database() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + new File(GoldRush.getInstance().getDataFolder(), "goldrush.db"));
            Statement st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "uuid TEXT PRIMARY KEY, name TEXT, pass TEXT, ip TEXT, balance INT DEFAULT 100, " +
                    "prefix TEXT DEFAULT '§7', premium LONG DEFAULT 0)");
            st.execute("CREATE TABLE IF NOT EXISTS ratings (uuid TEXT PRIMARY KEY, rating INTEGER DEFAULT 0)");
            st.execute("CREATE TABLE IF NOT EXISTS ip_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, ip TEXT, uuid TEXT, player_name TEXT, login_time LONG)");
            st.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public boolean exists(UUID u) { return query("SELECT uuid FROM users WHERE uuid='" + u + "'"); }
    public boolean pass(UUID u, String p) { return query("SELECT uuid FROM users WHERE uuid='" + u + "' AND pass='" + p + "'"); }
    public void register(UUID u, String n, String p, String ip) { exec("INSERT INTO users VALUES('" + u + "','" + n + "','" + p + "','" + ip + "',100,'§7',0)"); }
    public void updateIp(UUID u, String ip, String name) { exec("UPDATE users SET ip='" + ip + "', name='" + name + "' WHERE uuid='" + u + "'"); }
    public int balance(UUID u) { return getInt("SELECT balance FROM users WHERE uuid='" + u + "'"); }
    public void setBalance(UUID u, int v) { exec("UPDATE users SET balance=" + v + " WHERE uuid='" + u + "'"); }
    public String getIp(String name) { return getStr("SELECT ip FROM users WHERE name='" + name + "'"); }
    public List<String> getAccountsByIp(String ip) { return getList("SELECT name FROM users WHERE ip='" + ip + "'"); }

    public String getPrefix(UUID uuid) {
        String prefix = getStr("SELECT prefix FROM users WHERE uuid='" + uuid + "'");
        return prefix != null ? prefix : "§7";
    }
    public void setPrefix(UUID uuid, String prefix) { exec("UPDATE users SET prefix='" + prefix + "' WHERE uuid='" + uuid + "'"); }

    public int getRating(UUID uuid) { return getInt("SELECT rating FROM ratings WHERE uuid='" + uuid + "'"); }
    public void setRating(UUID uuid, int rating) { exec("INSERT OR REPLACE INTO ratings VALUES('" + uuid + "', " + rating + ")"); }
    public boolean existsInRatings(UUID uuid) { return query("SELECT uuid FROM ratings WHERE uuid='" + uuid + "'"); }

    public boolean hasPremium(UUID uuid) {
        long expiry = getLong("SELECT premium FROM users WHERE uuid='" + uuid + "'");
        return expiry > System.currentTimeMillis();
    }

    public void setPremium(UUID uuid, int days) {
        long expiry = System.currentTimeMillis() + (days * 86400000L);
        exec("UPDATE users SET premium = " + expiry + " WHERE uuid = '" + uuid + "'");
    }

    public long getLong(String sql) {
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public void exec(String sql) { try { conn.createStatement().execute(sql); } catch (Exception e) { e.printStackTrace(); } }

    public boolean query(String sql) {
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) { return rs.next(); }
        catch (Exception e) { return false; }
    }

    public ResultSet queryResult(String sql) {
        try {
            return conn.createStatement().executeQuery(sql);
        } catch (Exception e) {
            return null;
        }
    }

    public int getInt(String sql) {
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    private String getStr(String sql) {
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        } catch (Exception e) { return null; }
    }

    private List<String> getList(String sql) {
        List<String> l = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) l.add(rs.getString(1));
        } catch (Exception e) {}
        return l;
    }

    public void close() { try { conn.close(); } catch (Exception e) {} }
}