package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class AuthOK implements BoxCommand {

    private String login;
    public AuthOK (String login){
        this.login = login;
    }
}
