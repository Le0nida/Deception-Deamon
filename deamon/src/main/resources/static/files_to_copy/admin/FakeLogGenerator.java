package io.swagger.log;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
@RestController
public class FakeLogGenerator {
    private static final List<String> USERS = Arrays.asList(
            "michael_davis", "sarah_miller", "david_wilson", "emily_moore", "james_taylor",
            "olivia_anderson", "daniel_thomas", "sophia_jackson", "matthew_white", "isabella_harris",
            "joseph_martin", "mia_thompson", "noah_garcia", "ava_martinez", "benjamin_robinson",
            "amelia_lee", "ethan_walker", "harper_hall", "lucas_allen", "ella_young",
            "henry_king", "abigail_scott", "samuel_green", "lily_adams", "jacob_baker",
            "charlotte_wright", "jackson_turner", "chloe_parker", "logan_campbell", "scarlett_evans",
            "oliver_morgan", "amelia_sanders", "liam_reed", "sophia_morris", "william_bell",
            "emma_murphy", "james_brooks", "ava_bennett", "elijah_richardson", "isabella_cox",
            "lucas_washington", "mia_cole", "mason_fisher", "ella_simmons", "noah_hayes",
            "amelia_russell", "jackson_perry", "sofia_wood", "henry_patterson", "grace_hughes"
    );
    private static final List<String> LEVELS = Arrays.asList("INFO", "INFO", "INFO", "INFO", "INFO", "WARN", "WARN", "ERROR");
    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<String> INFO_MESSAGES = Arrays.asList(
            "User %s logged in",
            "User %s logged out",
            "User %s updated profile",
            "User %s accessed resource %s",
            "User %s downloaded file %s",
            "User %s viewed dashboard",
            "User %s changed settings",
            "User %s sent a message",
            "User %s uploaded file %s",
            "User %s updated status",
            "User %s reset password",
            "User %s deleted file %s",
            "User %s created a new account",
            "User %s updated their email address",
            "User %s requested account deletion",
            "User %s viewed notifications",
            "User %s updated their preferences",
            "User %s disabled their account",
            "User %s enabled two-factor authentication",
            "User %s disabled two-factor authentication",
            "User %s accessed the help section",
            "User %s logged in from IP address %s",
            "User %s logged out from IP address %s",
            "User %s attempted an unauthorized action"
    );

    private static final List<String> WARN_MESSAGES = Arrays.asList(
            "User %s attempted unauthorized access",
            "High memory usage detected",
            "Potential security threat detected from IP %s",
            "Failed attempt to access restricted resource %s",
            "Configuration file %s missing",
            "Service %s took too long to respond",
            "User %s exceeded quota limit",
            "Unusual login time for user %s",
            "Deprecated API call by user %s",
            "User %s made multiple failed login attempts",
            "Low disk space on server %s",
            "Resource %s is reaching capacity"
    );

    private static final List<String> ERROR_MESSAGES = Arrays.asList(
            "Failed login attempt for user %s from IP %s",
            "Database connection failed",
            "Error processing request from IP %s",
            "System crash detected",
            "Critical error in service %s",
            "Unhandled exception in thread %s",
            "Failed to load module %s",
            "User %s encountered fatal error on page %s",
            "Server %s is not responding",
            "Service %s failed to start"
    );

    private static final List<String> RESOURCES = Arrays.asList(
            "/api/users", "/api/users/{userId}", "/api/users/{userId}/profile", "/api/users/{userId}/posts",
            "/api/products", "/api/products/{productId}", "/api/products/{productId}/reviews",
            "/api/orders", "/api/orders/{orderId}", "/api/orders/{orderId}/status",
            "/api/payments", "/api/payments/{paymentId}", "/api/payments/{paymentId}/receipt",
            "/api/reports", "/api/reports/{reportId}",
            "/api/settings", "/api/settings/{settingId}",
            "/api/notifications", "/api/notifications/{notificationId}",
            "/api/messages", "/api/messages/{messageId}",
            "/api/files", "/api/files/{fileId}", "/api/files/{fileId}/download",
            "/api/posts", "/api/posts/{postId}", "/api/posts/{postId}/comments"
    );

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void generateLog() {
        int randomDelaySeconds = generateRandomDelaySeconds();
        try {
            Thread.sleep(randomDelaySeconds * 1000L); // Convert seconds to milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted during random wait.");
        }

        String level = getRandomElement(LEVELS);
        String user = getRandomElement(USERS);
        String ip = "192.168.1." + RANDOM.nextInt(256);
        String resource = getRandomElement(RESOURCES).replace("{userId}", String.valueOf(RANDOM.nextInt(1000)))
                .replace("{productId}", String.valueOf(RANDOM.nextInt(1000)))
                .replace("{orderId}", String.valueOf(RANDOM.nextInt(1000)))
                .replace("{paymentId}", String.valueOf(RANDOM.nextInt(1000)))
                .replace("{reportId}", String.valueOf(RANDOM.nextInt(1000)))
                .replace("{settingId}", String.valueOf(RANDOM.nextInt(1000)))
                .replace("{notificationId}", String.valueOf(RANDOM.nextInt(1000)))
                .replace("{messageId}", String.valueOf(RANDOM.nextInt(1000)))
                .replace("{fileId}", String.valueOf(RANDOM.nextInt(1000)))
                .replace("{postId}", String.valueOf(RANDOM.nextInt(1000)));
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(FORMATTER);

        String message;
        switch (level) {
            case "WARN":
                message = String.format(getRandomElement(WARN_MESSAGES), user, resource);
                break;
            case "ERROR":
                message = String.format(getRandomElement(ERROR_MESSAGES), user, ip);
                break;
            default:
                message = String.format(getRandomElement(INFO_MESSAGES), user, resource);
                break;
        }

        String logEntry = String.format("%s , %s , %s , %s", formattedDate, level, message, ip);
        logToFile(logEntry);
    }

    private int generateRandomDelaySeconds() {
        return ThreadLocalRandom.current().nextInt(3, 51);
    }

    private void logToFile(String logEntry) {
        try {
            Path logFile = Paths.get("logs/application.log");
            if (!Files.exists(logFile)) {
                Files.createDirectories(logFile.getParent());
                Files.createFile(logFile);
            }
            Files.write(logFile, (logEntry + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }

    @GetMapping("/api/fakeLogs")
    public List<String> getLogs() {
        try {
            // Leggi i log dal file
            return Files.lines(Paths.get("logs/application.log"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList(); // Gestione dell'errore
        }
    }
}