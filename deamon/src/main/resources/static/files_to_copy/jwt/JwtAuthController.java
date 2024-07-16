package io.swagger.api;

import io.swagger.configuration.jwt.JwtTokenService;
import io.swagger.log.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class JwtAuthController {

    @Value("${jwt.username}")
    private String username;

    @Value("${jwt.password}")
    private String password;

    private final JwtTokenService jwtTokenService;

    private final LogService logService;

    private final HttpServletRequest request;

    @Autowired
    public JwtAuthController(JwtTokenService jwtTokenService, LogService logService, HttpServletRequest request) {
        this.jwtTokenService = jwtTokenService;
        this.logService = logService;
        this.request = request;
    }

    @GetMapping("/token")
    public ResponseEntity<String> getToken(@RequestParam String username, @RequestParam String password) {
        logService.log(request);

        if (authenticate(username, password)) {
            String token = jwtTokenService.generateToken(username);
            return ResponseEntity.ok(token);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{ ERROR: Invalid credentials }");
        }
    }

    private boolean authenticate(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}
