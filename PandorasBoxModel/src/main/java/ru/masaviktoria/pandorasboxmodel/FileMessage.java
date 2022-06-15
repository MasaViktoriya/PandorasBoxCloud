package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class FileMessage implements BoxMessage {
    private final long fileSize;
    private final byte[] data;
    private final String fileName;
    private FileType fileType;

    public enum FileType {
        FILE("F"), DIRECTORY("D");
        private String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public FileMessage(Path path) throws IOException {
        data = Files.readAllBytes(path);
        fileName = path.getFileName().toString();
        if (Files.isDirectory(path)) {
            this.fileType = FileType.DIRECTORY;
            this.fileSize = -1;
        } else {
            this.fileType = FileType.FILE;
            this.fileSize = Files.size(path);
        }
    }
}
