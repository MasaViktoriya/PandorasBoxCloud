package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;
@Data
public class RegistrationRequest implements BoxCommand {
    private String newLogin;
    private String newPassword;

    public RegistrationRequest(String newLogin, String newPassword){
        this.newLogin = newLogin;
        this.newPassword = newPassword;
    }
}
