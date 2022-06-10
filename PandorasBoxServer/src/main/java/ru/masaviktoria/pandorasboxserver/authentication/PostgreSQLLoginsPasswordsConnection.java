package ru.masaviktoria.pandorasboxserver.authentication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLLoginsPasswordsConnection {
    public static Connection connection;
    public static Statement statement;

    PostgreSQLLoginsPasswordsConnection(){
    }

    public static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://ec2-52-48-159-67.eu-west-1.compute.amazonaws.com:5432/db61ke1j1tqoqj", "fojnmiissvizve", "cb684fa0ad43c18e1d6790ea94addb9fe6af5aafc6c7bb27e43ab44bbf5b75bc");
        statement = connection.createStatement();
    }

    public static void disconnect() {
        try {
            if (connection != null){
                connection.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
