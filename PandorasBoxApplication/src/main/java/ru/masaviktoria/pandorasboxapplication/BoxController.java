package ru.masaviktoria.pandorasboxapplication;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FilenameUtils;
import ru.masaviktoria.pandorasboxmodel.*;

import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.awt.Desktop;
import java.util.stream.Stream;

public class BoxController implements Initializable {

    @FXML
    public VBox vBox;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    public ImageView pandorasBoxLogo;
    @FXML
    public Label loginLabel;
    @FXML
    public Label myFilesHeader;
    @FXML
    public Label theBoxHeader;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField loginField;
    @FXML
    public Button loginButton;
    @FXML
    public Label wrongCredentialsLabel;
    @FXML
    public Label regLabel;
    @FXML
    public Button localUpButton;
    @FXML
    public Button localNewFolderButton;
    @FXML
    public Button localRenameButton;
    @FXML
    public Button localDeleteButton;
    @FXML
    public Button uploadButton;
    @FXML
    public Button downloadButton;
    @FXML
    public Button serverNewFolderButton;
    @FXML
    public Button serverRenameButton;
    @FXML
    public Button serverDeleteButton;
    @FXML
    public Button serverUpButton;
    @FXML
    public TextField localCurrentFolderTextField;
    @FXML
    public TextField serverCurrentFolderTextField;
    @FXML
    public TableView<FileListMappingInfo> localFilesTable;
    @FXML
    public TableColumn<FileListMappingInfo, ImageView> localTypeColumn;
    @FXML
    public TableColumn<FileListMappingInfo, String> localFileOrDirectoryNameColumn;
    @FXML
    public TableColumn<FileListMappingInfo, Long> localFileSizeColumn;
    @FXML
    public TableView<FileListMappingInfo> serverFilesTable;
    @FXML
    public TableColumn<FileListMappingInfo, ImageView> serverTypeColumn;
    @FXML
    public TableColumn<FileListMappingInfo, String> serverFileOrDirectoryNameColumn;
    @FXML
    public TableColumn<FileListMappingInfo, Long> serverFileSizeColumn;
    @FXML
    public TableColumn<FileListMappingInfo, String> localLastModifiedColumn;
    @FXML
    public TableColumn<FileListMappingInfo, String> serverLastModifiedColumn;
    @FXML
    public Label showPasswordLabel;
    @FXML
    public TextField passwordVisibleField;
    @FXML
    public Label returnToLoginLabel;
    @FXML
    public ImageView reloadIcon;
    @FXML
    public TextArea selectedFilePath;
    @FXML
    public TextArea selectedFileLink;
    @FXML
    public Button pathButton;
    @FXML
    public Button sharingLinkButton;

