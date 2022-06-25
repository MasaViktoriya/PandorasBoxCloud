package ru.masaviktoria.pandorasboxapplication;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class Prompts {

    protected static String createNewDirectoryPrompt(String location) {
        TextInputDialog newDirectoryNameDialog = new TextInputDialog("New Folder");
        newDirectoryNameDialog.setTitle("Create new " + location + " folder");
        newDirectoryNameDialog.setHeaderText("Folder name:");
        newDirectoryNameDialog.setContentText("-->");
        Optional<String> result = newDirectoryNameDialog.showAndWait();
        return result.orElse("");
    }

    protected static String renameDirectoryPrompt() {
        TextInputDialog renameDialog = new TextInputDialog("NewName");
        renameDialog.setTitle("Rename File or Directory");
        renameDialog.setHeaderText("New name:");
        renameDialog.setContentText("-->");
        Optional<String> result = renameDialog.showAndWait();
        return result.orElse("");
    }


    protected static boolean deletePrompt(String itemToDelete) {
        Alert deletePrompt = new Alert(Alert.AlertType.CONFIRMATION);
        deletePrompt.setTitle("You are going to delete this file");
        deletePrompt.setHeaderText("Delete " + itemToDelete + "?");
        deletePrompt.setContentText("");
        ButtonType yes = new ButtonType("Delete");
        ButtonType no = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        deletePrompt.getButtonTypes().setAll(yes, no);
        Optional<ButtonType> result = deletePrompt.showAndWait();
        return result.get() == yes;
    }
}
