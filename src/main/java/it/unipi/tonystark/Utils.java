package it.unipi.tonystark;

import java.text.Normalizer;

public class Utils {

    /**
     * This method removes all the non-letter characters from a string
     * @param value the string to convert
     * @param accent if true, the method will remove also the accents
     * @return the string without non-letter characters
     */
    public static String removeNonLetters(String value, boolean accent) {
        if (accent) {
            //split each letter with an accent into two characters: the letter and the accent
            //i.e. "à" -> "a`"
            value = Normalizer.normalize(value, Normalizer.Form.NFD);
            return value.replaceAll("[^\\p{L}]", "");
        }
        else {
            return value.replaceAll("[^\\p{L}]", "");
        }
    }

}
