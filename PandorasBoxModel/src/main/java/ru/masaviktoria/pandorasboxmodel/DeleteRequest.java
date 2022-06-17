package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class DeleteRequest implements BoxCommand{
    private String itemToDelete;

    public DeleteRequest(String itemToDelete){
        this.itemToDelete = itemToDelete;
    }
}
