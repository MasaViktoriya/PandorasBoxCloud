package ru.masaviktoria.pandorasboxapplication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    private  final int port;



    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Network (int port) throws IOException {
        this.port = port;
        Socket socket = new Socket("localhost", port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public String readServerMessage () throws IOException {
        return inputStream.readUTF();
    }

    public  void sendMessageToServer(String clientsMessage) throws IOException {
        outputStream.writeUTF(clientsMessage);
        outputStream.flush();
    }

    public DataOutputStream getOutputStream (){
        return this.outputStream;
    }
    public DataInputStream getInputStream() {
        return inputStream;
    }
}

