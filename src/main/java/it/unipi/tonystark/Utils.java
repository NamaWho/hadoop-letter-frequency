package it.unipi.tonystark;

import java.text.Normalizer;

public class Utils {

    /**
     * Filter the characters from the input text
     * @param inputStr the input text
     * @param normalize a boolean to normalize the input text
     * @return the filtered text
     */
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
