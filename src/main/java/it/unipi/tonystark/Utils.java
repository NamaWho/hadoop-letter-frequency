package it.unipi.tonystark;

import java.text.Normalizer;

public class Utils {

    public static String filterCharacters(String inputStr, boolean normalize) {
        // Remove characters outside the range 0x0000 to 0x024F (Unicode Basic Latin and Latin-1 Supplement)
        StringBuilder filteredString = new StringBuilder();

        for(Character c: inputStr.toCharArray()) {
            if (c <= 0x024F) {
                filteredString.append(c);
            }
        }

        // Apply regex to remove unwanted characters and convert to lowercase
        String value = filteredString.toString().replaceAll("[^\\p{L}]", "").toLowerCase();

        // Normalize the string if requested
        if (normalize) {
            // Normalize to NFD form and remove combining diacritical marks
            value = Normalizer.normalize(value, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
        }

        return value;
    }

}
