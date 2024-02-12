package cybersec.deception.deamon.model;

import java.util.List;
import java.util.Map;

public class SecurityConfig {

    private String flowType;

    private String client_id;

    private String client_secret;

    private String username;

    private String password;

    private List<String> scopes;

    private String authorizationUri;

    private String tokenUri;

    private Map<String, List<String>> scopesMap;

    public SecurityConfig() {
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getAuthorizationUri() {
        return authorizationUri;
    }

    public void setAuthorizationUri(String authorizationUri) {
        this.authorizationUri = authorizationUri;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public String getScopesInString() {
        return String.join(", ", scopes);
    }

    public Map<String, List<String>> getScopesMap() {
        return scopesMap;
    }

    public void setScopesMap(Map<String, List<String>> scopesMap) {
        this.scopesMap = scopesMap;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
