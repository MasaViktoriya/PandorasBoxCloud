package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

import java.io.IOException;

@Data
public class PathInRequest implements BoxCommand {

    private final String selectedDirectory;

    public PathInRequest(String selectedDirectory) throws IOException {
        this.selectedDirectory = selectedDirectory;
    }
}
