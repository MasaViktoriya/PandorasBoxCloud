package ru.masaviktoria.pandorasboxapplication;

import java.util.regex.Pattern;

public class RegExMatchers {

    protected static boolean credentialsMatcher(String credential) {
        return Pattern.matches("^[\\w\\.\\-]{1,20}$", credential);
    }

    protected static boolean nameMatcher(String name) {
        return Pattern.matches("^[\\p{L}\\p{N}\\s_.-]{1,255}$", name);
    }
}
