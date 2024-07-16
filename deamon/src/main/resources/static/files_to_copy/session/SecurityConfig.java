package io.swagger.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Configurazione di gestione della sessione
        http
                .sessionManagement()
                .invalidSessionUrl("/session-invalid")
                .maximumSessions(1)
                .expiredUrl("/session-expired")
                .maxSessionsPreventsLogin(true)
                .and()
                .sessionFixation().migrateSession();

        // Configurazione della scadenza della sessione
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .sessionManagement()
                .sessionFixation().none()
                .invalidSessionStrategy(new SimpleRedirectInvalidSessionStrategy("/session-invalid"))
                .maximumSessions(1)
                .expiredUrl("/session-expired")
                .maxSessionsPreventsLogin(true);

        // Configurazione di default per altre richieste
        http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll();
    }
}