package ru.masaviktoria.pandorasboxapplication;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import ru.masaviktoria.pandorasboxmodel.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoxController implements Initializable {

    private Network network;

    @FXML
    public VBox vBox;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    public ImageView pandorasBoxLogo;
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
    //нужно допилить структуру папок, пока реализован простой список
    /*    @FXML
    public TreeView<FileListInfo> localTreeView;
    @FXML
    public TreeView<FileListInfo> serverTreeView;*/
    @FXML
    public ListView<String> localListView;
    @FXML
    public ListView<String> serverListView;
    @FXML
    public TextArea selectedFileInfoArea;

    private String dir;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            network = new Network(CommandsAndConstants.PORT);
            Thread localFilesListThread = new Thread(this::showLocalFiles);
            localFilesListThread.setDaemon(true);
            localFilesListThread.start();
            Thread readServerMessagesThread = new Thread(this::readMessagesFromServer);
            readServerMessagesThread.setDaemon(true);
            readServerMessagesThread.start();
        } catch (IOException e) {
            System.out.println("Initialization or runtime fail");
            e.printStackTrace();
        }
    }

    private void showLocalFiles() {
        Platform.runLater(() -> {
            this.dir = System.getProperty("user.home");
            localListView.getItems().clear();
            try {
                localListView.getItems().addAll(streamFiles(dir, 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Set<String> streamFiles(String directory, int depth) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(directory), depth)) {
            return stream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }

    private void readMessagesFromServer() {
        try {
            while (true) {
                BoxMessage message = network.read();
                if (message instanceof AuthOK) {
                    authOK();
                } else if (message instanceof AuthFailed) {
                    authFailed();
                } else if (message instanceof FileList fileList) {
                    showServerFiles(fileList);
                } else if (message instanceof LogoutOK) {
                    logoutOK();
                } else if (message instanceof FileInfomationField fileInfomationField) {
                    showFileInformation(fileInfomationField);
                } else if (message instanceof FileContainer fileContainer) {
                    saveFileFromContainer(fileContainer);
                }
            }
        } catch (Exception e) {
            System.out.println("Reading of server messages was interrupted");
            e.printStackTrace();
        }
    }

    public void authentication(Event mouseEvent) {
        try {
            network.write(new AuthRequest(loginField.getText(), passwordField.getText()));
        } catch (IOException e) {
            System.out.println("Authentication request failed");
            e.printStackTrace();
        }
    }

    // пока так, потом думаю прикрутить включение видимости пароля
    public void registration(MouseEvent mouseEvent) {
        Platform.runLater(() -> {
            serverListView.getItems().clear();
            loginField.setVisible(true);
            passwordField.setVisible(true);
            loginButton.setText("Create");
            regLabel.setText("Use only a-Z and 0-9");
            regLabel.setDisable(true);
            wrongCredentialsLabel.setVisible(false);
            loginButton.setOnMouseClicked((EventHandler) -> {
                if (Pattern.matches("[a-zA-Z0-9]*", loginField.getText()) && Pattern.matches("[a-zA-Z0-9]*", passwordField.getText())) {
                    try {
                        network.write(new RegistrationRequest(loginField.getText(), passwordField.getText()));
                    } catch (IOException e) {
                        System.out.println("Registration request failed");
                        e.printStackTrace();
                    }
                } else {
                    Platform.runLater(() -> {
                        wrongCredentialsLabel.setVisible(true);
                        System.out.println("Wrong format of login or password");
                    });
                }
            });
        });
    }

    private void authOK() {
        Platform.runLater(() -> {
            wrongCredentialsLabel.setVisible(false);
            loginField.setVisible(false);
            passwordField.setVisible(false);
            regLabel.setVisible(false);
            loginButton.setOnMouseClicked((EventHandler) event -> logout());
            loginButton.setText("Log out");
        });
        System.out.println("Authentication successful");
    }

//неплохо бы допилить объяснение причины фейла (что неверное - логин, пароль, запрещенные символы при регистрации или попытка зарегать существующий логин)
    private void authFailed() {
        Platform.runLater(() -> {
            wrongCredentialsLabel.setVisible(true);
            loginField.clear();
            passwordField.clear();
        });
        System.out.println("Authentication failed: login or password is incorrect");
    }

    private void logout() {
        try {
            network.write(new LogoutRequest());
        } catch (IOException e) {
            System.out.println("Logout request failed");
            e.printStackTrace();
        }
    }

    private void logoutOK() {
        Platform.runLater(() -> {
            serverListView.getItems().clear();
            loginField.clear();
            passwordField.clear();
            loginField.setVisible(true);
            passwordField.setVisible(true);
            loginButton.setText("Log in");
            loginButton.setOnMouseClicked((EventHandler) this::authentication);
            regLabel.setText("Create account");
            regLabel.setDisable(false);
            regLabel.setVisible(true);
        });
        System.out.println("Logout successful");
    }

    private void showServerFiles(FileList fileList) {
        Platform.runLater(() -> {
            serverListView.getItems().clear();
            serverListView.getItems().addAll(fileList.getFiles());
        });
    }

    private void showFileInformation(FileInfomationField fileInfomationField) {
        Platform.runLater(() -> {
            selectedFileInfoArea.setText(fileInfomationField.getInformation());
        });
    }

    public void uploadFile(MouseEvent mouseEvent) {
        try {
            String selectedFile = localListView.getSelectionModel().getSelectedItem();
            network.write(new FileContainer(Path.of(dir).resolve(selectedFile)));
            System.out.println("Upload successful");
        } catch (IOException e) {
            System.out.println("Upload unsuccessful");
            e.printStackTrace();
        }
    }

    public void downloadFile(MouseEvent mouseEvent) {
        try {
            String selectedFile = serverListView.getSelectionModel().getSelectedItem();
            System.out.println("Download started: " + selectedFile);
            network.write(new FileRequest(selectedFile));
        } catch (IOException e) {
            System.out.println("File was not accepted");
            e.printStackTrace();
        }
    }

    private void saveFileFromContainer(FileContainer fileContainer) {
        try {
            Path current = Path.of(dir).resolve(fileContainer.getFileName());
            Files.write(current, fileContainer.getData());
            System.out.println("Received file: " + fileContainer.getFileName());
            Platform.runLater(() -> {
                localListView.getItems().clear();
                try {
                    localListView.getItems().addAll(streamFiles(dir, 1));
                } catch (IOException e) {
                    System.out.println("Local files listing error");
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            System.out.println("File saving unsuccessful");
            e.printStackTrace();
        }
    }

    public void goUpLocally(MouseEvent mouseEvent) {
        String parentDir = Path.of(dir).getParent().toString();
        Platform.runLater(() -> {
            try {
                localListView.getItems().clear();
                localListView.getItems().addAll(streamFiles(parentDir, 1));
            } catch (IOException e) {
                System.out.println("Local navigation error");
                e.printStackTrace();
            }
        });
        this.dir = parentDir;
    }

    public void checkLocalDirectory(MouseEvent mouseEvent) {
        Path selectedPath = Path.of(dir).resolve(localListView.getSelectionModel().getSelectedItem());
        if (Files.isDirectory(selectedPath)) {
            Platform.runLater(() -> {
                try {
                    localListView.getItems().clear();
                    localListView.getItems().addAll(streamFiles(selectedPath.toString(), 1));
                } catch (IOException e) {
                    System.out.println("Local navigation error");
                    e.printStackTrace();
                }
            });
            this.dir = selectedPath.toString();
        }
    }

    public void goUpOnServer(MouseEvent mouseEvent) {
        try {
            String selectedFile = serverListView.getSelectionModel().getSelectedItem();
            network.write(new PathUpRequest(selectedFile));
        } catch (IOException e) {
            System.out.println("Server navigation error");
            e.printStackTrace();
        }
    }

    public void checkServerDirectory(MouseEvent mouseEvent) {
        try {
            String selectedFile = serverListView.getSelectionModel().getSelectedItem();
            network.write(new PathInRequest(selectedFile));
        } catch (IOException e) {
            System.out.println("Server navigation error");
            e.printStackTrace();
        }
    }
}

