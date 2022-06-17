package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class NewFolderRequest implements BoxCommand{

    private String newFolderName;

    public NewFolderRequest(String newFolderName){
        this.newFolderName = newFolderName;
    }
}
