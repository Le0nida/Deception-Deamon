package cybersec.deception.deamon.utils;

import org.springframework.stereotype.Component;

@Component
public class Utils {

    // Controlla se una stringa è vuota o null
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Controlla se una stringa è composta solo da spazi bianchi
    public static boolean isWhitespace(String str) {
        return str != null && str.trim().isEmpty();
    }

    // Controlla se una stringa è un numero intero
    public static boolean isInteger(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }

        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Tronca una stringa alla lunghezza desiderata
    public static String truncate(String str, int maxLength) {
        if (isNullOrEmpty(str) || maxLength <= 0) {
            return str;
        }

        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    public static <T> boolean isNullOrEmpty(T[] array) {
        return array == null || array.length == 0;
    }
}
