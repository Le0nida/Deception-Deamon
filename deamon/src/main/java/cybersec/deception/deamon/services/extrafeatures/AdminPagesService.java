package cybersec.deception.deamon.services.extrafeatures;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.servermanipulation.ApplPropUtils;
import cybersec.deception.deamon.utils.servermanipulation.PomMavenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class AdminPagesService {

    @Value("${folder.api}")
    private String generatedApiPath;

    @Value("${folder.static}")
    private String generatedStaticPath;

    @Value("${log.destination.folder}")
    private String generatedLogPath;

    @Value("${adminFiles.source.path}")
    private String adminFilesSourcePath;

    @Value("${securityconfig.destination.path}")
    private String generatedSecurityConfigPath;

    @Value("${swagger2springboot.starter.path}")
    private String swagger2springboot;

    private final static String importAdminString = """
                    import org.springframework.beans.factory.annotation.Value;
                    import org.springframework.context.annotation.Bean;
                    import org.springframework.context.annotation.Configuration;
                    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
                    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
                    import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
                    import org.springframework.security.core.userdetails.User;
                    import org.springframework.security.core.userdetails.UserDetailsService;
                    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
                    import org.springframework.security.crypto.password.PasswordEncoder;
                    import org.springframework.security.provisioning.InMemoryUserDetailsManager;
                    import org.springframework.security.web.session.HttpSessionEventPublisher;
                    """;
    private final static String injectedAdminValues = """
            public class SecurityConfig extends WebSecurityConfigurerAdapter {
                        
                @Value("${admin.username}")
                private String username;
                        
                @Value("${admin.password}")
                private String password;
                        

            """;
    private final static String securityAdminConfig = """
                protected void configure(HttpSecurity http) throws Exception {
                            
                                    http
                                            .csrf().disable()
                                            .authorizeRequests()
                                            .antMatchers("/admin/login", "/css/**").permitAll() // Permetti l'accesso alla pagina di login e ai CSS
                                            .antMatchers("/admin/**").authenticated() // Richiedi autenticazione per tutte le pagine sotto /admin/
                                            .anyRequest().permitAll()
                                            .and()
                                            .formLogin()
                                            .loginPage("/admin/login")
                                            .defaultSuccessUrl("/admin/dashboard", true)
                                            .permitAll()
                                            .and()
                                            .logout()
                                            .logoutUrl("/admin/logout")
                                            .logoutSuccessUrl("/admin/login")
                                            .permitAll()
                                            .and()
                                            .exceptionHandling()
                                            .accessDeniedPage("/admin/login?error");
            """;

    private final static String extraAdminConfig = """
        public class SecurityConfig extends WebSecurityConfigurerAdapter {
        
            @Bean
                @Override
                public UserDetailsService userDetailsService() {
                    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
                    manager.createUser(User.withUsername(username)
                            .password(passwordEncoder().encode(password))
                            .roles("ADMIN")
                            .build());
                    return manager;
                }
                        
                @Bean
                public PasswordEncoder passwordEncoder() {
                    return new BCryptPasswordEncoder();
                }
                        
                @Bean
                public HttpSessionEventPublisher httpSessionEventPublisher() {
                    return new HttpSessionEventPublisher();
                }
            """;

    public void addAdminFeatures(String username, String password) {

        // Copio i file statici
        copyStaticFiles();

        // Aggiungo le configurazioni di sicurezza
        addSecurityConfig();

        // Aggiungo le credenziali al file application.properties
        ApplPropUtils.addApplicationPropertiesAdminConfig(username, password);

        // Aggiungo le configurazioni thymeleaf per pagine html
        ApplPropUtils.addApplicationPropertiesThymeleafConfig();

        // Aggiungo le dipendenze di sicurezza e thymeleaf per pagine html
        PomMavenUtils.configureAdminPom();
    }


    private void copyStaticFiles() {
        // Copio il file AdminController.java
        FileUtils.copyFile(FileUtils.buildPath(adminFilesSourcePath,"AdminController.java"), FileUtils.buildPath(generatedApiPath,"AdminController.java"));

        // Copio il file FakeLogGenerator.java
        FileUtils.copyFile(FileUtils.buildPath(adminFilesSourcePath,"FakeLogGenerator.java"), FileUtils.buildPath(generatedLogPath,"FakeLogGenerator.java"));

        // Copio i file di presentazione html, css, js
        FileUtils.copyDirectory(FileUtils.buildPath(adminFilesSourcePath, "admin_pages"), generatedStaticPath);

        // Abilito lo scheduling per generare i log fake
        FileUtils.replaceStringInFile(swagger2springboot, "import springfox.documentation.oas.annotations.EnableOpenApi;", "import org.springframework.scheduling.annotation.EnableScheduling;\nimport springfox.documentation.oas.annotations.EnableOpenApi;");
        FileUtils.replaceStringInFile(swagger2springboot, "public class Swagger2SpringBoot implements CommandLineRunner {", "@EnableScheduling\npublic class Swagger2SpringBoot implements CommandLineRunner {");
    }

    private void addSecurityConfig() {
        if (FileUtils.existsFile(generatedSecurityConfigPath)) {
            // Aggiungo gli import
            FileUtils.replaceStringInFile(generatedSecurityConfigPath, "import org.springframework.context.annotation.Configuration;", importAdminString);

            // Aggiungo i @Value user e pass
            FileUtils.replaceStringInFile(generatedSecurityConfigPath, "public class SecurityConfig extends WebSecurityConfigurerAdapter {", injectedAdminValues);

            // Aggiungo http config
            FileUtils.replaceStringInFile(generatedSecurityConfigPath, "protected void configure(HttpSecurity http) throws Exception {", securityAdminConfig);

            // Aggiungo funzioni di configurazione extra
            FileUtils.replaceStringInFile(generatedSecurityConfigPath, "public class SecurityConfig extends WebSecurityConfigurerAdapter {", extraAdminConfig);
        }
        else {
            FileUtils.copyFile(FileUtils.buildPath(adminFilesSourcePath,"SecurityConfig.java"), generatedSecurityConfigPath);
        }
    }

}
