package ru.masaviktoria.pandorasboxapplication;

import javafx.application.Platform;
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
                if (message instanceof FileList fileList) {
                    Platform.runLater(() -> {
                        serverListView.getItems().clear();
                        serverListView.getItems().addAll(fileList.getFiles());
                    });
                } else if (message instanceof FileMessage fileMessage) {
                    Path current = Path.of(dir).resolve(fileMessage.getFileName());
                    Files.write(current, fileMessage.getData());
                    Platform.runLater(() -> {
                        localListView.getItems().clear();
                        try {
                            localListView.getItems().addAll(streamFiles(dir, 1));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Reading of server messages was interrupted");
            e.printStackTrace();
        }
    }

    public void uploadFile(MouseEvent mouseEvent) {
        try {
            String selectedFile = localListView.getSelectionModel().getSelectedItem();
            network.write(new FileMessage(Path.of(dir).resolve(selectedFile)));
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
            System.out.println("Received file: " + selectedFile);
        } catch (IOException e) {
            System.out.println("File was not accepted");
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
            e.printStackTrace();
        }
    }

    public void checkServerDirectory(MouseEvent mouseEvent) {
        try {
            String selectedFile = serverListView.getSelectionModel().getSelectedItem();
            network.write(new PathInRequest(selectedFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

