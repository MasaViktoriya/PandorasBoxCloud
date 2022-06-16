package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;

@Data
public class AuthRequest implements BoxCommand {
    private String login;
    private String password;

    public AuthRequest(String login, String password){
        this.login = login;
        this.password = password;
    }
}
