package ru.masaviktoria.pandorasboxapplication;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import ru.masaviktoria.pandorasboxmodel.BoxMessage;

import java.io.IOException;
import java.net.Socket;

public class Network {

    private ObjectDecoderInputStream inputStream;
    private ObjectEncoderOutputStream outputStream;

    public Network(int port) throws IOException {
        Socket socket = new Socket("localhost", port);
        outputStream = new ObjectEncoderOutputStream(socket.getOutputStream());
        inputStream = new ObjectDecoderInputStream(socket.getInputStream());
    }

    public BoxMessage read() throws IOException, ClassNotFoundException {
        return (BoxMessage) inputStream.readObject();
    }

    public void write(BoxMessage msg) throws IOException {
        outputStream.writeObject(msg);
        outputStream.flush();
    }


}