    private Network network;
    private String dir;
    private boolean isAuthorized = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.dir = CommandsAndConstants.LOCALROOTDIRECTORY;
        startLocalNavigation();
        networkConnection();
        startServerCommunication();
    }

    private void networkConnection() {
        try {
            network = new Network(CommandsAndConstants.PORT);
        } catch (IOException e) {
            System.out.println("Connection failed");
            connectionAlert();
            e.printStackTrace();
        }
    }

    private void connectionAlert() {
        Platform.runLater(() -> {
            Alert connectionAlert = new Alert(Alert.AlertType.ERROR);
            connectionAlert.setTitle("Connection failed");
            connectionAlert.setHeaderText("Server is disconnected");
            connectionAlert.setContentText("Check your internet connection and reload app");
            connectionAlert.showAndWait();
        });
    }

    private void startLocalNavigation() {
        Thread localFilesListThread = new Thread(new Runnable() {
            @Override
            public void run() {
                createLocalFilesTable();
                showLocalFiles();
            }
        });
        localFilesListThread.setDaemon(true);
        localFilesListThread.start();
    }

    private void startServerCommunication() {
        Thread serverFilesListThread = new Thread(this::createServerFilesTable);
        serverFilesListThread.setDaemon(true);
        serverFilesListThread.start();
        Thread readServerCommandsThread = new Thread(this::readCommandsFromServer);
        readServerCommandsThread.setDaemon(true);
        readServerCommandsThread.start();
    }

    private void createLocalFilesTable() {
        Platform.runLater(() -> {
            localTypeColumn.setCellValueFactory(param -> {
                String type = param.getValue().getFileType().getName();
                if (!type.isEmpty()) {
                    return new SimpleObjectProperty<ImageView>(new ImageView(new Image(getClass().getResourceAsStream("foldericon.png"))));
                } else {
                    return new SimpleObjectProperty<ImageView>(new ImageView(new Image(getClass().getResourceAsStream("fileicon.jpg"))));
                }
            });
            localFileOrDirectoryNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
            localFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<Long>(param.getValue().getFileSize()));
            localFileSizeColumn.setCellFactory(column -> {
                return new TableCell<FileListMappingInfo, Long>() {
                    @Override
                    protected void updateItem(Long item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            String text = String.format("%,d bytes", item);
                            if (item == -1) {
                                text = "";
                            }
                            setText(text);
                        }
                    }
                };
            });
            localLastModifiedColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(CommandsAndConstants.DTF)));
            localFilesTable.getSortOrder().add(localFileSizeColumn);
        });
    }

    private void createServerFilesTable() {
        Platform.runLater(() -> {
            serverTypeColumn.setCellValueFactory(param -> {
                String type = param.getValue().getFileType().getName();
                if (!type.isEmpty()) {
                    return new SimpleObjectProperty<ImageView>(new ImageView(new Image(getClass().getResourceAsStream("foldericon.png"))));
                } else {
                    return new SimpleObjectProperty<ImageView>(new ImageView(new Image(getClass().getResourceAsStream("fileicon.jpg"))));
                }
            });
            serverFileOrDirectoryNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
            serverFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<Long>(param.getValue().getFileSize()));
            serverFileSizeColumn.setCellFactory(column -> {
                return new TableCell<FileListMappingInfo, Long>() {
                    @Override
                    protected void updateItem(Long item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            String text = String.format("%,d bytes", item);
                            if (item == -1) {
                                text = "";
                            }
                            setText(text);
                        }
                    }
                };
            });
            serverLastModifiedColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(CommandsAndConstants.DTF)));
            serverFilesTable.getSortOrder().add(serverFileSizeColumn);
        });
    }

    @FXML
    private void showLocalFiles() {
        Platform.runLater(() -> {
            try {
                localCurrentFolderTextField.setText("Current: " + Path.of(dir).normalize().toAbsolutePath());
                localFilesTable.getItems().clear();
                List<FileListMappingInfo> fileList = Files.list(Path.of(dir))
                        .map(FileListMappingInfo::new)
                        .filter(FileListMappingInfo::isNotSystem)
                        .filter(FileListMappingInfo::isNotHidden)
                        .toList();
                localFilesTable.getItems().addAll(fileList);
                localFilesTable.sort();
                localFilesTable.scrollTo(0);
            } catch (IOException e) {
                System.out.println("File listing error");
                e.printStackTrace();
            }
        });
    }

    private void showServerFiles(FileList fileList) {
        Platform.runLater(() -> {
            String currentDir = fileList.getCurrentDir().substring(13);
            serverCurrentFolderTextField.setText("Current: " + currentDir);
            serverFilesTable.getItems().clear();
            serverFilesTable.getItems().addAll(fileList.getFiles());
            serverFilesTable.sort();
            serverFilesTable.scrollTo(0);
        });
    }

    private void readCommandsFromServer() {
        try {
            while (true) {
                BoxCommand boxCommand = network.read();
                if (boxCommand instanceof AuthOK authOK) {
                    authorizationProceed(authOK);
                } else if (boxCommand instanceof AuthFailed) {
                    authorizationFailed();
                } else if (boxCommand instanceof RegistrationFailed) {
                    registrationFailed();
                } else if (boxCommand instanceof FileList fileList) {
                    showServerFiles(fileList);
                } else if (boxCommand instanceof LogoutOK) {
                    logoutProceed();
                } else if (boxCommand instanceof FileContainer fileContainer) {
                    saveFileFromContainer(fileContainer);
                } else if (boxCommand instanceof NewFolderFailed) {
                    newFolderCreationFailedAlert();
                } else if (boxCommand instanceof RenameFailed) {
                    renameFailedAlert();
                } else if (boxCommand instanceof DeleteFailed) {
                    noSuchFileAlert();
                }
            }
        } catch (SocketException s) {
            System.out.println("Server was disconnected");
            connectionAlert();
        } catch (Exception e) {
            System.out.println("Reading error");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToPasswordByTabOrEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.TAB || keyEvent.getCode() == KeyCode.ENTER) {
            if (passwordField.isVisible()) {
                passwordField.requestFocus();
            } else {
                passwordVisibleField.requestFocus();
            }
        }
    }

    @FXML
    private void proceedAuthByButton(Event mouseEvent) {
        authorizationRequest();
    }

    @FXML
    private void proceedAuthByEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (!returnToLoginLabel.isVisible()) {
                authorizationRequest();
            } else {
                registrationRequest();
            }
        }
    }

    @FXML
    private void showPassword() {
        passwordField.setVisible(false);
        String pass = passwordField.getText();
        passwordField.clear();
        passwordVisibleField.setVisible(true);
        passwordVisibleField.setText(pass);
        showPasswordLabel.setText("Hide password");
        showPasswordLabel.setOnMouseClicked((EventHandler) -> {
            hidePassword();
        });
    }

    private void hidePassword() {
        Platform.runLater(() -> {
            passwordVisibleField.setVisible(false);
            String pass = passwordVisibleField.getText();
            passwordVisibleField.clear();
            passwordField.setVisible(true);
            passwordField.setText(pass);
            showPasswordLabel.setText("Show password");
            showPasswordLabel.setOnMouseClicked((EventHandler) -> {
                showPassword();
            });
        });
    }

    private void authorizationRequest() {
        if (!passwordField.getText().isEmpty()) {
            if (Pattern.matches("^[\\w\\.\\-]{1,20}$", loginField.getText()) && Pattern.matches("^[\\w\\.\\-]{1,20}$", passwordField.getText())) {
                try {
                    network.write(new AuthRequest(loginField.getText(), passwordField.getText()));
                } catch (IOException e) {
                    System.out.println("Authentication request failed");
                    e.printStackTrace();
                }
            } else {
                wrongCredentialsFormatAlert();
            }
        } else {
            if (Pattern.matches("^[\\w\\.\\-]{1,20}$", loginField.getText()) && Pattern.matches("^[\\w\\.\\-]{1,20}$", passwordVisibleField.getText())) {
                try {
                    network.write(new AuthRequest(loginField.getText(), passwordVisibleField.getText()));
                } catch (IOException e) {
                    System.out.println("Authentication request failed");
                    e.printStackTrace();
                }
            } else {
                wrongCredentialsFormatAlert();
            }
        }
    }

    @FXML
    private void createAccount() {
        Platform.runLater(() -> {
            serverFilesTable.getItems().clear();
            loginButton.setText("Create");
            regLabel.setText("Use only a-Z 0-9 . _");
            regLabel.setDisable(true);
            returnToLoginLabel.setVisible(true);
            wrongCredentialsLabel.setVisible(false);
            loginButton.setOnMouseClicked((EventHandler) -> {
                registrationRequest();
            });
        });
    }

    private void registrationRequest() {
        if (!passwordField.getText().isEmpty()) {
            if (Pattern.matches("^[\\w\\.\\-]{1,20}$", loginField.getText()) && Pattern.matches("^[\\w\\.\\-]{1,20}$", passwordField.getText())) {
                try {
                    network.write(new RegistrationRequest(loginField.getText(), passwordField.getText()));
                } catch (IOException e) {
                    System.out.println("Registration request failed");
                    e.printStackTrace();
                }
            } else {
                wrongCredentialsLabel.setVisible(true);
                wrongCredentialsFormatAlert();
            }
        } else {
            if (Pattern.matches("^[\\w\\.\\-]{1,20}$", loginField.getText()) && Pattern.matches("^[\\w\\.\\-]{1,20}$", passwordVisibleField.getText())) {
                try {
                    network.write(new RegistrationRequest(loginField.getText(), passwordVisibleField.getText()));
                } catch (IOException e) {
                    System.out.println("Registration request failed");
                    e.printStackTrace();
                }
            } else {
                wrongCredentialsLabel.setVisible(true);
                wrongCredentialsFormatAlert();
            }
        }
    }

    @FXML
    private void returnToLogin() {
        returnToLoginLabel.setVisible(false);
        loginButton.setText("Log in");
        loginButton.setOnMouseClicked((EventHandler) this::proceedAuthByButton);
        regLabel.setText("Create account");
        regLabel.setDisable(false);
        wrongCredentialsLabel.setVisible(false);
    }

    private void wrongCredentialsFormatAlert() {
        Platform.runLater(() -> {
            Alert wrongFormatAlert = new Alert(Alert.AlertType.ERROR);
            wrongFormatAlert.setTitle("Input Error");
            wrongFormatAlert.setHeaderText("Wrong format");
            wrongFormatAlert.setContentText("Please, use only: \na-Z \n0-9 \n_ . -");
            wrongFormatAlert.showAndWait();
        });
    }

    private void authorizationProceed(AuthOK authOK) {
        Platform.runLater(() -> {
            isAuthorized = true;
            loginLabel.setVisible(true);
            loginLabel.setText("login: " + authOK.getLogin());
            wrongCredentialsLabel.setVisible(false);
            loginField.setVisible(false);
            passwordField.setVisible(false);
            passwordVisibleField.setVisible(false);
            showPasswordLabel.setVisible(false);
            regLabel.setVisible(false);
            returnToLoginLabel.setVisible(false);
            loginButton.setOnMouseClicked((EventHandler) event -> logoutRequest());
            loginButton.setText("Log out");
        });
        System.out.println("Authorization successful");
    }

    private void authorizationFailed() {
        Platform.runLater(() -> {
            wrongCredentialsLabel.setVisible(true);
            wrongCredentialsAlert();
            loginField.clear();
            passwordField.clear();
            passwordVisibleField.clear();
        });
    }

    private void wrongCredentialsAlert() {
        Platform.runLater(() -> {
            Alert wrongCredentialsAlert = new Alert(Alert.AlertType.ERROR);
            wrongCredentialsAlert.setTitle("Authorization failed");
            wrongCredentialsAlert.setHeaderText("Wrong login or password");
            wrongCredentialsAlert.setContentText("Please, try again. Use only a-Z 0-9 . _");
            wrongCredentialsAlert.showAndWait();
        });
    }

    private void registrationFailed() {
        Platform.runLater(() -> {
            wrongCredentialsLabel.setVisible(true);
            loginAlreadyExistsAlert();
            loginField.clear();
            passwordField.clear();
            passwordVisibleField.clear();
        });
    }

    private void loginAlreadyExistsAlert() {
        Platform.runLater(() -> {
            Alert loginAlreadyExistsAlert = new Alert(Alert.AlertType.ERROR);
            loginAlreadyExistsAlert.setTitle("Registration failed");
            loginAlreadyExistsAlert.setHeaderText("This login already exists");
            loginAlreadyExistsAlert.setContentText("Please, create another account or log in.\nUse only a-Z 0-9 . _");
            loginAlreadyExistsAlert.showAndWait();
        });
    }

    private void logoutRequest() {
        try {
            network.write(new LogoutRequest());
        } catch (IOException e) {
            System.out.println("Logout request failed");
            e.printStackTrace();
        }
    }

    private void logoutProceed() {
        Platform.runLater(() -> {
            isAuthorized = false;
            serverFilesTable.getItems().clear();
            serverCurrentFolderTextField.setText("Current:");
            loginLabel.setVisible(false);
            loginLabel.setText("login: ");
            loginField.clear();
            passwordField.clear();
            passwordVisibleField.clear();
            loginField.setVisible(true);
            passwordField.setVisible(true);
            passwordVisibleField.setVisible(false);
            showPasswordLabel.setVisible(true);
            showPasswordLabel.setText("Make visible");
            showPasswordLabel.setOnMouseClicked((EventHandler) -> {
                showPassword();
            });
            loginButton.setText("Log in");
            loginButton.setOnMouseClicked((EventHandler) this::proceedAuthByButton);
            regLabel.setText("Create account");
            regLabel.setDisable(false);
            regLabel.setVisible(true);
            returnToLoginLabel.setVisible(false);
            wrongCredentialsLabel.setVisible(false);
        });
        System.out.println("Logout successful");
    }

    //todo: процесс загрузки файла - отображение
    @FXML
    private void uploadFile() {
        if (isAuthorized) {
            if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
                try {
                    String selectedFile = localFilesTable.getSelectionModel().getSelectedItem().getFileName();
                    network.write(new FileContainer(Path.of(dir).resolve(selectedFile)));
                    System.out.println("Upload successful");
                } catch (IOException e) {
                    System.out.println("Upload unsuccessful");
                    e.printStackTrace();
                }
            }
        }
    }

    //todo: процесс загрузки файла - отображение
    @FXML
    private void downloadFile() {
        if (isAuthorized) {
            if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
                try {
                    String selectedFile = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                    System.out.println("Download started: " + selectedFile);
                    network.write(new FileRequest(selectedFile));
                } catch (IOException e) {
                    System.out.println("File was not accepted");
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveFileFromContainer(FileContainer fileContainer) {
        try {
            Path current = Path.of(dir).resolve(fileContainer.getFileName());
            Files.write(current, fileContainer.getFileData());
            System.out.println("Received file: " + fileContainer.getFileName());
            showLocalFiles();
        } catch (IOException e) {
            System.out.println("File saving unsuccessful");
            e.printStackTrace();
        }
    }

    @FXML
    private void goUpLocally() {
        if (!dir.equals(CommandsAndConstants.LOCALROOTDIRECTORY)) {
            this.dir = Path.of(dir).getParent().toString();
            showLocalFiles();
            selectedFilePath.clear();
        } else {
            showLocalFiles();
            selectedFilePath.clear();
        }
    }

    @FXML
    private void checkLocalDirectory(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
                Path selectedPath = Path.of(dir).resolve(localFilesTable.getSelectionModel().getSelectedItem().getFileName());
                if (Files.isDirectory(selectedPath)) {
                    this.dir = selectedPath.toString();
                    showLocalFiles();
                    selectedFilePath.clear();
                }
            }
        }
    }

    @FXML
    private void showSelectedFilePath() {
        if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
            Path selectedPath = Path.of(dir).resolve(localFilesTable.getSelectionModel().getSelectedItem().getFileName()).toAbsolutePath().normalize();
            if (!Files.isDirectory(selectedPath)) {
                selectedFilePath.setText(String.valueOf(selectedPath));
            }
        }
    }

    //todo: показ ссылки на файл на сервере (sharing link)

    @FXML
    private void goUpOnServer() {
        if (isAuthorized) {
            try {
                network.write(new PathUpRequest());
            } catch (IOException e) {
                System.out.println("Server navigation error");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void checkServerDirectory(MouseEvent mouseEvent) {
        if (isAuthorized) {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
                    String selectedFile = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                    try {
                        network.write(new PathInRequest(selectedFile));
                    } catch (IOException e) {
                        System.out.println("Server navigation error");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @FXML
    private void createNewLocalDirectory() {
        TextInputDialog newDirectoryNameDialog = new TextInputDialog("NewFolder");
        newDirectoryNameDialog.setTitle("Create new local folder");
        newDirectoryNameDialog.setHeaderText("Folder name:");
        newDirectoryNameDialog.setContentText("-->");
        Optional<String> result = newDirectoryNameDialog.showAndWait();
        if (result.isPresent()) {
            String newDirectoryName = result.get();
            if (Pattern.matches("^[\\p{L}\\p{N}\\s_.-]{1,255}$", newDirectoryName)) {
                if (!Files.exists(Path.of(dir).resolve(newDirectoryName))) {
                    try {
                        Files.createDirectory(Path.of(dir).resolve(newDirectoryName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("User created a new local folder: " + newDirectoryName);
                    showLocalFiles();
                } else {
                    newFolderCreationFailedAlert();
                }
            } else {
                wrongFileOrDirectoryNameFormatAlert();
            }
        }
    }

    @FXML
    private void createNewServerDirectory() {
        if (isAuthorized) {
            TextInputDialog newDirectoryNameDialog = new TextInputDialog("NewFolder");
            newDirectoryNameDialog.setTitle("Create new server folder");
            newDirectoryNameDialog.setHeaderText("Folder name:");
            newDirectoryNameDialog.setContentText("-->");
            Optional<String> result = newDirectoryNameDialog.showAndWait();
            if (result.isPresent()) {
                String newDirectoryName = result.get();
                if (Pattern.matches("^[\\p{L}\\p{N}\\s_.-]{1,255}$", newDirectoryName)) {
                    try {
                        network.write(new NewFolderRequest(newDirectoryName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("User created a new server folder: " + newDirectoryName);
                } else {
                    wrongFileOrDirectoryNameFormatAlert();
                }
            }
        }
    }

    private void newFolderCreationFailedAlert() {
        Platform.runLater(() -> {
            Alert newFolderCreationFailed = new Alert(Alert.AlertType.ERROR);
            newFolderCreationFailed.setTitle("New folder was not created");
            newFolderCreationFailed.setHeaderText("The folder with this name already exists");
            newFolderCreationFailed.setContentText("");
            newFolderCreationFailed.showAndWait();
        });
    }

    private void wrongFileOrDirectoryNameFormatAlert() {
        Platform.runLater(() -> {
            Alert wrongFormatAlert = new Alert(Alert.AlertType.ERROR);
            wrongFormatAlert.setTitle("Input Error");
            wrongFormatAlert.setHeaderText("Wrong format");
            wrongFormatAlert.setContentText("Please, use: \nany letters \nany numbers \n_  . - and space");
            wrongFormatAlert.showAndWait();
        });
    }

    //todo: UID для файлов, запись в базу, переименование и удаление на сервере через UID
    @FXML
    private void renameSelectedLocalFileOrDirectory() {
        if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
            String oldName = dir + "/" + localFilesTable.getSelectionModel().getSelectedItem().getFileName();
            TextInputDialog renameDialog = new TextInputDialog("NewName");
            renameDialog.setTitle("Rename File or Directory");
            renameDialog.setHeaderText("New name:");
            renameDialog.setContentText("-->");
            Optional<String> result = renameDialog.showAndWait();
            if (result.isPresent()) {
                String newName = result.get();
                if (Pattern.matches("^[\\p{L}\\p{N}\\s_.-]{1,255}$", newName)) {
                    newName = dir + "/" + newName + "." + FilenameUtils.getExtension(oldName);
                    if (!Files.exists(Path.of(newName))) {
                        Path source = Path.of(oldName);
                        try {
                            Files.move(source, source.resolveSibling(newName));
                            showLocalFiles();
                        } catch (IOException e) {
                            System.out.println("File rename failed");
                            e.printStackTrace();
                        }
                    } else {
                        renameFailedAlert();
                    }
                } else {
                    wrongFileOrDirectoryNameFormatAlert();
                }
            }
        }
    }

    @FXML
    private void renameSelectedServerFileOrDirectory() {
        if (isAuthorized) {
            if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
                String oldName = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                TextInputDialog renameDialog = new TextInputDialog("NewName");
                renameDialog.setTitle("Rename File or Directory");
                renameDialog.setHeaderText("New name:");
                renameDialog.setContentText("-->");
                Optional<String> result = renameDialog.showAndWait();
                if (result.isPresent()) {
                    String newName = result.get();
                    if (Pattern.matches("^[\\p{L}\\p{N}\\s_.-]{1,255}$", newName)) {
                        newName = newName + "." + FilenameUtils.getExtension(oldName);
                        try {
                            network.write(new RenameRequest(oldName, newName));
                        } catch (IOException e) {
                            System.out.println("Rename failed");
                            e.printStackTrace();
                        }
                    } else {
                        wrongFileOrDirectoryNameFormatAlert();
                    }
                }
            }
        }
    }

    private void renameFailedAlert() {
        Platform.runLater(() -> {
            Alert renameFailed = new Alert(Alert.AlertType.ERROR);
            renameFailed.setTitle("Rename failed");
            renameFailed.setHeaderText("The file or folder with this name already exists");
            renameFailed.setContentText("");
            renameFailed.showAndWait();
        });
    }

    @FXML
    private void deleteSelectedLocalFileOrDirectory() {
        if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
            String itemToDelete = dir + "/" + localFilesTable.getSelectionModel().getSelectedItem().getFileName();
            Platform.runLater(() -> {
                Alert deletePrompt = new Alert(Alert.AlertType.CONFIRMATION);
                deletePrompt.setTitle("You are going to delete this file");
                deletePrompt.setHeaderText("Delete " + itemToDelete + "?");
                deletePrompt.setContentText("");
                ButtonType yes = new ButtonType("Delete");
                ButtonType no = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                deletePrompt.getButtonTypes().setAll(yes, no);
                Optional<ButtonType> result = deletePrompt.showAndWait();
                if (result.get() == yes) {
                    File file = new File(itemToDelete);
                    if (Files.exists(Path.of(itemToDelete))) {
                        Desktop.getDesktop().moveToTrash(file);
                        System.out.println("File " + itemToDelete + " was moved to Recycle Bin");
                    } else {
                        noSuchFileAlert();
                    }
                    showLocalFiles();
                }
            });
        }
    }

    @FXML
    private void deleteSelectedServerFileOrDirectory() {
        if (isAuthorized) {
            if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
                String itemToDelete = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                Platform.runLater(() -> {
                    Alert deletePrompt = new Alert(Alert.AlertType.CONFIRMATION);
                    deletePrompt.setTitle("You are going to delete this file");
                    deletePrompt.setHeaderText("Delete " + itemToDelete + "?");
                    deletePrompt.setContentText("");
                    ButtonType yes = new ButtonType("Delete");
                    ButtonType no = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    deletePrompt.getButtonTypes().setAll(yes, no);
                    Optional<ButtonType> result = deletePrompt.showAndWait();
                    if (result.get() == yes) {
                        try {
                            network.write(new DeleteRequest(itemToDelete));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private void noSuchFileAlert() {
        Platform.runLater(() -> {
            Alert noSuchFileAlert = new Alert(Alert.AlertType.ERROR);
            noSuchFileAlert.setTitle("Deletion Error");
            noSuchFileAlert.setHeaderText("The file does not exist");
            noSuchFileAlert.setContentText("");
            noSuchFileAlert.showAndWait();
        });
    }
}
