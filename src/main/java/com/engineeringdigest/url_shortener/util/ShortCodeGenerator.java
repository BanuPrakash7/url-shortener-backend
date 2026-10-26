package com.engineeringdigest.url_shortener.util;

import java.security.SecureRandom;
import java.util.Random;

public class ShortCodeGenerator {

    private static final String ALPHABETS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();

    public static String generate(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABETS.charAt(random.nextInt(62)));
        }
        return sb.toString();
    }
}
