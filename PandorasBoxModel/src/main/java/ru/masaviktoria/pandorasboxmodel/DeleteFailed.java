package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class DeleteFailed implements BoxCommand{
    public boolean isDirectoryNotEmpty;

    public DeleteFailed(boolean isDirectoryNotEmpty){
        this.isDirectoryNotEmpty = isDirectoryNotEmpty;
    }
    public DeleteFailed(){

    }
}
