package ru.masaviktoria.pandorasboxserver.services;

import ru.masaviktoria.pandorasboxmodel.*;
import ru.masaviktoria.pandorasboxserver.ProcessingResult;

import java.io.IOException;
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

    public static ProcessingResult pathInRequestHandle(PathInRequest pathInRequest, Path currentDir) throws IOException {
        if (Files.isDirectory(currentDir.resolve(pathInRequest.getSelectedDirectory()))) {
            Path newCurrentDir = currentDir.resolve(pathInRequest.getSelectedDirectory());
            return new ProcessingResult(new FileList(newCurrentDir), newCurrentDir);
        } else {
            return new ProcessingResult(new FileListMappingInfo(currentDir.resolve(pathInRequest.getSelectedDirectory())));
        }
    }

    public static ProcessingResult newFolderHandle(NewFolderRequest newFolderRequest, Path currentDir) throws IOException {
        if(!Files.exists(currentDir.resolve(newFolderRequest.getNewFolderName()))) {
            Files.createDirectory(currentDir.resolve(newFolderRequest.getNewFolderName()));
            System.out.println("User created a new server folder: " + newFolderRequest.getNewFolderName());
            return new ProcessingResult(new FileList(currentDir));
        }else{
            return new ProcessingResult(new NewFolderFailed());
        }
    }
}