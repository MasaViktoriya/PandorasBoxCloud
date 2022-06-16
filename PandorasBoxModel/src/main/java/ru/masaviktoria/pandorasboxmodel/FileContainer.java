package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;

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
//todo: не хватает поля fileLastModified

// todo: возможно, enum не понадобится
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
        fileData = Files.readAllBytes(path);
        fileName = path.getFileName().toString();
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
