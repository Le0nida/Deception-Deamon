package cybersec.deception.deamon.services;

import cybersec.deception.deamon.model.SecurityConfig;
import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.servermanipulation.ApplPropUtils;
import cybersec.deception.deamon.utils.servermanipulation.PomMavenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SecurityService {

    @Value("${securityconfig.source.path}")
    private String secConfigSourcePath;

    @Value("${securityconfig.password.source.path}")
    private String secConfigPasswordSourcePath;

    @Value("${securityconfig.implicit.destination.path}")
    private String secConfigImplicitSourcePath;

    @Value("${securityconfig.destination.path}")
    private String secConfigDestinationPath;


    public void manageSecurity(SecurityConfig securityConfig) {

        // Aggiungo le dipendenze di spring secuirty al pom
        PomMavenUtils.configureSecurityPom();

        // Setto i parametri oauth in application.properties
        ApplPropUtils.addApplicationPropertiesSecurityConfig(securityConfig);

        // Definisco i metodi da autorizzare nel file SecurityConfig.java
        List<String> newContent = new ArrayList<>();
        if (securityConfig.getFlowType().equals("password")) {
            List<String> fileContent = FileUtils.leggiFile(secConfigPasswordSourcePath);
            for (String line: fileContent) {
                newContent.add(line);
                if (line.trim().equals(".authorizeRequests()")) {
                    newContent.add(createAntMatchersLine(securityConfig.getScopesMap()));
                }
                if (line.contains(".password(passwordEncoder().encode(")) {
                    newContent.add(createAuthoritiesLine(securityConfig.getScopesMap()));
                }
            }

        }
        else if (securityConfig.getFlowType().equals("implicit")) {
            List<String> fileContent = FileUtils.leggiFile(secConfigImplicitSourcePath);
            for (String line: fileContent) {
                newContent.add(line);
                if (line.trim().equals(".authorizeRequests()")) {
                    newContent.add(createAntMatchersLine(securityConfig.getScopesMap()));
                }
            }

        } else {
            List<String> fileContent = FileUtils.leggiFile(secConfigSourcePath);
            for (String line: fileContent) {
                if (line.contains(".and().oauth2Login()") && securityConfig.getFlowType().equals("client_credentials")) {
                    newContent.add(".and().oauth2Client().and().oauth2ResourceServer().jwt();");
                    continue;
                }

                newContent.add(line);
                if (line.trim().equals(".authorizeRequests()")) {
                    newContent.add(createAntMatchersLine(securityConfig.getScopesMap()));
                }

            }
        }
        FileUtils.scriviFile(secConfigDestinationPath, newContent);

        if (securityConfig.getFlowType().equals("password")) {
            FileUtils.replaceStringInFile(secConfigDestinationPath, "usernameTOSUBSTITUTE", securityConfig.getUsername());
            FileUtils.replaceStringInFile(secConfigDestinationPath, "passwordTOSUBSTITUTE", securityConfig.getPassword());
        }
    }

    private String createAuthoritiesLine(Map<String, List<String>> scopesMap) {

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String path: scopesMap.keySet()) {
            if (scopesMap.get(path) != null && !scopesMap.get(path).isEmpty()) {
                sb.append(".authorities(");
                for (String scope: scopesMap.get(path)) {
                    if (first) {
                        first = false;
                    }
                    else {
                        sb.append(", ");
                    }
                    sb.append("\"").append(scope).append("\"");
                }
                sb.append(")\n");
            }

        }
        return sb.toString();
    }

    private String createAntMatchersLine(Map<String, List<String>> scopesMap) {

        StringBuilder sb = new StringBuilder();
        for (String path: scopesMap.keySet()) {
            String realPath = path.split(" - ")[0];
            String httpMethod = path.split(" - ")[1];
            sb.append(".antMatchers(HttpMethod.").append(httpMethod).append(",\"").append(realPath).append("\")").append(".hasAnyAuthority(");;
            if (scopesMap.get(path) != null && !scopesMap.get(path).isEmpty()) {

                boolean first = true;
                for (String scope: scopesMap.get(path)) {
                    if (first) {
                        first = false;
                    }
                    else {
                        sb.append(", ");
                    }
                    sb.append("\"").append(scope).append("\"");
                }

            }
            sb.append(")\n");
        }
        return sb.toString();
    }
}
