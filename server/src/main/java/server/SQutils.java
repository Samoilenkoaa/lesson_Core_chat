package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SQutils {

    private static Connection connection;
    private static Statement stmt;
    public static final String NAME = "name";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";

    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }


    private static void disconnect() {
        try {
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public static List<Map<String, String>> getAllUsers() {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            connect();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
               Map<String, String> map = new HashMap<>();
               map.put(NAME, rs.getString(NAME));
               map.put(LOGIN, rs.getString(LOGIN));
               map.put(PASSWORD, rs.getString(PASSWORD));
               list.add(map);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            disconnect();
        }
        return list;
    }

    public static void saveUser(String name, String login, String password) {
        try {
            connect();
            stmt.executeUpdate("INSERT INTO users (name, login, password) VALUES ('" + name + "','"+login+"','" + password+"')");
            disconnect();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            disconnect();
        }

    }



}
