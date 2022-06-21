package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class FileListMappingInfo implements Serializable, BoxCommand {

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
    private String fileSharingLink;
    private int fileOwner;
    private boolean isNotSystem = true;
    private boolean isNotHidden;

    public FileListMappingInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                this.fileType = FileType.DIRECTORY;
                this.fileSize = -1;
                File file = new File(String.valueOf(path));
                if(file.listFiles() != null){
                    this.isNotSystem = true;
                }else {
                    this.isNotSystem = false;
                }
            } else {
                this.fileType = FileType.FILE;
                this.fileSize = Files.size(path);
            }
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
            if(Files.isHidden(path)){
                this.isNotHidden = false;
            }else{
                this.isNotHidden = true;
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info");
        }
    }
}
