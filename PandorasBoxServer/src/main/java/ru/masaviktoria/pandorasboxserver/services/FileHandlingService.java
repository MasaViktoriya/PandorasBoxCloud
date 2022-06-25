package ru.masaviktoria.pandorasboxserver.services;

import ru.masaviktoria.pandorasboxmodel.*;
import ru.masaviktoria.pandorasboxserver.ProcessingResult;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FileHandlingService {

    public static ProcessingResult fileRequestHandle(FileRequest fileRequest, Path currentDir) throws IOException {
        System.out.println("File " + fileRequest.getName() + " sent to user");
        return new ProcessingResult(new FileContainer(currentDir.resolve(fileRequest.getName())));
    }

    //todo: UUID для файлов, его запись в базу
    //todo: sharing links
    public static ProcessingResult fileContainerHandle(FileContainer fileContainer, Path currentDir, String user) throws IOException {
        Path path = currentDir.resolve(fileContainer.getFileName());
        Files.write(path, fileContainer.getFileData());
        FileListMappingInfo fileListMappingInfo = new FileListMappingInfo(path);
        try {
            ResultSet fileOwnerID = SQLService.getID(user);
            while (fileOwnerID.next()){
                int fileOwner = fileOwnerID.getInt("id");
                fileListMappingInfo.setFileOwner(fileOwner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SQLService.insertToFileInformation(fileListMappingInfo, fileContainer);
        System.out.println("File " + fileContainer.getFileName() + " saved on server by user " + user);
        return new ProcessingResult(new FileList(currentDir));
    }

//todo:  переименование на сервере через UUID
    public static ProcessingResult renameFileOrDirectory(RenameRequest renameRequest, Path currentDir) throws IOException{
        String newName = renameRequest.getNewName();
        if(!Files.exists(currentDir.resolve(newName))) {
            Path source = currentDir.resolve(renameRequest.getOldName());
            Files.move(source, source.resolveSibling(newName));
            System.out.println("File "+ source + " was renamed to " + newName);
            //  SQLService.renameFile(newName, uid);
            return new ProcessingResult(new FileList(currentDir));
        }else{
            return new ProcessingResult(new RenameFailed());
        }
    }

    //todo: удаление на сервере через UUID
    public  static ProcessingResult deleteFileOrDirectory(DeleteRequest deleteRequest, Path currentDir) {
        try {
            File fileToDelete = new File(String.valueOf(currentDir.resolve(deleteRequest.getItemToDelete())));
            Desktop.getDesktop().moveToTrash(fileToDelete);
            System.out.println("File "+ fileToDelete + " was deleted");
            return new ProcessingResult(new FileList(currentDir));
        }  catch (Exception e){
            System.out.println("Deletion error");
            return new ProcessingResult(new DeleteFailed());
        }
    }

    public static ProcessingResult newDirectoryHandle(NewDirectoryRequest newDirectoryRequest, Path currentDir) throws IOException {
        if(!Files.exists(currentDir.resolve(newDirectoryRequest.getNewFolderName()))) {
            Files.createDirectory(currentDir.resolve(newDirectoryRequest.getNewFolderName()));
            System.out.println("User created a new server folder: " + newDirectoryRequest.getNewFolderName());
            return new ProcessingResult(new FileList(currentDir));
        }else{
            return new ProcessingResult(new NewDirectoryFailed());
        }
    }
}
