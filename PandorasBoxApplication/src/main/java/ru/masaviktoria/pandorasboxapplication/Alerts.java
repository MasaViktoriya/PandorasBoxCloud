package ru.masaviktoria.pandorasboxapplication;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class Alerts extends Alert {

    public Alerts(AlertType alertType, String title, String headerText, String contentText) {
        super(alertType);
        Platform.runLater(() -> {
            setTitle(title);
            setHeaderText(headerText);
            setContentText(contentText);
            showAndWait();
        });
    }

    protected static void connectionAlert() {
        new Alerts(Alert.AlertType.ERROR, "Connection failed", "Server is disconnected", "Check your internet connection and reload app");
    }

    protected static void wrongCredentialsFormatAlert() {
        new Alerts(Alert.AlertType.ERROR, "Input Error", "Wrong format", "Please, use only: \na-Z \n0-9 \n_ . -");
    }

    protected static void wrongCredentialsAlert() {
        new Alerts(Alert.AlertType.ERROR, "Authorization failed", "Wrong login or password", "Please, try again. Use only a-Z 0-9 . _");
    }


    protected static void loginAlreadyExistsAlert() {
        new Alerts(Alert.AlertType.ERROR, "Registration failed", "This login already exists", "Please, create another account or log in.\nUse only a-Z 0-9 . _");
    }

    protected static void newFolderCreationFailedAlert() {
        new Alerts(Alert.AlertType.ERROR, "New folder was not created", "The folder with this name already exists", "");
    }

    protected static void wrongFileOrDirectoryNameFormatAlert() {
        new Alerts(Alert.AlertType.ERROR, "Input Error", "Wrong format", "Please, use: \nany letters \nany numbers \n_  . - and space");
    }

    protected static void renameFailedAlert() {
        new Alerts(Alerts.AlertType.ERROR, "Rename failed", "The file or folder with this name already exists", "");
    }

    protected static void noSuchFileAlert() {
        new Alerts(Alerts.AlertType.ERROR, "Deletion Error", "The file does not exist", "");
    }
}