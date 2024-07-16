package cybersec.deception.deamon.services.extrafeatures;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.TokenUtils;
import cybersec.deception.deamon.utils.servermanipulation.ApplPropUtils;
import cybersec.deception.deamon.utils.servermanipulation.PomMavenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JWTAuthenticationService {

    private static final int SECRET_LENGTH = 32;

    @Value("${folder.api}")
    private String generatedApiPath;

    @Value("${folder.config}")
    private String generatedConfigPath;

    @Value("${jwtFiles.source.path}")
    private String jwtFilesSourcePath;

    @Value("${securityconfig.destination.path}")
    private String generatedSecurityConfigPath;

    private final static String importJWTString = """
            import io.swagger.configuration.jwt.JwtAuthenticationFilter;
            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.beans.factory.annotation.Value;
            import org.springframework.context.annotation.Configuration;
            import org.springframework.security.config.annotation.web.builders.HttpSecurity;
            import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
            import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
            import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
            """;

    private final static String injectedJWTValues = """
            public class SecurityConfig extends WebSecurityConfigurerAdapter {
                        
                @Value("${jwt.patterns}")
                private String jwtPatterns;
                        
                private final JwtAuthenticationFilter jwtAuthenticationFilter;
                        
                @Autowired
                public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                }
            """;
    private final static String securityJWTConfig = """
            protected void configure(HttpSecurity http) throws Exception {
                        
                    // Configurazione per gli endpoint che richiedono JWT
                    http
                            .csrf().disable()
                            .authorizeRequests()
                            .antMatchers(jwtPatterns).authenticated()
                            .and()
                            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                            .exceptionHandling()
                            .authenticationEntryPoint((request, response, authException) -> {
                                response.setContentType("application/json");
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getOutputStream().println("{ \\"error\\": \\"Unauthorized - A valid JWT token is required\\" }");
                            });                                .accessDeniedPage("/admin/login?error");
            """;

    public void addJWTAuthenticationFeatures(String user, String pass, String paths){

        // Copio i file statici
        copyStaticFiles();

        // Aggiungo le configurazioni di sicurezza
        addSecurityConfig();

        // Aggiungo le configurazioni JWT in application.properties
        String secret = TokenUtils.generateToken(SECRET_LENGTH);
        ApplPropUtils.addApplicationPropertiesJWTConfig(user, pass, paths, secret);

        // Aggiungo le dipendenze di jsonwebtoken
        PomMavenUtils.configureJWTPom();
    }

    private void addSecurityConfig() {
        if (FileUtils.existsFile(generatedSecurityConfigPath)) {
            // Aggiungo gli import
            FileUtils.replaceStringInFile(generatedSecurityConfigPath, "import org.springframework.context.annotation.Configuration;", importJWTString);

            // Aggiungo i @Value user e pass
            FileUtils.replaceStringInFile(generatedSecurityConfigPath, "public class SecurityConfig extends WebSecurityConfigurerAdapter {", injectedJWTValues);

            // Aggiungo http config
            FileUtils.replaceStringInFile(generatedSecurityConfigPath, "protected void configure(HttpSecurity http) throws Exception {", securityJWTConfig);
        }
        else {
            FileUtils.copyFile(FileUtils.buildPath(jwtFilesSourcePath,"SecurityConfig.java"), generatedSecurityConfigPath);
        }
    }

    private void copyStaticFiles() {
        // Copio i file di configurazione
        FileUtils.copyDirectory(FileUtils.buildPath(jwtFilesSourcePath, "config"), FileUtils.buildPath(generatedConfigPath, "jwt"));

        // Copio il controller per gestire l'endpoint di ottenimento del token
        FileUtils.copyFile(FileUtils.buildPath(jwtFilesSourcePath, "JwtAuthController.java"), FileUtils.buildPath(generatedApiPath, "JwtAuthController.java"));
    }

}
