package io.swagger.log;

import io.swagger.log.logmodel.LogRequest;
import io.swagger.log.logmodel.LogResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@RestController
public class LogController {

    @Autowired
    private LogService logService;

    @Value("${crypto.key}")
    private String encryptionKey;

    @Value("${token.decrypted.value}")
    private String decryptedKey;

    @PostMapping("/logs")
    public ResponseEntity<LogResponse> getLogs(@RequestBody LogRequest logRequest) {
        LogResponse response = new LogResponse();
        if (logRequest != null && logRequest.getToken() != null) {
            String decryptedToken = decryptToken(logRequest.getToken());
            if (decryptedToken != null) {
                if (decryptedToken.equals(decryptedKey)){
                    response.setLogs(logService.findLogs(logRequest.getFilter()));
                    response.setMessage("OK");
                }
                else {
                    response.setMessage("Decryption problem");
                }
            }
            else {
                response.setMessage("Invalid token");
            }
        }
        else {
            response.setMessage("Token null");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String decryptToken(String encryptedToken) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedToken);

            byte[] keyBytes = encryptionKey.getBytes();

            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
