package cybersec.deception.deamon.utils.servermanipulation;

import cybersec.deception.deamon.model.SecurityConfig;
import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Component
public class ApplPropUtils {

    private static String driverClass;
    private static String url;
    private static String username;
    private static String password;
    private static String appPropertiesPath;
    private static String instructionTxtPath;

    @Value("${instructions.server}")
    public void setInstructionTxtPath(String instructionTxtPath) {
        ApplPropUtils.instructionTxtPath = instructionTxtPath;
    }

    @Value("${database.driverclass}")
    public void setDriverClass(String driverClass) {
        ApplPropUtils.driverClass = driverClass;
    }

    @Value("${database.url}")
    public void setUrl(String url) {
        ApplPropUtils.url = url;
    }

    @Value("${database.username}")
    public void setUsername(String username) {
        ApplPropUtils.username = username;
    }

    @Value("${database.password}")
    public void setPassword(String password) {
        ApplPropUtils.password = password;
    }

    @Value("${application.properties.location}")
    public void setAppPropertiesPath(String appPropertiesPath) {
        ApplPropUtils.appPropertiesPath = appPropertiesPath;
    }

    public static void addApplicationPropertiesJPAconfig() {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(appPropertiesPath, true))) {
            writer.newLine();
            writer.newLine();
            writer.write("# Configurazione del datasource");
            writer.newLine();
            writer.write("spring.datasource.url="+url);
            writer.newLine();
            writer.write("spring.datasource.username=" + username);
            writer.newLine();
            writer.write("spring.datasource.password=" + password);
            writer.newLine();
            writer.write("spring.datasource.driver-class-name=" + driverClass);
            writer.newLine();
            writer.newLine();
            writer.write("# Configurazione JPA");
            writer.newLine();
            writer.write("spring.jpa.properties.hibernate.show_sql=true");
            writer.newLine();
            writer.write("spring.jpa.hibernate.ddl-auto=update");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addApplicationPropertiesCryptoConfig() {

        String encriptionKey = TokenUtils.generateEncryptionKey();
        String decryptedToken = TokenUtils.generateToken(32);
        // aggiungo il token cifrato al file di istruzioni
        String encr = TokenUtils.encryptToken(encriptionKey, decryptedToken);
        FileUtils.replaceStringInFile(instructionTxtPath, "TOKEN_TO_INSERT", encr);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(appPropertiesPath, true))) {

            writer.newLine();
            writer.newLine();
            writer.write("# Gestione log");
            writer.newLine();
            writer.write("crypto.key="+encriptionKey);
            writer.newLine();
            writer.write("token.decrypted.value="+decryptedToken);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addApplicationPropertiesSecurityConfig(SecurityConfig config) {

        // implicit e authcode sono uguali ma implicit non ha il secret
        // anche pwd non aggiunge nulla

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(appPropertiesPath, true))) {
            writer.newLine();
            writer.newLine();
            writer.write("# Configurazione della sicurezza oauth");
            writer.newLine();
            writer.write("spring.security.oauth2.client.registration.defaultsecurity.client-id="+config.getClient_id());
            writer.newLine();
            writer.write("spring.security.oauth2.client.registration.defaultsecurity.client-secret="+config.getClient_secret());
            writer.newLine();
            writer.write("spring.security.oauth2.client.registration.defaultsecurity.authorization-grant-type=" + config.getFlowType());
            writer.newLine();
            writer.write("spring.security.oauth2.client.registration.defaultsecurity.redirect-uri-template={baseUrl}/login/oauth2/code/{registrationId}");
            writer.newLine();
            writer.write("spring.security.oauth2.client.registration.defaultsecurity.client-name=RestApiServer");
            writer.newLine();
            writer.write("spring.security.oauth2.client.registration.defaultsecurity.scope=" + config.getScopesInString());
            writer.newLine();
            writer.newLine();
            writer.write("spring.security.oauth2.client.provider.defaultsecurity.authorization-uri=" + config.getAuthorizationUri());
            writer.newLine();
            writer.write("spring.security.oauth2.client.provider.defaultsecurity.token-uri=" + config.getTokenUri());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addApplicationPropertiesAdminConfig(String adminUsername, String adminPassword) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(appPropertiesPath, true))) {
            writer.newLine();
            writer.newLine();
            writer.write("# Configurazione della credenziali di amministrazione");
            writer.newLine();
            writer.write("admin.username="+adminUsername);
            writer.newLine();
            writer.write("admin.password="+adminPassword);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
