package it.unipi.tonystark;

import static it.unipi.tonystark.Utils.filterCharacters;

public class test {
    public static void main(String[] args) {
        String input = "Hello, World! 123. ò è à ì ù â ê î ô û ã õ ç @ º Дд Жж Йй Цц Чч Шш Щщ Ъъ Ыы Ьь Ээ Юю Яя";
        String value = filterCharacters(input, false);
        System.out.println(value);
    }
}
