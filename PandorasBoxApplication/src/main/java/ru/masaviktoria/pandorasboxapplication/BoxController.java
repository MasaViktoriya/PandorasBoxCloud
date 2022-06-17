package ru.masaviktoria.pandorasboxapplication;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FilenameUtils;
import ru.masaviktoria.pandorasboxmodel.*;

import java.io.*;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

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
    public TableColumn<FileListMappingInfo, String> localTypeColumn;
    @FXML
    public TableColumn<FileListMappingInfo, String> localFileOrDirectoryName;
    @FXML
    public TableColumn<FileListMappingInfo, Long> localFileSize;
    @FXML
    public TableView<FileListMappingInfo> serverFilesTable;
    @FXML
    public TableColumn<FileListMappingInfo, String> serverTypeColumn;
    @FXML
    public TableColumn<FileListMappingInfo, String> serverFileOrDirectoryName;
    @FXML
    public TableColumn<FileListMappingInfo, Long> serverFileSize;
    @FXML
    public TableColumn<FileListMappingInfo, String> localLastModifiedColumn;
    @FXML
    public TableColumn<FileListMappingInfo, String> serverLastModifiedColumn;
    @FXML
    public TextArea selectedFileInfoArea;

    private Network network;
    private String dir;
    private boolean isAuthorized = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            this.dir = CommandsAndConstants.LOCALROOTDIRECTORY;
            network = new Network(CommandsAndConstants.PORT);
            Thread localFilesListThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    createLocalFilesTable();
                    showLocalFiles();
                }
            });
            localFilesListThread.setDaemon(true);
            localFilesListThread.start();
            /*Thread serverFilesListThread = new Thread(this::createServerFilesTable);
            serverFilesListThread.setDaemon(true);
            serverFilesListThread.start();*/
            createServerFilesTable();
            Thread readServerCommandsThread = new Thread(this::readCommandsFromServer);
            readServerCommandsThread.setDaemon(true);
            readServerCommandsThread.start();

        } catch (IOException e) {
            System.out.println("Initialization or runtime fail");
            e.printStackTrace();
        }
    }

    private void createLocalFilesTable() {
        Platform.runLater(() -> {
            localTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getName()));
            localFileOrDirectoryName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
            localFileSize.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getFileSize()));
            localFileSize.setCellFactory(column -> {
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
            localFilesTable.getSortOrder().add(localTypeColumn);
        });
    }

    private void createServerFilesTable() {
        Platform.runLater(() -> {
           serverTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getName()));
            serverFileOrDirectoryName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
            serverFileSize.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getFileSize()));
            serverFileSize.setCellFactory(column -> {
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
            serverFilesTable.getSortOrder().add(serverTypeColumn);
        });
    }

    private void showLocalFiles() {
        Platform.runLater(() -> {
            try {
                localCurrentFolderTextField.setText("Current folder: " + Path.of(dir).normalize().toAbsolutePath());
                localFilesTable.getItems().clear();
                localFilesTable.getItems().addAll(Files.list(Path.of(dir)).map(FileListMappingInfo::new).toList());
                localFilesTable.sort();
            } catch (IOException e) {
                System.out.println("File listing error");
                e.printStackTrace();
            }
        });
    }

    private void showServerFiles(FileList fileList) {
        Platform.runLater(() -> {
            Path currentDir = Path.of(fileList.getCurrentDir());
            serverCurrentFolderTextField.setText("Current folder: " + currentDir);
            serverFilesTable.getItems().clear();
            serverFilesTable.getItems().addAll(fileList.getFiles());
            localFilesTable.sort();
        });
    }

    //todo: показ информации о файле в нижнем окне
    private void showFileInformation(FileInfomationField fileInfomationField) {
       /* Platform.runLater(() -> {
            selectedFileInfoArea.setText(fileInfomationField.getInformation());
        });*/
    }

    private void readCommandsFromServer() {
        try {
            while (true) {
                BoxCommand boxCommand = network.read();
                if (boxCommand instanceof AuthOK authOK) {
                    authorizationProceed(authOK);
                } else if (boxCommand instanceof AuthFailed) {
                    authorizationOrRegistrationFailed();
                } else if (boxCommand instanceof FileList fileList) {
                    showServerFiles(fileList);
                } else if (boxCommand instanceof LogoutOK) {
                    logoutProceed();
                } else if (boxCommand instanceof FileInfomationField fileInfomationField) {
                    showFileInformation(fileInfomationField);
                } else if (boxCommand instanceof FileContainer fileContainer) {
                    saveFileFromContainer(fileContainer);
                } else if (boxCommand instanceof NewFolderFailed) {
                    newFolderCreationFailedAlert();
                } else if (boxCommand instanceof DeleteFailed deleteFailed) {
                    deleteFailed(deleteFailed);
                }
            }
        } catch (Exception e) {
            System.out.println("Reading of server messages was interrupted");
            e.printStackTrace();
        }
    }

    private void wrongFormatAlert() {
        Platform.runLater(()-> {
            Alert wrongFormatAlert = new Alert(Alert.AlertType.ERROR);
            wrongFormatAlert.setTitle("Input Error");
            wrongFormatAlert.setHeaderText("Wrong format");
            wrongFormatAlert.setContentText("Please, use only a-Z 0-9 . _");
            wrongFormatAlert.showAndWait();
        });
    }

    public void authorizationRequest(Event mouseEvent) {
        if (Pattern.matches("^[\\w\\.]{1,20}$", loginField.getText()) && Pattern.matches("^[\\w\\.]{1,20}$", passwordField.getText())) {
            try {
                network.write(new AuthRequest(loginField.getText(), passwordField.getText()));
            } catch (IOException e) {
                System.out.println("Authentication request failed");
                e.printStackTrace();
            }
        } else {
                wrongCredentialsLabel.setVisible(true);
                wrongFormatAlert();
                System.out.println("Wrong format of login or password");
            }
    }

    //todo: включение видимости пароля
    //todo: возврат к вводу логина, если передумал регистрироваться
    public void registrationRequest(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            Platform.runLater(() -> {
                serverFilesTable.getItems().clear();
                loginField.setVisible(true);
                passwordField.setVisible(true);
                loginButton.setText("Create");
                regLabel.setText("Use only a-Z 0-9 . _");
                regLabel.setDisable(true);
                wrongCredentialsLabel.setVisible(false);
                loginButton.setOnMouseClicked((EventHandler) -> {
                    if (Pattern.matches("^[\\w\\.]{1,20}$", loginField.getText()) && Pattern.matches("^[\\w\\.]{1,20}$", passwordField.getText())) {
                        try {
                            network.write(new RegistrationRequest(loginField.getText(), passwordField.getText()));
                        } catch (IOException e) {
                            System.out.println("Registration request failed");
                            e.printStackTrace();
                        }
                    } else {
                        Platform.runLater(() -> {
                            wrongCredentialsLabel.setVisible(true);
                            wrongFormatAlert();
                            System.out.println("Wrong format of login or password");
                        });
                    }
                });
            });
        }
    }

    private void authorizationProceed(AuthOK authOK) {
        Platform.runLater(() -> {
            isAuthorized = true;
            loginLabel.setText("login: " + authOK.getLogin());
            wrongCredentialsLabel.setVisible(false);
            loginField.setVisible(false);
            passwordField.setVisible(false);
            regLabel.setVisible(false);
            loginButton.setOnMouseClicked((EventHandler) event -> logoutRequest());
            loginButton.setText("Log out");
        });
        System.out.println("Authorization successful");
    }

    //todo: объяснение причины фейла (что конкретно неверное - логин, пароль, попытка зарегать существующий логин)
    private void authorizationOrRegistrationFailed() {
        Platform.runLater(() -> {
            wrongCredentialsLabel.setVisible(true);
            wrongCredentialsAlert();
            loginField.clear();
            passwordField.clear();
        });
        System.out.println("Authorization or registration failed: credentials are incorrect");
    }


    private void wrongCredentialsAlert() {
        Platform.runLater(()-> {
            Alert wrongCredentialsAlert = new Alert(Alert.AlertType.ERROR);
            wrongCredentialsAlert.setTitle("Authentication failed");
            wrongCredentialsAlert.setHeaderText("Wrong login or password");
            wrongCredentialsAlert.setContentText("Please, try again. Use only a-Z 0-9 . _");
            wrongCredentialsAlert.showAndWait();
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
            loginField.clear();
            passwordField.clear();
            loginField.setVisible(true);
            passwordField.setVisible(true);
            loginButton.setText("Log in");
            loginButton.setOnMouseClicked((EventHandler) this::authorizationRequest);
            regLabel.setText("Create account");
            regLabel.setDisable(false);
            regLabel.setVisible(true);
        });
        System.out.println("Logout successful");
    }

    //todo: процесс загрузки файла - отображение
    public void uploadFile(MouseEvent mouseEvent) {
        if (isAuthorized) {
            try {
                String selectedFile = localFilesTable.getSelectionModel().getSelectedItem().getFileName();
                if (selectedFile != null) {
                    network.write(new FileContainer(Path.of(dir).resolve(selectedFile)));
                    System.out.println("Upload successful");
                }
            } catch (IOException e) {
                System.out.println("Upload unsuccessful");
                e.printStackTrace();
            }
        }
    }

    //todo: процесс загрузки файла - отображение
    public void downloadFile(MouseEvent mouseEvent) {
        if (isAuthorized) {
            try {
                String selectedFile = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                if (selectedFile != null) {
                    System.out.println("Download started: " + selectedFile);
                    network.write(new FileRequest(selectedFile));
                }
            } catch (IOException e) {
                System.out.println("File was not accepted");
                e.printStackTrace();
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

    public void goUpLocally(MouseEvent mouseEvent) {
        if (!dir.equals(CommandsAndConstants.LOCALROOTDIRECTORY)) {
            this.dir = Path.of(dir).getParent().toString();
            showLocalFiles();
        } else {
            showLocalFiles();
        }
    }

    public void checkLocalDirectory(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
                Path selectedPath = Path.of(dir).resolve(localFilesTable.getSelectionModel().getSelectedItem().getFileName());
                if (Files.isDirectory(selectedPath)) {
                    this.dir = selectedPath.toString();
                    showLocalFiles();
                }
            }
        }
    }

    public void goUpOnServer(MouseEvent mouseEvent) {
        if (isAuthorized) {
            try {
                network.write(new PathUpRequest());
            } catch (IOException e) {
                System.out.println("Server navigation error");
                e.printStackTrace();
            }
        }
    }

    public void checkServerDirectory(MouseEvent mouseEvent) {
        if (isAuthorized) {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                String selectedFile = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                if (selectedFile != null) {
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

    public void createNewLocalDirectory(MouseEvent mouseEvent) {
        TextInputDialog newDirectoryNameDialog = new TextInputDialog("NewFolder");
        newDirectoryNameDialog.setTitle("Create new local folder");
        newDirectoryNameDialog.setHeaderText("Use only a-Z 0-9 . _\nFolder name:");
        newDirectoryNameDialog.setContentText("-->");
        Optional<String> result = newDirectoryNameDialog.showAndWait();
        if (result.isPresent()) {
            String newDirectoryName = result.get();
            if (Pattern.matches("^[\\w\\.]{1,255}$", newDirectoryName)) {
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
                wrongFormatAlert();
            }
        }
    }

    public void createNewServerDirectory(MouseEvent mouseEvent) {
        if (isAuthorized) {
            TextInputDialog newDirectoryNameDialog = new TextInputDialog("NewFolder");
            newDirectoryNameDialog.setTitle("Create new server folder");
            newDirectoryNameDialog.setHeaderText("Use only a-Z 0-9 . _\nFolder name:");
            newDirectoryNameDialog.setContentText("-->");
            Optional<String> result = newDirectoryNameDialog.showAndWait();
            if (result.isPresent()) {
                String newDirectoryName = result.get();
                if (Pattern.matches("^[\\w\\.]{1,255}$", newDirectoryName)) {
                    try {
                        network.write(new NewFolderRequest(newDirectoryName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("User created a new server folder: " + newDirectoryName);
                } else {
                    wrongFormatAlert();
                }
            }
        }
    }

    private void newFolderCreationFailedAlert() {
        Platform.runLater(()-> {
            Alert newFolderCreationFailed = new Alert(Alert.AlertType.ERROR);
            newFolderCreationFailed.setTitle("New folder was not created");
            newFolderCreationFailed.setHeaderText("The folder with this name already exists");
            newFolderCreationFailed.setContentText("");
            newFolderCreationFailed.showAndWait();
        });
    }

    public void renameSelectedLocalFileOrDirectory(MouseEvent mouseEvent) {
        if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
            String oldName = dir + "/" + localFilesTable.getSelectionModel().getSelectedItem().getFileName();
            TextInputDialog renameDialog = new TextInputDialog("NewName." + FilenameUtils.getExtension(oldName));
            renameDialog.setTitle("Rename File or Directory");
            renameDialog.setHeaderText("Use only a-Z 0-9 . _\nNew name:");
            renameDialog.setContentText("-->");
            Optional<String> result = renameDialog.showAndWait();
            if (result.isPresent()) {
                String newName = result.get();
                if (Pattern.matches("^[\\w\\.]{1,255}$", newName)) {
                    newName = dir + "/" + newName;
                    Path source = Path.of(oldName);
                    try {
                        Files.move(source, source.resolveSibling(newName));
                        showLocalFiles();
                    } catch (IOException e) {
                        System.out.println("File rename failed");
                        e.printStackTrace();
                    }
                } else {
                    wrongFormatAlert();
                }
            }
        }
    }

    public void renameSelectedServerFileOrDirectory(MouseEvent mouseEvent) {
        if (isAuthorized) {
            if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
                String oldName = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                TextInputDialog renameDialog = new TextInputDialog("NewName." + FilenameUtils.getExtension(oldName));
                renameDialog.setTitle("Rename File or Directory");
                renameDialog.setHeaderText("Use only a-Z 0-9 . _\nNew name:");
                renameDialog.setContentText("-->");
                Optional<String> result = renameDialog.showAndWait();
                if (result.isPresent()) {
                    String newName = result.get();
                    if (Pattern.matches("^[\\w\\.]{1,255}$", newName)) {
                        try {
                            network.write(new RenameRequest(oldName, newName));
                        } catch (IOException e) {
                            System.out.println("File rename failed");
                            e.printStackTrace();
                        }
                    } else {
                        wrongFormatAlert();
                    }
                }
            }
        }
    }

    public void deleteSelectedLocalFileOrDirectory(MouseEvent mouseEvent) {
        if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
            String itemToDelete = localFilesTable.getSelectionModel().getSelectedItem().getFileName();
            try {
                Files.deleteIfExists(Path.of(dir + "/" + itemToDelete));
                showLocalFiles();
            } catch (FileNotFoundException f) {
                noSuchFileAlert();
                System.out.println("File not found");
            } catch (DirectoryNotEmptyException d) {
                directoryNotEmptyAlert();
                System.out.println("Folder is not empty");
            } catch (IOException e) {
                System.out.println("Deletion unsuccessful");
            }
        }
    }

    //todo ошибка с access denied при стирании папок на уровень внутрь
    public void deleteSelectedServerFileOrDirectory(MouseEvent mouseEvent) throws IOException {
        if (isAuthorized) {
            String itemToDelete = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
            if (itemToDelete != null) {
                network.write(new DeleteRequest(itemToDelete));
            }
        }
    }

    private void deleteFailed(DeleteFailed deleteFailed) {
        if (deleteFailed.isDirectoryNotEmpty) {
            directoryNotEmptyAlert();
        } else {
            noSuchFileAlert();
        }
    }

    private void directoryNotEmptyAlert() {
        Platform.runLater(()-> {
            Alert directoryNotEmptyAlert = new Alert(Alert.AlertType.ERROR);
            directoryNotEmptyAlert.setTitle("Deletion Error");
            directoryNotEmptyAlert.setHeaderText("Folder is not empty");
            directoryNotEmptyAlert.setContentText("");
            directoryNotEmptyAlert.showAndWait();
        });
    }

    private void noSuchFileAlert() {
        Platform.runLater(()-> {
            Alert noSuchFileAlert = new Alert(Alert.AlertType.ERROR);
            noSuchFileAlert.setTitle("Deletion Error");
            noSuchFileAlert.setHeaderText("The file does not exist");
            noSuchFileAlert.setContentText("");
            noSuchFileAlert.showAndWait();
        });
    }
}
