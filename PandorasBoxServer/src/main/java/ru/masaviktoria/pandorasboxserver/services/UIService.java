package ru.masaviktoria.pandorasboxserver.services;

import ru.masaviktoria.pandorasboxmodel.*;
import ru.masaviktoria.pandorasboxserver.ProcessingResult;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UIService {

    public static ProcessingResult pathUpRequestHandle(Path currentDir) throws IOException {
        if (!currentDir.getParent().equals(Path.of(CommandsAndConstants.SERVERROOTDIRECTORY))) {
            Path newCurrentDir = currentDir.getParent();
            return new ProcessingResult(new FileList(newCurrentDir), newCurrentDir);
        } else {
            return new ProcessingResult(new FileList(currentDir), currentDir);
        }
    }

    //todo: отправка информации о файле
    public static ProcessingResult pathInRequestHandle(PathInRequest pathInRequest, Path currentDir) throws IOException {
        if (Files.isDirectory(currentDir.resolve(pathInRequest.getSelectedDirectory()))) {
            Path newCurrentDir = currentDir.resolve(pathInRequest.getSelectedDirectory());
            return new ProcessingResult(new FileList(newCurrentDir), newCurrentDir);
        } else {
            return new ProcessingResult(new FileInfomationField());
        }
    }

    public static ProcessingResult newFolderHandle(NewFolderRequest newFolderRequest, Path currentDir) throws IOException {
        if(!Files.exists(currentDir.resolve(newFolderRequest.getNewFolderName()))) {
            Files.createDirectory(currentDir.resolve(newFolderRequest.getNewFolderName()));
            System.out.println("User created a new server folder: " + newFolderRequest.getNewFolderName());
            return new ProcessingResult(new FileList(currentDir));
        }else{
            return new ProcessingResult(new NewFolderFailed());        }

    }

    public static ProcessingResult renameFileOrDirectory(RenameRequest renameRequest, Path currentDir) throws IOException{
        String newName = renameRequest.getNewName();
        Path source = currentDir.resolve(renameRequest.getOldName());
        Files.move(source, source.resolveSibling(newName));
        return new ProcessingResult(new FileList(currentDir));
    }

    //todo ошибки с access denied
    public  static ProcessingResult deleteFileOrDirectory(DeleteRequest deleteRequest, Path currentDir) throws IOException {
        try {
            Files.deleteIfExists(currentDir.resolve(deleteRequest.getItemToDelete()));
            return new ProcessingResult(new FileList(currentDir));
        } catch (DirectoryNotEmptyException d) {
            System.out.println("Folder is not empty");
            return new ProcessingResult(new DeleteFailed(true));
        } catch (FileNotFoundException f){
            System.out.println("File not found");
            return new ProcessingResult(new DeleteFailed(false));
        }
    }
}