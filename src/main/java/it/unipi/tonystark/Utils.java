package it.unipi.tonystark;

import java.text.Normalizer;

public class Utils {

    public static String filterCharacters(String inputStr, boolean normalize) {
        if (inputStr == null || inputStr.isEmpty()) {
            return "";
        }

        if (normalize) inputStr = Normalizer.normalize(inputStr, Normalizer.Form.NFD);
        StringBuilder filteredString = new StringBuilder();

        for (char c : inputStr.toCharArray()) {
            if (c <= 0x024F && c != 0x00AA && c!= 0x00BA && Character.isLetter(c)) {
                filteredString.append(Character.toLowerCase(c));
            }
        }
        return filteredString.toString();
    }
}
