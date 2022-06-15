package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class FileRequest implements BoxMessage {
    private final String name;
}
