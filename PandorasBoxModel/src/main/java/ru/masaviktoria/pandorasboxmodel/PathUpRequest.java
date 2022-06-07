package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

import java.io.IOException;
import java.util.List;


@Data
public class PathUpRequest implements BoxMessage {

    private List<String> files;
    private final String selectedFileOrDirectory;

    public PathUpRequest(String selectedFileOrDirectory) throws IOException {
        this.selectedFileOrDirectory = selectedFileOrDirectory;
    }
}

