package ru.masaviktoria.pandorasboxapplication;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.URL;
import java.nio.file.AccessDeniedException;
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
    public Button localOpenButton;
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
    public Button serverOpenButton;
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

            network = new Network(ClientCommandsAndConstants.PORT);
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
            try {
                StringBuilder builder = new StringBuilder(System.getProperty("user.home") + "\\Pictures");
                this.dir = builder.toString();
                localListView.getItems().clear();
                localListView.getItems().addAll(streamFiles(dir, 1));
            } catch (AccessDeniedException a) {
                System.out.println("Access to some folders is denied");
            } catch (IOException | NullPointerException e) {
                System.out.println("Local files list could not be printed");
                e.printStackTrace();
            }
        });
    }

    private void showServerFiles(String fileListFromServer) {
        Platform.runLater(() -> {
            serverListView.getItems().clear();
            try {
                String[] fileListArr = fileListFromServer.split(">>");
                for (int i = 1; i < fileListArr.length; i++) {
                    serverListView.getItems().add(fileListArr[i]);
                }
            } catch (Exception e) {
                System.out.println("Server files list could not be printed");
                e.printStackTrace();
            }
        });
    }

    private Set<String> streamFiles(String directory, int depth) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(directory), depth)) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }

    private void readMessagesFromServer() {
        try {
            while (true) {
                String serverMessage = network.readServerMessage();
                System.out.println("Server: " + serverMessage);
                if (serverMessage.startsWith(ClientCommandsAndConstants.STARTLIST)) {
                    showServerFiles(serverMessage);
                }
            }
        } catch (IOException e) {
            System.out.println("Reading of server messages was interrupted");
            e.printStackTrace();
        }
    }

    public void uploadFile(MouseEvent mouseEvent) {
        try {
            String selectedFile = localListView.getSelectionModel().getSelectedItem();
            network.sendMessageToServer(ClientCommandsAndConstants.UPLOAD);
            network.sendMessageToServer(selectedFile);
            System.out.println("Upload started: " + selectedFile);
            File fileForSending = Path.of(dir).resolve(selectedFile).toFile();
            network.getOutputStream().writeLong(fileForSending.length());
            byte[] buffer = new byte[1024];
            try (FileInputStream fis = new FileInputStream(fileForSending)) {
                while (fis.available() > 0) {
                    int read = fis.read(buffer);
                    network.getOutputStream().write(buffer, 0, read);
                }
            }
            network.getOutputStream().flush();
            System.out.println("Upload successful");
        } catch (IOException e) {
            System.out.println("Upload unsuccessful");
            e.printStackTrace();
        }
    }

    public void downloadFile(MouseEvent mouseEvent) {
        try {
            String selectedFile = serverListView.getSelectionModel().getSelectedItem();
            network.sendMessageToServer(ClientCommandsAndConstants.DOWNLOAD);
            network.sendMessageToServer(selectedFile);
            System.out.println("Download started: " + selectedFile);
            File fileForDownloading = Path.of(dir).resolve(selectedFile).toFile();
            long fileSize = network.getInputStream().readLong();
            System.out.println("Length: " + fileSize);
            try (FileOutputStream os = new FileOutputStream(fileForDownloading)) {
                byte[] buf = new byte[1024];
                for (int i = 0; i < (fileSize + 1023) / 1024; i++) {
                    int read = network.getInputStream().read(buf);
                    os.write(buf, 0, read);
                }
                System.out.println("File accepted");
            } catch (IOException e) {
                System.out.println("Reading or writing error occurred");
                e.printStackTrace();
            }
            System.out.println("Received file: " + selectedFile);
        } catch (IOException e) {
            System.out.println("File was not accepted");
            e.printStackTrace();
        }
        showLocalFiles();
    }
}

