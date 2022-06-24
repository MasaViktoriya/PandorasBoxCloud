package ru.masaviktoria.pandorasboxserver.services;

import ru.masaviktoria.pandorasboxmodel.FileContainer;
import ru.masaviktoria.pandorasboxmodel.FileListMappingInfo;

import java.sql.*;

public class SQLService {
    public static Connection connection;
    public static Statement statement;

    SQLService() {
    }

    private static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://ec2-52-48-159-67.eu-west-1.compute.amazonaws.com:5432/db61ke1j1tqoqj", "fojnmiissvizve", "cb684fa0ad43c18e1d6790ea94addb9fe6af5aafc6c7bb27e43ab44bbf5b75bc");
        statement = connection.createStatement();
    }

    private static void disconnect() {
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

    protected static ResultSet getID(String user_login) throws SQLException {
        connect();
        ResultSet file_owner = statement.executeQuery(String.format("SELECT id from public.\"logins-passwords\" where user_login = '%s'", user_login));
        disconnect();
        return file_owner;
    }

    //todo: добавить поле file_sharing_link
    protected static void insertToFileInformation(FileListMappingInfo fileListMappingInfo, FileContainer fileContainer) {
        try {
            connect();
            statement.executeUpdate(String.format("INSERT INTO public.\"file_information\" (file_name, file_size, file_last_modified, file_owner, file_bytes) VALUES ('%s', '%s', '%s', '%s', '%s')", fileListMappingInfo.getFileName(), fileListMappingInfo.getFileSize(), fileListMappingInfo.getLastModified(), fileListMappingInfo.getFileOwner(), (Object) fileContainer.getFileData()));
            disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //todo: UID для файлов позволит использовать этот метод
    protected static void renameFile(String newName, String uid){
        try {
            connect();
            statement.executeUpdate(String.format("UPDATE public.\"file_information\" SET file_name = '%s' WHERE uid = '%s'", newName, uid));
            disconnect();
        }catch (SQLException e) {
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
