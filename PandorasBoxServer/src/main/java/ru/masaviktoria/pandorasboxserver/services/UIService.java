package ru.masaviktoria.pandorasboxserver.services;

import ru.masaviktoria.pandorasboxmodel.*;
import ru.masaviktoria.pandorasboxserver.HandlingResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UIService {

    public static HandlingResult pathUpRequestHandle(Path currentDir) throws IOException {
        if (!currentDir.getParent().equals(Path.of(CommandsAndConstants.SERVERROOTDIRECTORY))) {
            Path newCurrentDir = currentDir.getParent();
            return new HandlingResult(new FileList(newCurrentDir), newCurrentDir);
        } else {
            return new HandlingResult(new FileList(currentDir), currentDir);
        }
    }

    //todo: отправка информации о файле
    public static HandlingResult pathInRequestHandle(PathInRequest pathInRequest, Path currentDir) throws IOException {
        if (Files.isDirectory(currentDir.resolve(pathInRequest.getSelectedDirectory()))) {
            Path newCurrentDir = currentDir.resolve(pathInRequest.getSelectedDirectory());
            return new HandlingResult(new FileList(newCurrentDir), newCurrentDir);
        } else {
            return new HandlingResult(new FileInfomationField());
        }
    }
}
