package ru.masaviktoria.pandorasboxserver.services;

import ru.masaviktoria.pandorasboxmodel.*;
import ru.masaviktoria.pandorasboxserver.HandlingResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegAndAuthService {

    public static HandlingResult registrationRequestHandle(RegistrationRequest registrationRequest) throws IOException {
        if (!checkExistingUser(registrationRequest.getNewLogin())) {
            SQLService.insertToLoginsPasswords(registrationRequest.getNewLogin(), registrationRequest.getNewPassword());
            System.out.println("New user created in database");
            Path newCurrentDir = Path.of(CommandsAndConstants.SERVERROOTDIRECTORY).resolve(registrationRequest.getNewLogin());
            Files.createDirectory(newCurrentDir);
            System.out.println("New folder " + registrationRequest.getNewLogin() + " created in " + CommandsAndConstants.SERVERROOTDIRECTORY);
            String user = registrationRequest.getNewLogin();
            return new HandlingResult(new AuthOK(), newCurrentDir, user);
        } else {
            System.out.println("User " + registrationRequest.getNewLogin() + " already exists");
            return new HandlingResult(new AuthFailed());
        }
    }

    private static boolean checkExistingUser(String newLogin) {
        try {
            ResultSet userLogin = SQLService.findLogin(newLogin);
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

    public static HandlingResult authRequestHandle(AuthRequest authRequest) {
        if (checkCredentials(authRequest.getLogin(), authRequest.getPassword())) {
            Path newCurrentDir = Path.of(CommandsAndConstants.SERVERROOTDIRECTORY).resolve(authRequest.getLogin());
            String user = authRequest.getLogin();
            System.out.println("Authorisation successful");
            return new HandlingResult(new AuthOK(), newCurrentDir, user);
        } else {
            System.out.println("Login or password is incorrect");
            return new HandlingResult(new AuthFailed());
        }
    }

    private static boolean checkCredentials(String login, String password) {
        try {
            ResultSet userPassword = SQLService.returnPassword(login);
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

    //todo: сброс юзера
    public static HandlingResult logoutRequestHandle() {
        Path newCurrentDir = Path.of(CommandsAndConstants.SERVERROOTDIRECTORY);
        System.out.println("User logged out");
        return new HandlingResult(new LogoutOK(), newCurrentDir);
    }
}


