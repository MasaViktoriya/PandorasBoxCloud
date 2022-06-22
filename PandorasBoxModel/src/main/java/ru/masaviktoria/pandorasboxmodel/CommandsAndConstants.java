package ru.masaviktoria.pandorasboxmodel;

import java.time.format.DateTimeFormatter;

public class CommandsAndConstants {
    public static final int PORT = 8139;
    public static final String SERVERROOTDIRECTORY = "server_files";
    public static final String LOCALROOTDIRECTORY = System.getProperty("user.home");
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String STARTLIST = "#startlist#";
    public static final String UPLOAD = "#upload#";
    public static final String DOWNLOAD = "#download#";
}
