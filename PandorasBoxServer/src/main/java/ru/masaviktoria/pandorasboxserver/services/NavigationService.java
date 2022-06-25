package ru.masaviktoria.pandorasboxserver.services;

import ru.masaviktoria.pandorasboxmodel.*;
import ru.masaviktoria.pandorasboxserver.ProcessingResult;
import ru.masaviktoria.pandorasboxserver.ServerConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NavigationService {

    public static ProcessingResult pathUpRequestHandle(Path currentDir) throws IOException {
        if (!currentDir.getParent().equals(Path.of(ServerConstants.SERVERROOTDIRECTORY))) {
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
}