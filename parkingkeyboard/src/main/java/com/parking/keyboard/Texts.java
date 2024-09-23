package com.parking.keyboard;

import java.util.regex.Pattern;


public class Texts {
    private static Pattern ENGLISH_LETTER_DIGITS = Pattern.compile("[^a-zA-Z0-9]");

    public static boolean isEnglishLetterOrDigit(String str) {
        return !ENGLISH_LETTER_DIGITS.matcher(str).find();
    }

    public static boolean isNewEnergyType(String number) {
        if (number != null && number.length() >= 2) {
            final int size = 8 - number.length();
            for (int i = 0; i < size; i++) {
                number += "0";
            }
            if (Pattern.matches("[^0-9A-Z民使][A-Z][0-9A-Z][0-9A-Z]\\w{2}[0-9A-Z][0-9A-Z]", number)) {
                return true;
            } else {
                return false;
            }
        } else if (number != null && number.length() == 1) {
            return Pattern.matches("[^0-9A-Z民使]", number);
        } else {
            return true;
        }
    }
}
