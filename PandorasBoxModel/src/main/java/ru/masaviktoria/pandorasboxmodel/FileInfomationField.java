package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class FileInfomationField implements BoxCommand {
    private String information;

    public FileInfomationField(){
        this.information = information;
    }
}
