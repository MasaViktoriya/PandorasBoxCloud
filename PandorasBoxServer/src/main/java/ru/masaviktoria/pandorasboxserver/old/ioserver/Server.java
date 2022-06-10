package ru.masaviktoria.pandorasboxserver.old.ioserver;

import ru.masaviktoria.pandorasboxmodel.CommandsAndConstants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(CommandsAndConstants.PORT)){
            System.out.println("Server started");
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e){
            System.out.println("Server connection is corrupted");
            e.printStackTrace();
        }
    }
}

