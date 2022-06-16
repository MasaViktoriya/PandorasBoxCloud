package ru.masaviktoria.pandorasboxserver.services;

import ru.masaviktoria.pandorasboxmodel.FileContainer;

import java.sql.*;

public class SQLService {
    public static Connection connection;
    public static Statement statement;

    SQLService() {
    }

    public static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://ec2-52-48-159-67.eu-west-1.compute.amazonaws.com:5432/db61ke1j1tqoqj", "fojnmiissvizve", "cb684fa0ad43c18e1d6790ea94addb9fe6af5aafc6c7bb27e43ab44bbf5b75bc");
        statement = connection.createStatement();
    }

    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected static void insertToLoginsPasswords(String newLogin, String newPassword) {
        try {
            connect();
            statement.executeUpdate(String.format("INSERT INTO public.\"logins-passwords\" (user_login, user_password) VALUES ('%s', '%s')", newLogin, newPassword));
            disconnect();
        } catch (SQLException e) {
            System.out.println("Writing to database failed");
            e.printStackTrace();
        }
    }

    //todo: не хватает полей file_last_modified, file_sharing_link
    protected static void insertToFileInformation(FileContainer fileContainer) {
        try {
            connect();
            statement.executeUpdate(String.format("INSERT INTO public.\"file_information\" (file_name, file_size, file_type, file_server_path, file_owner) VALUES ('%s', '%s', '%s', '%s', '%s')", fileContainer.getFileName(), fileContainer.getFileSize(), fileContainer.getFileType(), fileContainer.getFileServerPath(), fileContainer.getFileOwner()));
            disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected static ResultSet returnPassword(String login) throws SQLException {
        connect();
        ResultSet userPassword = statement.executeQuery(String.format("SELECT user_password FROM public.\"logins-passwords\" WHERE user_login = '%s'", login));
        disconnect();
        return userPassword;
    }

    protected static ResultSet findLogin(String login) throws SQLException {
        connect();
        ResultSet userLogin = statement.executeQuery(String.format("SELECT user_login FROM public.\"logins-passwords\" WHERE user_login = '%s'", login));
        disconnect();
        return userLogin;
    }
}
