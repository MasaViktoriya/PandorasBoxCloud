package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

import java.io.IOException;
import java.util.List;


@Data
public class PathInRequest implements BoxMessage {

    private List<String> files;
    private final String selectedDirectory;

    public PathInRequest(String selectedDirectory) throws IOException {
        this.selectedDirectory = selectedDirectory;
    }
}
