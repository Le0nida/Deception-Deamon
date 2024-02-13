package io.swagger.api;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Date;
import java.util.Random;

@Service
public class ApiUtils {

    // Metodo per simulare un ritardo casuale
    public void simulateRandomDelay() {
        Random random = new Random();
        int delay;
        if (random.nextDouble() < 0.85) { // Probabilità dell'85% di generare un ritardo entro un secondo
            delay = random.nextInt(1000);
        } else if (random.nextDouble() < 0.95) {
            delay = random.nextInt(4000) + 1000; // Ritardo compreso tra 1 e 5 secondi
        } else {
            delay = random.nextInt(9000) + 5000; // Ritardo compreso tra 5 e 10 secondi
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Metodo per simulare un'eccezione casuale
    public boolean shouldThrowException() {
        Random random = new Random();
        return random.nextDouble() < 0.05; // Probabilità del 5% di generare INTERNAL_SERVER_ERROR
    }

    // Metodo per generare un'eccezione casuale
    public HttpStatus getExceptionToThrow(HttpServletRequest request) {
        Date d = new Date(request.getSession().getCreationTime());
        String minutes = ""+d.getMinutes();
        if (minutes.contains(""+7)) {
            return HttpStatus.METHOD_NOT_ALLOWED;
        }
        else if (minutes.contains(""+8)) {
            return HttpStatus.NOT_ACCEPTABLE;
        }
        return null;
    }

}
