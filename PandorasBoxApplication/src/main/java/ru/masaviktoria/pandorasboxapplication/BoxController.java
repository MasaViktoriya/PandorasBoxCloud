package ru.masaviktoria.pandorasboxapplication;

import javafx.application.Platform;
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
import ru.masaviktoria.pandorasboxmodel.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class BoxController implements Initializable {

    private Network network;
    private String dir;

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
    //todo: допилить структуру папок, пока реализован простой список
    /*@FXML
    public TreeView<FileListInfo> localTreeView;
    @FXML
    public TreeView<FileListInfo> serverTreeView;*/
    @FXML
    public ListView<String> localListView;
    @FXML
    public ListView<String> serverListView;
    @FXML
    public TextArea selectedFileInfoArea;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            network = new Network(CommandsAndConstants.PORT);
            Thread localFilesListThread = new Thread(this::showLocalFiles);
            localFilesListThread.setDaemon(true);
            localFilesListThread.start();
            Thread readServerCommandsThread = new Thread(this::readCommandsFromServer);
            readServerCommandsThread.setDaemon(true);
            readServerCommandsThread.start();
        } catch (IOException e) {
            System.out.println("Initialization or runtime fail");
            e.printStackTrace();
        }
    }

    private void showLocalFiles() {
        Platform.runLater(() -> {
            this.dir = CommandsAndConstants.LOCALROOTDIRECTORY;
            localListView.getItems().clear();
            localListView.getItems().addAll(getFilesListFromDirectory(dir));
        });
    }

    private List<String> getFilesListFromDirectory(String dir) {
        String[] list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }

    //заменила метод на более простой, без глубины
/*    private Set<String> streamFiles(String directory, int depth) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(directory), depth)) {
            return stream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }*/

    private void readCommandsFromServer() {
        try {
            while (true) {
                BoxCommand boxCommand = network.read();
                if (boxCommand instanceof AuthOK) {
                    authorizationProceed();
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
                }
            }
        } catch (Exception e) {
            System.out.println("Reading of server messages was interrupted");
            e.printStackTrace();
        }
    }

    //todo: указание на запрещенные символы при авторизации
    public void authorizationRequest(Event mouseEvent) {
        if (Pattern.matches("[a-zA-Z0-9]*", loginField.getText()) && Pattern.matches("[a-zA-Z0-9]*", passwordField.getText())) {
            try {
                network.write(new AuthRequest(loginField.getText(), passwordField.getText()));
            } catch (IOException e) {
                System.out.println("Authentication request failed");
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                wrongCredentialsLabel.setVisible(true);
                System.out.println("Wrong format of login or password");
            });
        }
    }

    //todo: включение видимости пароля
    //todo: указание на запрещенные символы при регистрации
    //todo: возврат к вводу логина, если передумал регистрироваться
    public void registrationRequest(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
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
    }

    //todo: отображение логина юзера в интерфейсе
    private void authorizationProceed() {
        Platform.runLater(() -> {
            wrongCredentialsLabel.setVisible(false);
            loginField.setVisible(false);
            passwordField.setVisible(false);
            regLabel.setVisible(false);
            loginButton.setOnMouseClicked((EventHandler) event -> logoutRequest());
            loginButton.setText("Log out");
        });
        System.out.println("Authorization successful");
    }

    //todo: объяснение причины фейла (что неверное - логин, пароль, попытка зарегать существующий логин)
    private void authorizationOrRegistrationFailed() {
        Platform.runLater(() -> {
            wrongCredentialsLabel.setVisible(true);
            loginField.clear();
            passwordField.clear();
        });
        System.out.println("Authorization or registration failed: credentials are incorrect");
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
            serverListView.getItems().clear();
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

    //todo: проверка пустой папки, перехват NPE
    private void showServerFiles(FileList fileList) {
        Platform.runLater(() -> {
            serverListView.getItems().clear();
            serverListView.getItems().addAll(fileList.getFiles());
        });
    }

    //todo: показ информации о файле в нижнем окне
    private void showFileInformation(FileInfomationField fileInfomationField) {
       /* Platform.runLater(() -> {
            selectedFileInfoArea.setText(fileInfomationField.getInformation());
        });*/
    }

    //todo: процесс загрузки файла - графическое отображение
    public void uploadFile(MouseEvent mouseEvent) {
        try {
            String selectedFile = localListView.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                network.write(new FileContainer(Path.of(dir).resolve(selectedFile)));
                System.out.println("Upload successful");
            }
        } catch (IOException e) {
            System.out.println("Upload unsuccessful");
            e.printStackTrace();
        }
    }

    //todo: процесс загрузки файла - графическое отображение
    public void downloadFile(MouseEvent mouseEvent) {
        try {
            String selectedFile = serverListView.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                System.out.println("Download started: " + selectedFile);
                network.write(new FileRequest(selectedFile));
            }
        } catch (IOException e) {
            System.out.println("File was not accepted");
            e.printStackTrace();
        }
    }

    private void saveFileFromContainer(FileContainer fileContainer) {
        try {
            Path current = Path.of(dir).resolve(fileContainer.getFileName());
            Files.write(current, fileContainer.getFileData());
            System.out.println("Received file: " + fileContainer.getFileName());
            Platform.runLater(() -> {
                localListView.getItems().clear();
                localListView.getItems().addAll(getFilesListFromDirectory(dir));
            });
        } catch (IOException e) {
            System.out.println("File saving unsuccessful");
            e.printStackTrace();
        }
    }

    public void goUpLocally(MouseEvent mouseEvent) {
        if (!dir.equals(CommandsAndConstants.LOCALROOTDIRECTORY)) {
            String parentDir = Path.of(dir).getParent().toString();
            Platform.runLater(() -> {
                localListView.getItems().clear();
                localListView.getItems().addAll(getFilesListFromDirectory(parentDir));
            });
            this.dir = parentDir;
        } else {
            Platform.runLater(() -> {
                localListView.getItems().clear();
                localListView.getItems().addAll(getFilesListFromDirectory(dir));
            });
        }
    }

    public void checkLocalDirectory(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            if (localListView.getSelectionModel().getSelectedItem() != null) {
                Path selectedPath = Path.of(dir).resolve(localListView.getSelectionModel().getSelectedItem());
                if (Files.isDirectory(selectedPath)) {
                    Platform.runLater(() -> {
                        localListView.getItems().clear();
                        localListView.getItems().addAll(getFilesListFromDirectory(selectedPath.toString()));
                    });
                    this.dir = selectedPath.toString();
                }
            }
        }
    }

    public void goUpOnServer(MouseEvent mouseEvent) {
        try {
            network.write(new PathUpRequest());
        } catch (IOException e) {
            System.out.println("Server navigation error");
            e.printStackTrace();
        }
    }

    public void checkServerDirectory(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            String selectedFile = serverListView.getSelectionModel().getSelectedItem();
            if(selectedFile != null) {
                try {
                    network.write(new PathInRequest(selectedFile));
                } catch (IOException e) {
                    System.out.println("Server navigation error");
                    e.printStackTrace();
                }
            }
        }
    }

    //todo: кнопка Delete для сервера
    //todo: кнопка Delete для локальных файлов
    //todo: кнопка New для сервера
    //todo: кнопка New для локальных файлов
    //todo: кнопка Rename для сервера
    //todo: кнопка Rename для локальных файлов
}

