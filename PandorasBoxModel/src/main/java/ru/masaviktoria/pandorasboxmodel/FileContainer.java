package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class FileContainer implements BoxCommand {
    private final byte[] fileData;
    private final String fileName;

    public FileContainer(Path path) throws IOException {
        this.fileData = Files.readAllBytes(path);
        this.fileName = path.getFileName().toString();
    }
}
