package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class RenameRequest implements BoxCommand{

    private String oldName;
    private String newName;

    public RenameRequest (String oldName, String newName){
        this.newName = newName;
        this.oldName = oldName;
    }
}
