package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class FileRequest implements BoxCommand {
    private final String name;
}
