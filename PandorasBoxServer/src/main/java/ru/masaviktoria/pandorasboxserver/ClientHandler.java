package ru.masaviktoria.pandorasboxserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientHandler implements Runnable {

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private final String filesDir = "server_files";

    public ClientHandler(Socket socket) throws IOException {
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Client accepted");
        sendFileListToClient();
    }

    @Override
    public void run() {
          try {
            while (true){
                String clientMessage = inputStream.readUTF();
                System.out.println("Received from client: " + clientMessage);
                if (clientMessage.equals(ServerCommandsAndConstants.UPLOAD)){
                   String uploadFileName = inputStream.readUTF();
                   acceptFile(uploadFileName);
                }
                if (clientMessage.equals(ServerCommandsAndConstants.DOWNLOAD)){
                    String downloadFileName = inputStream.readUTF();
                    sendFile(downloadFileName);
                }
            }
        } catch (Exception e){
            System.out.println("Connection is broken");
            e.printStackTrace();
        }
    }

    private void sendMessageToClient(String serverMessage){
       try {
           outputStream.writeUTF(serverMessage);
           outputStream.flush();
       }catch (IOException e){
           System.out.println("Message sending failed");
           e.printStackTrace();
       }
    }

    private void sendFileListToClient() {
        try {
            Set<String> serverFileListSet = streamFiles(filesDir, 1);
            StringBuilder serverFilesList = new StringBuilder();
            serverFilesList.append(">>");
            for (String  i: serverFileListSet) {
                serverFilesList.append(i);
                serverFilesList.append(">>");
            }
            sendMessageToClient(ServerCommandsAndConstants.STARTLIST + serverFilesList);
        } catch (IOException | NullPointerException e) {
            System.out.println("Server files list could not be obtained");
            e.printStackTrace();
        }
    }

    private Set<String> streamFiles(String dir, int depth) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(dir), depth)) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }

    private void acceptFile (String fileName) throws InterruptedException {

        try{
            long fileSize = inputStream.readLong();
            File uploadedFile = Path.of(filesDir).resolve(fileName).toFile();
            try (FileOutputStream os = new FileOutputStream(uploadedFile)) {
                byte[] buffer = new byte[1024];
                for (int i = 0; i < (fileSize+1023)/1024; i++) {
                    int read = inputStream.read(buffer);
                    os.write(buffer, 0, read);
                }
            }catch (IOException e) {
                System.out.println("Reading or writing error occurred");
                e.printStackTrace();
            }
            System.out.println("Received file: " + fileName);
            System.out.println("Length: " + fileSize);
            } catch (IOException e) {
                System.out.println("File was not accepted by server");
                e.printStackTrace();
            }
        sendFileListToClient();
    }

    private void sendFile(String fileName) {
        try {
             File fileForSending = Path.of(filesDir).resolve(fileName).toFile();
             outputStream.writeLong(fileForSending.length());
             System.out.println("Start sending file " + fileName);
             byte[] buf = new byte[1024];
             try(FileInputStream fis = new FileInputStream(fileForSending)){
                    while(fis.available() >0){
                    int read = fis.read(buf);
                    outputStream.write(buf, 0, read);
                    }
             }
        outputStream.flush();
        System.out.println("Sending successful");
        } catch (IOException e) {
            System.out.println("Sending unsuccessful");
            e.printStackTrace();
        }
    }
}

