package ru.masaviktoria.pandorasboxmodel;

import lombok.Data;
@Data
public class RegistrationRequest implements BoxMessage{
    private String newLogin;
    private String newPassword;

    public RegistrationRequest(String newLogin, String newPassword){
        this.newLogin = newLogin;
        this.newPassword = newPassword;
    }
}
