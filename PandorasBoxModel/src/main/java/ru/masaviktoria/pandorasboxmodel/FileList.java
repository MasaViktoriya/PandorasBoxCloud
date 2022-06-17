package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FileList implements BoxCommand {
    private List<FileListMappingInfo> files;
    private String currentDir;

    public FileList(Path path) throws IOException {
        this.files = Files.list(path).map(FileListMappingInfo::new).collect(Collectors.toList());
        this.currentDir = path.toString();
    }
}
