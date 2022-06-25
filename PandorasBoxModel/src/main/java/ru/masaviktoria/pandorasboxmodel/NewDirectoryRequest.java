package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class NewDirectoryRequest implements BoxCommand{

    private String newFolderName;

    public NewDirectoryRequest(String newFolderName){
        this.newFolderName = newFolderName;
    }
}
