package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class FileInfomationField implements BoxMessage {
    private String information;

    public FileInfomationField(String information){
        this.information = information;
    }
}
