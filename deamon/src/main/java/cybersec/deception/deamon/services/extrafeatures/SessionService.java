package cybersec.deception.deamon.services.extrafeatures;

import cybersec.deception.deamon.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service

public class SessionService {

    @Value("${sessionFiles.source.path}")
    private String sessionFilesSourcePath;

    @Value("${securityconfig.destination.path}")
    private String generatedSecurityConfigPath;

    private final static String importSessionString = """
            import org.springframework.context.annotation.Configuration;
            import org.springframework.security.config.annotation.web.builders.HttpSecurity;
            import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
            import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
            import org.springframework.security.config.http.SessionCreationPolicy;
            import org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy;
            """;
    private final static String securitySessionConfig = """
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
            """;

    public void addSessionFeatures(){

        if (FileUtils.existsFile(generatedSecurityConfigPath)) {
            // Aggiungo gli import
            FileUtils.replaceStringInFile(generatedSecurityConfigPath, "import org.springframework.context.annotation.Configuration;", importSessionString);

            // Aggiungo http config
            FileUtils.replaceStringInFile(generatedSecurityConfigPath, "protected void configure(HttpSecurity http) throws Exception {", securitySessionConfig);
        }
        else {
            FileUtils.copyFile(FileUtils.buildPath(sessionFilesSourcePath,"SecurityConfig.java"), generatedSecurityConfigPath);
        }
    }
}
