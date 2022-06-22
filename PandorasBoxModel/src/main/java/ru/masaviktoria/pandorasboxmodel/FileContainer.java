package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.apache.commons.io.FilenameUtils;

//todo: класс частично дублирует функционал FileListMappingInfo. Отличие в том, что здесь байты файла. Избавиться от дублирования
@Data
public class FileContainer implements BoxCommand {
    private final long fileSize;
    private final byte[] fileData;
    private final String fileName;
    private FileOrDirectory fileOrDirectory;
    private final String fileType;
    private String fileServerPath;
    private String fileSharingLink;
    private String fileOwner;
    private LocalDateTime lastModified;


    public enum FileOrDirectory {
        FILE("F"), DIRECTORY("D");
        private String name;

        FileOrDirectory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public FileContainer(Path path) throws IOException {
        this.fileData = Files.readAllBytes(path);
        this.fileName = path.getFileName().toString();
        this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
        if (Files.isDirectory(path)) {
            this.fileOrDirectory = FileOrDirectory.DIRECTORY;
            this.fileSize = -1;
            this.fileType = "dir";
        } else {
            this.fileOrDirectory = FileOrDirectory.FILE;
            this.fileSize = Files.size(path);
            this.fileType = FilenameUtils.getExtension(fileName);
        }
    }
}
