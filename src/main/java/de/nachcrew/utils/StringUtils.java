package de.nachcrew.utils;

import java.security.SecureRandom;

public class StringUtils {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBER = "123456789";
    private static SecureRandom random = new SecureRandom();
    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + NUMBER + CHAR_LOWER;

    public static String generateRandomString(int length) {
        if (length < 1) throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            sb.append(rndChar);
        }
        return sb.toString();

    }
}