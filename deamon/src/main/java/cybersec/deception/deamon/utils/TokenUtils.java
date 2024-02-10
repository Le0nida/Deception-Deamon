package cybersec.deception.deamon.utils;

import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class TokenUtils {

    public static String generateEncryptionKey(){
        // Specifica l'algoritmo di cifratura (AES in questo caso)
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Errore Generating the Key");
            return generateToken(32);
        }

        // Imposta la lunghezza della chiave (128, 192 o 256 bits)
        keyGenerator.init(256);

        // Genera la chiave segreta
        SecretKey secretKey = keyGenerator.generateKey();

        // Converte la chiave in formato Base64 per memorizzarla o trasmetterla
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        // Stampa la chiave generata
        System.out.println("Generated Key: " + encodedKey);

        return encodedKey;
    }

    public static String generateToken(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[length];
        secureRandom.nextBytes(tokenBytes);
        return bytesToHex(tokenBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


}
