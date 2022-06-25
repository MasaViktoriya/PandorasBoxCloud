package ru.masaviktoria.pandorasboxapplication;

import ru.masaviktoria.pandorasboxmodel.*;

import java.io.IOException;
import java.net.SocketException;

public class NetworkService {

    private Network network;

    public NetworkService(){
        networkConnection();
       // startServerCommunication();
    }


    private void networkConnection() {
        try {
            network = new Network(ClientConstants.PORT);
        } catch (IOException e) {
            System.out.println("Connection failed");
            Alerts.connectionAlert();
            e.printStackTrace();
        }
    }

/*    private void startServerCommunication() {
        Thread readServerCommandsThread = new Thread(() -> readCommandsFromServer());
        readServerCommandsThread.setDaemon(true);
        readServerCommandsThread.start();
    }*/

    protected void readCommandsFromServer(CallBackInterface callBack) {
        try {
            while (true) {
                BoxCommand boxCommand = network.read();
                if (boxCommand instanceof AuthOK authOK) {
                    callBack.handleCallbacks(authOK);
                } else if (boxCommand instanceof AuthFailed) {
                    callBack.handleCallbacks(new AuthFailed());
                } else if (boxCommand instanceof RegistrationFailed) {
                    callBack.handleCallbacks(new RegistrationFailed());
                } else if (boxCommand instanceof FileList fileList) {
                    callBack.handleCallbacks(fileList);
                } else if (boxCommand instanceof LogoutOK) {
                    callBack.handleCallbacks(new LogoutOK());
                } else if (boxCommand instanceof FileContainer fileContainer) {
                    callBack.handleCallbacks(fileContainer);
                } else if (boxCommand instanceof NewDirectoryFailed) {
                    callBack.handleCallbacks( new NewDirectoryFailed());
                } else if (boxCommand instanceof RenameFailed) {
                    callBack.handleCallbacks(new RenameFailed());
                } else if (boxCommand instanceof DeleteFailed) {
                    callBack.handleCallbacks(new DeleteFailed());
                }
            }
        } catch (SocketException s) {
            System.out.println("Server was disconnected");
            Alerts.connectionAlert();
        } catch (Exception e) {
            System.out.println("Reading error");
            e.printStackTrace();
        }
    }

    protected void sendAuthRequest(String login, String password) {
        try {
            network.write(new AuthRequest(login, password));
        } catch (IOException e) {
            System.out.println("Authentication request failed");
            e.printStackTrace();
        }
    }

    protected void sendRegistrationRequest(String login, String password) {
        try {
            network.write(new RegistrationRequest(login, password));
        } catch (IOException e) {
            System.out.println("Registration request failed");
            e.printStackTrace();
        }
    }


    protected void sendLogoutRequest() {
        try {
            network.write(new LogoutRequest());
        } catch (IOException e) {
            System.out.println("Logout request failed");
            e.printStackTrace();
        }
    }

    protected void sendFileContainer(FileContainer fileContainer) {
        try {
            network.write(fileContainer);
            System.out.println("Upload successful");
        } catch (IOException e) {
            System.out.println("Upload unsuccessful");
            e.printStackTrace();
        }
    }

    protected void sendFileRequest(String selectedFile) {
        try {
            System.out.println("Download started: " + selectedFile);
            network.write(new FileRequest(selectedFile));
        } catch (IOException e) {
            System.out.println("File was not accepted");
            e.printStackTrace();
        }
    }

    protected void sendPathUpRequest() {
        try {
            network.write(new PathUpRequest());
        } catch (IOException e) {
            System.out.println("Server navigation error");
            e.printStackTrace();
        }
    }

    protected void sendPathInRequest(String selectedFile) {
        try {
            network.write(new PathInRequest(selectedFile));
        } catch (IOException e) {
            System.out.println("Server navigation error");
            e.printStackTrace();
        }
    }

    protected void sendNewDirectoryRequest(String newDirectoryName) {
        try {
            network.write(new NewDirectoryRequest(newDirectoryName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void sendRenameRequest(String oldName, String newName) {
        try {
            network.write(new RenameRequest(oldName, newName));
        } catch (IOException e) {
            System.out.println("Rename failed");
            e.printStackTrace();
        }
    }

    protected void sendDeleteRequest(String itemToDelete) {
        try {
            network.write(new DeleteRequest(itemToDelete));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}