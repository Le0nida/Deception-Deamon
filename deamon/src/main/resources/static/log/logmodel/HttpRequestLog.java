package io.swagger.log.logmodel;

import javax.persistence.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Entity
@Table(name = "TABLECODETOSUBSTITUTElogs")
public class HttpRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "request_url")
    private String requestURL;

    @Column(name = "headers_host")
    private String headers_host;

    @Column(name = "headers_useragent")
    private String headers_useragent;

    @Column(name = "headers_contenttype")
    private String headers_contenttype;

    @Column(name = "headers_acccept")
    private String headers_acccept;

    @Column(name = "headers_authorization")
    private String headers_authorization;

    @Column(name = "query_parameters")
    private String queryParameters;

    @Column(name = "request_body")
    private String requestBody;

    @Column(name = "client_ip_address")
    private String clientIPAddress;

    @Column(name = "client_port")
    private int clientPort;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "authentication_type")
    private String authenticationType;

    @Column(name = "accepted_content_types")
    private String acceptedContentTypes;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "accepted_compression_types")
    private String acceptedCompressionTypes;

    @Column(name = "accepted_connection_types")
    private String acceptedConnectionTypes;

    @Column(name = "cookies")
    private String cookies;

    public HttpRequestLog(HttpServletRequest request) {
        // Metodo HTTP
        this.httpMethod = request.getMethod();

        // URL della richiesta
        this.requestURL = request.getRequestURL().toString();

        // Intestazioni della richiesta
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            if (headerName.equalsIgnoreCase("Host")) {
                this.headers_host = headerValue;
            } else if (headerName.equalsIgnoreCase("Authorization")) {
                this.headers_authorization = headerValue;
            } else if (headerName.equalsIgnoreCase("User-Agent")) {
                this.headers_useragent = headerValue;
            } else if (headerName.equalsIgnoreCase("Content-Type")) {
                this.headers_contenttype = headerValue;
            } else if (headerName.equalsIgnoreCase("Accept")) {
                this.headers_acccept = headerValue;
            }
        }

        // Parametri della query (se presenti)
        StringBuilder queryParamsBuilder = new StringBuilder();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            queryParamsBuilder.append(paramName).append(": ").append(paramValue).append("\n");
        }
        this.queryParameters = queryParamsBuilder.toString();

        // Corpo della richiesta (se presente)
        this.requestBody = getRequestBody(request);

        // Indirizzo IP del client
        this.clientIPAddress = request.getRemoteAddr();

        // Porta del client
        this.clientPort = request.getRemotePort();

        // Protocollo utilizzato
        this.protocol = request.getProtocol();

        // Tipo di autenticazione (se presente)
        this.authenticationType = request.getAuthType();

        // Tipo di contenuto accettato dal client
        this.acceptedContentTypes = request.getHeader("Accept");

        // Lingua preferita del client
        this.preferredLanguage = request.getHeader("Accept-Language");

        // Tipo di compressione accettata dal client
        this.acceptedCompressionTypes = request.getHeader("Accept-Encoding");

        // Tipo di connessione accettata dal client
        this.acceptedConnectionTypes = request.getHeader("Connection");

        // Informazioni sulle cookies (se presenti)
        StringBuilder cookiesBuilder = new StringBuilder();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookiesBuilder.append(cookie.getName()).append(": ").append(cookie.getValue()).append("\n");
            }
        }
        this.cookies = cookiesBuilder.toString();
    }

    public HttpRequestLog() {

    }

    private String getRequestBody(HttpServletRequest request) {
        return request.toString();
        // TODO to implement
    }


    public Long getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public String getHeaders_host() {
        return headers_host;
    }

    public String getHeaders_useragent() {
        return headers_useragent;
    }

    public String getHeaders_contenttype() {
        return headers_contenttype;
    }

    public String getHeaders_acccept() {
        return headers_acccept;
    }

    public String getHeaders_authorization() {
        return headers_authorization;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getClientIPAddress() {
        return clientIPAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public String getAcceptedContentTypes() {
        return acceptedContentTypes;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public String getAcceptedCompressionTypes() {
        return acceptedCompressionTypes;
    }

    public String getAcceptedConnectionTypes() {
        return acceptedConnectionTypes;
    }

    public String getCookies() {
        return cookies;
    }

}
