package ru.masaviktoria.pandorasboxapplication;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class BoxController implements Initializable, CallBackInterface {

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
    public TableColumn<FileListMappingInfo, String> localLastModifiedColumn;
    @FXML
    public TableView<FileListMappingInfo> serverFilesTable;
    @FXML
    public TableColumn<FileListMappingInfo, ImageView> serverTypeColumn;
    @FXML
    public TableColumn<FileListMappingInfo, String> serverFileOrDirectoryNameColumn;
    @FXML
    public TableColumn<FileListMappingInfo, Long> serverFileSizeColumn;
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

    public String dir;
    private boolean isAuthorized = false;
    private NetworkService networkService;
    private FileService fileService;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.dir = ClientConstants.LOCALROOTDIRECTORY;
        this.networkService = new NetworkService();
        this.fileService = new FileService();
        startLocalNavigation();
        startServerNavigation();
        listenToCallbacks();
    }

    private void listenToCallbacks() {
        Thread listenToCallbacksThread = new Thread(() -> networkService.readCommandsFromServer(BoxController.this));
        listenToCallbacksThread.setDaemon(true);
        listenToCallbacksThread.start();
    }

    @Override
    public void handleCallbacks(BoxCommand boxCommand) {
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
            downloadFileProceed(fileContainer);
        } else if (boxCommand instanceof NewDirectoryFailed) {
            newDirectoryFailed();
        } else if (boxCommand instanceof RenameFailed) {
            renameFailed();
        } else if (boxCommand instanceof DeleteFailed) {
            deleteFailed();
        }
    }

    private void startLocalNavigation() {
        Thread localFilesListThread = new Thread(() -> {
            createLocalFilesTable();
            showLocalFiles();
        });
        localFilesListThread.setDaemon(true);
        localFilesListThread.start();
    }

    private void startServerNavigation() {
        Thread serverFilesListThread = new Thread(this::createServerFilesTable);
        serverFilesListThread.setDaemon(true);
        serverFilesListThread.start();
    }

    private void createFilesTable(TableView<FileListMappingInfo> table, TableColumn<FileListMappingInfo, ImageView> type, TableColumn<FileListMappingInfo, String> name, TableColumn<FileListMappingInfo, Long> size, TableColumn<FileListMappingInfo, String> lastModified){
        Platform.runLater(() -> {
            type.setCellValueFactory(param -> {
                String typeName = param.getValue().getFileType().getName();
                if (!typeName.isEmpty()) {
                    return new SimpleObjectProperty<>(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("foldericon.png")))));
                } else {
                    return new SimpleObjectProperty<>(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("fileicon.jpg")))));
                }
            });
            name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
            size.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getFileSize()));
            size.setCellFactory(column -> {
                return new TableCell<>() {
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
            lastModified.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(ClientConstants.DTF)));
            table.getSortOrder().add(size);
        });
    }

    private void createLocalFilesTable() {
        createFilesTable(localFilesTable, localTypeColumn, localFileOrDirectoryNameColumn, localFileSizeColumn, localLastModifiedColumn);
    }

    private void createServerFilesTable() {
        createFilesTable(serverFilesTable, serverTypeColumn, serverFileOrDirectoryNameColumn, serverFileSizeColumn, serverLastModifiedColumn);
    }

    @FXML
    private void showLocalFiles() {
        Platform.runLater(() -> {
                localCurrentFolderTextField.setText("Current: " + fileService.getCurrentDirectory(dir));
                localFilesTable.getItems().clear();
                try {
                localFilesTable.getItems().addAll(fileService.getFileList(dir));
                } catch (IOException e) {
                    System.out.println("File listing error");
                    e.printStackTrace();
                }
                localFilesTable.sort();
                localFilesTable.scrollTo(0);
        });
    }

    void showServerFiles(FileList fileList) {
        Platform.runLater(() -> {
            String currentDir = fileList.getCurrentDir().substring(13);
            serverCurrentFolderTextField.setText("Current: " + currentDir);
            serverFilesTable.getItems().clear();
            serverFilesTable.getItems().addAll(fileList.getFiles());
            serverFilesTable.sort();
            serverFilesTable.scrollTo(0);
        });
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
    private void startAuthenticationByButton() {
        authenticationRequest(ClientConstants.AUTHORIZE);
    }

    @FXML
    private void startAuthenticationByEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (!returnToLoginLabel.isVisible()) {
                authenticationRequest(ClientConstants.AUTHORIZE);
            } else {
                authenticationRequest(ClientConstants.REGISTER);
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
        showPasswordLabel.setOnMouseClicked((EventHandler) -> hidePassword());
    }

    private void hidePassword() {
        Platform.runLater(() -> {
            passwordVisibleField.setVisible(false);
            String pass = passwordVisibleField.getText();
            passwordVisibleField.clear();
            passwordField.setVisible(true);
            passwordField.setText(pass);
            showPasswordLabel.setText("Show password");
            showPasswordLabel.setOnMouseClicked((EventHandler) -> showPassword());
        });
    }

    private void authenticationRequest(String type) {
        String login = loginField.getText();
        String password = passwordField.getText();
        String passwordVisible = passwordVisibleField.getText();
        if (!password.isEmpty()) {
            if (RegExMatchers.credentialsMatcher(login) && RegExMatchers.credentialsMatcher(password)) {
                if (type.equals(ClientConstants.AUTHORIZE)) {
                    networkService.sendAuthRequest(login, password);
                } else {
                    networkService.sendRegistrationRequest(login, password);
                }
            } else {
                Alerts.wrongCredentialsFormatAlert();
            }
        } else {
            if (RegExMatchers.credentialsMatcher(login) && RegExMatchers.credentialsMatcher(passwordVisible)) {
                if (type.equals(ClientConstants.AUTHORIZE)) {
                    networkService.sendAuthRequest(login, passwordVisible);
                } else {
                    networkService.sendRegistrationRequest(login, passwordVisible);
                }
            } else {
                Alerts.wrongCredentialsFormatAlert();
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
            loginButton.setOnMouseClicked((EventHandler) -> authenticationRequest(ClientConstants.REGISTER));
        });
    }

    @FXML
    private void returnToLogin() {
        returnToLoginLabel.setVisible(false);
        loginButton.setText("Log in");
        loginButton.setOnMouseClicked((EventHandler) -> authenticationRequest(ClientConstants.AUTHORIZE));
        regLabel.setText("Create account");
        regLabel.setDisable(false);
        wrongCredentialsLabel.setVisible(false);
    }

    void authorizationProceed(AuthOK authOK) {
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
            loginButton.setOnMouseClicked((EventHandler) -> networkService.sendLogoutRequest());
            loginButton.setText("Log out");
        });
        System.out.println("Authorization successful");
    }

    void authorizationFailed() {
        Platform.runLater(() -> {
            wrongCredentialsLabel.setVisible(true);
            Alerts.wrongCredentialsAlert();
            loginField.clear();
            passwordField.clear();
            passwordVisibleField.clear();
        });
    }

    void registrationFailed() {
        Platform.runLater(() -> {
            wrongCredentialsLabel.setVisible(true);
            Alerts.loginAlreadyExistsAlert();
            loginField.clear();
            passwordField.clear();
            passwordVisibleField.clear();
        });
    }

    void logoutProceed() {
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
            showPasswordLabel.setOnMouseClicked((EventHandler) -> showPassword());
            loginButton.setText("Log in");
            loginButton.setOnMouseClicked((EventHandler) -> authenticationRequest(ClientConstants.AUTHORIZE));
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
    private void uploadFileRequest() {
        if (isAuthorized) {
            if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
                String selectedFile = localFilesTable.getSelectionModel().getSelectedItem().getFileName();
                try {
                    networkService.sendFileContainer(fileService.uploadFile(selectedFile, dir));
                } catch (IOException e) {
                    System.out.println("Upload error");
                    e.printStackTrace();
                }
            }
        }
    }

    //todo: процесс загрузки файла - отображение
    @FXML
    private void downloadFileRequest() {
        if (isAuthorized) {
            if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
                String selectedFile = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                networkService.sendFileRequest(selectedFile);
            }
        }
    }

    private void downloadFileProceed(FileContainer fileContainer) {
        fileService.saveFileFromContainer(fileContainer, dir);
        showLocalFiles();
    }

    @FXML
    private void goUpLocally() {
        String parentDir = fileService.getParentDirIfPossible(dir);
        if(!parentDir.isEmpty()){
            this.dir = parentDir;
            showLocalFiles();
        }
        selectedFilePath.clear();
    }

    @FXML
    private void getIntoLocalDirectory(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
                String selectedItem = localFilesTable.getSelectionModel().getSelectedItem().getFileName();
                String checkDir = fileService.checkLocalDirectory(selectedItem, dir);
                if(!checkDir.isEmpty()){
                    this.dir = checkDir;
                    showLocalFiles();
                    selectedFilePath.clear();
                }
            }
        }
    }

    @FXML
    private void showSelectedFilePath() {
        if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
            String selectedFile = localFilesTable.getSelectionModel().getSelectedItem().getFileName();
            String path = fileService.getSelectedFilePath(selectedFile, dir);
            selectedFilePath.setText(path);
        }
    }

    //todo: показ ссылки на файл на сервере (sharing link)

    @FXML
    private void goUpOnServer() {
        if (isAuthorized) {
            networkService.sendPathUpRequest();
        }
    }

    @FXML
    private void getIntoServerDirectoryRequest(MouseEvent mouseEvent) {
        if (isAuthorized) {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
                    String selectedFile = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                    networkService.sendPathInRequest(selectedFile);
                }
            }
        }
    }


    //todo может не работать, так как не указан платформенный тред, проверить
    @FXML
    private void createNewLocalDirectoryRequest() {
        String newDirectoryName = Prompts.createNewDirectoryPrompt(ClientConstants.LOCAL);
        if (!newDirectoryName.isEmpty()) {
            if (RegExMatchers.nameMatcher(newDirectoryName)) {
                if (!fileService.createNewLocalDirectory(newDirectoryName, dir)) {
                    Alerts.newFolderCreationFailedAlert();
                } else {
                    showLocalFiles();
                }
            } else {
                Alerts.wrongFileOrDirectoryNameFormatAlert();
            }
        }
    }

    //todo может не работать, так как не указан платформенный тред, проверить
    @FXML
    private void createNewServerDirectoryRequest() {
        if (isAuthorized) {
            String newDirectoryName = Prompts.createNewDirectoryPrompt(ClientConstants.SERVER);
            if (!newDirectoryName.isEmpty()) {
                if (RegExMatchers.nameMatcher(newDirectoryName)) {
                    networkService.sendNewDirectoryRequest(newDirectoryName);
                } else {
                    Alerts.wrongFileOrDirectoryNameFormatAlert();
                }
            }
        }
    }

    protected void newDirectoryFailed() {
        Platform.runLater(Alerts::newFolderCreationFailedAlert);
    }

    //todo может не работать, так как не указан платформенный тред, проверить
    @FXML
    private void renameSelectedLocalFileOrDirectoryRequest() {
        if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
            String oldName = localFilesTable.getSelectionModel().getSelectedItem().getFileName();
            String newName = Prompts.renameDirectoryPrompt();
            if (!newName.isEmpty()) {
                if (RegExMatchers.nameMatcher(newName)) {
                    if (fileService.renameLocalFileOrDirectory(newName, oldName, dir)) {
                        showLocalFiles();
                    } else {
                        Alerts.renameFailedAlert();
                    }
                } else {
                    Alerts.wrongFileOrDirectoryNameFormatAlert();
                }
            }
        }
    }

    //todo может не работать, так как не указан платформенный тред, проверить
    @FXML
    private void renameSelectedServerFileOrDirectoryRequest() {
        if (isAuthorized) {
            if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
                String oldName = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                String newName = Prompts.renameDirectoryPrompt();
                if (!newName.isEmpty()) {
                    if (RegExMatchers.nameMatcher(newName)) {
                        newName = newName + "." + FilenameUtils.getExtension(oldName);
                        networkService.sendRenameRequest(oldName, newName);
                    } else {
                        Alerts.wrongFileOrDirectoryNameFormatAlert();
                    }
                }
            }
        }
    }

    protected void renameFailed() {
        Platform.runLater(Alerts::renameFailedAlert);
    }

    //todo может не работать, так как не указан платформенный тред, проверить
    @FXML
    private void deleteSelectedLocalFileOrDirectoryRequest() {
        if (localFilesTable.getSelectionModel().getSelectedItem() != null) {
            String itemToDelete = localFilesTable.getSelectionModel().getSelectedItem().getFileName();
            if (Prompts.deletePrompt(itemToDelete)) {
                if (!fileService.deleteLocalFileOrDirectory(itemToDelete, dir)) {
                    Alerts.noSuchFileAlert();
                }
            }
            showLocalFiles();
        }
    }

    //todo может не работать, так как не указан платформенный тред, проверить
    @FXML
    private void deleteSelectedServerFileOrDirectoryRequest() {
        if (isAuthorized) {
            if (serverFilesTable.getSelectionModel().getSelectedItem() != null) {
                String itemToDelete = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
                if (Prompts.deletePrompt(itemToDelete)) {
                    networkService.sendDeleteRequest(itemToDelete);
                }
            }
        }
    }

    protected void deleteFailed() {
        Platform.runLater(Alerts::noSuchFileAlert);
    }
}

    //todo: UUID для файлов, запись в базу, переименование и удаление на сервере через UUID