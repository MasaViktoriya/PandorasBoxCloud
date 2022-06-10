package ru.masaviktoria.pandorasboxserver.authentication;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public static boolean checkLogin(String login, String password) {
        try {
            PostgreSQLLoginsPasswordsConnection.connect();
            ResultSet userPassword = PostgreSQLLoginsPasswordsConnection.statement.executeQuery(String.format("SELECT user_password FROM public.\"logins-passwords\" WHERE user_login = '%s'", login));
            PostgreSQLLoginsPasswordsConnection.disconnect();
            while (userPassword.next()) {
                if (userPassword.getString("user_password").equals(password)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Connection to database failed");
            e.printStackTrace();
        }
        return false;
    }

    public static void newUserToDatabase(String newLogin, String newPassword) {
        try {
            PostgreSQLLoginsPasswordsConnection.connect();
            PostgreSQLLoginsPasswordsConnection.statement.executeUpdate(String.format("INSERT INTO public.\"logins-passwords\" (user_login, user_password) VALUES ('%s', '%s')", newLogin, newPassword));
            PostgreSQLLoginsPasswordsConnection.disconnect();
        } catch (SQLException e) {
            System.out.println("Writing to database failed");
            e.printStackTrace();
        }
    }

    public static boolean checkExistingUser(String newLogin){
        try {
            PostgreSQLLoginsPasswordsConnection.connect();
            ResultSet userLogin = PostgreSQLLoginsPasswordsConnection.statement.executeQuery(String.format("SELECT user_login FROM public.\"logins-passwords\" WHERE user_login = '%s'", newLogin));
            PostgreSQLLoginsPasswordsConnection.disconnect();
            while (userLogin.next()) {
                if (userLogin.getString("user_login").equals(newLogin)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Connection to database failed");
            e.printStackTrace();
        }
        return false;
    }
}


