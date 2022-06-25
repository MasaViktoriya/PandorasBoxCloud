package ru.masaviktoria.pandorasboxapplication;

import java.time.format.DateTimeFormatter;

public class ClientConstants {
    public static final int PORT = 8139;
    public static final String LOCALROOTDIRECTORY = System.getProperty("user.home");
    public static final String AUTHORIZE = "authorize";
    public static final String REGISTER = "register";
    public static final String LOCAL = "local";
    public static final String SERVER = "server";
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

}
