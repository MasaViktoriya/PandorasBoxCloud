package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

//todo: класс частично дублирует функционал FileContainer, только здесь нет байтов. избавиться от дублирования
@Data
public class FileListMappingInfo implements Serializable {

    public enum FileType {
        FILE(""), DIRECTORY("->");
        private String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private String fileName;
    private FileType fileType;
    private long fileSize;
    private LocalDateTime lastModified;

    public FileListMappingInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                this.fileType = FileType.DIRECTORY;
                this.fileSize = -1;
            } else {
                this.fileType = FileType.FILE;
                this.fileSize = Files.size(path);
            }
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info");
        }
    }
}
