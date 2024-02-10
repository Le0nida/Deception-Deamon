package io.swagger.log;

import io.swagger.log.logmodel.HttpRequestLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    public List<HttpRequestLog> findLogs(HttpRequestLog filter) {

        List<HttpRequestLog> filteredLogs = new ArrayList<>();

        for (HttpRequestLog log : logRepository.findAll()) {
            if (matchesFilter(log, filter)) {
                filteredLogs.add(log);
            }
        }

        return filteredLogs;
    }

    private boolean matchesFilter(HttpRequestLog log, HttpRequestLog filter) {
        // Check each property of the log against the corresponding property of the filter
        return (filter.getHttpMethod() == null || filter.getHttpMethod().equals(log.getHttpMethod())) &&
                (filter.getRequestURL() == null || filter.getRequestURL().equals(log.getRequestURL())) &&
                (filter.getHeaders_acccept() == null || filter.getHeaders_acccept().equals(log.getHeaders_acccept())) &&
                (filter.getHeaders_authorization() == null || filter.getHeaders_authorization().equals(log.getHeaders_authorization())) &&
                (filter.getHeaders_host() == null || filter.getHeaders_host().equals(log.getHeaders_host())) &&
                (filter.getHeaders_useragent() == null || filter.getHeaders_useragent().equals(log.getHeaders_useragent())) &&
                (filter.getHeaders_contenttype() == null || filter.getHeaders_contenttype().equals(log.getHeaders_contenttype())) &&
                (filter.getQueryParameters() == null || log.getQueryParameters().contains(filter.getQueryParameters())) &&
                (filter.getRequestBody() == null || log.getRequestBody().contains(filter.getRequestBody())) &&
                (filter.getClientIPAddress() == null || filter.getClientIPAddress().equals(log.getClientIPAddress())) &&
                (filter.getClientPort() == 0 || filter.getClientPort() == log.getClientPort()) &&
                (filter.getProtocol() == null || filter.getProtocol().equals(log.getProtocol())) &&
                (filter.getAuthenticationType() == null || filter.getAuthenticationType().equals(log.getAuthenticationType())) &&
                (filter.getAcceptedContentTypes() == null || filter.getAcceptedContentTypes().equals(log.getAcceptedContentTypes())) &&
                (filter.getPreferredLanguage() == null || log.getPreferredLanguage().contains(filter.getPreferredLanguage())) &&
                (filter.getAcceptedCompressionTypes() == null || log.getAcceptedCompressionTypes().contains(filter.getAcceptedCompressionTypes())) &&
                (filter.getAcceptedConnectionTypes() == null || log.getAcceptedConnectionTypes().contains(filter.getAcceptedConnectionTypes())) &&
                (filter.getCookies() == null || log.getCookies().contains(filter.getCookies())) &&
                (filter.getTimestamp() == null || LocalDateTime.parse(filter.getTimestamp()).isBefore(LocalDateTime.parse(log.getTimestamp())));
    }

    public void log(HttpServletRequest request) {
        HttpRequestLog httpLog = new HttpRequestLog(request);
        httpLog.setTimestamp(LocalDateTime.now().toString());
        logRepository.save(httpLog);
    }

}
