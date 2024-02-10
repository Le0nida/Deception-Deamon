package io.swagger.api;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ApiUtils {

    // Metodo per simulare un ritardo casuale
    public void simulateRandomDelay() {
        Random random = new Random();
        int delay;
        if (random.nextDouble() < 0.85) { // Probabilità dell'80% di generare un ritardo entro un secondo
            delay = random.nextInt(1000); // Ritardo massimo di 1 secondo
        } else if (random.nextDouble() < 0.95) {
            delay = random.nextInt(4000) + 1000; // Ritardo compreso tra 1 e 5 secondi
        } else {
            delay = random.nextInt(15000) + 8000; // Ritardo compreso tra 8 e 15 secondi
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
        return random.nextDouble() < 0.1; // Probabilità del 10% di generare un'eccezione
    }

}
