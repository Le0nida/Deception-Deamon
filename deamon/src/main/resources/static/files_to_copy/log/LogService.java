package io.swagger.log;

import io.swagger.log.logmodel.HttpRequestLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    public List<HttpRequestLog> findLogs(HttpRequestLog filter) {

        if (filter == null) {
            return logRepository.findAll();
        }
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
        return (StringUtils.isEmpty(filter.getHttpMethod()) || filter.getHttpMethod().equals(log.getHttpMethod())) &&
                (StringUtils.isEmpty(filter.getRequestURL()) || filter.getRequestURL().equals(log.getRequestURL())) &&
                (StringUtils.isEmpty(filter.getHeaders_acccept()) || filter.getHeaders_acccept().equals(log.getHeaders_acccept())) &&
                (StringUtils.isEmpty(filter.getHeaders_authorization()) || filter.getHeaders_authorization().equals(log.getHeaders_authorization())) &&
                (StringUtils.isEmpty(filter.getHeaders_host()) || filter.getHeaders_host().equals(log.getHeaders_host())) &&
                (StringUtils.isEmpty(filter.getHeaders_useragent()) || filter.getHeaders_useragent().equals(log.getHeaders_useragent())) &&
                (StringUtils.isEmpty(filter.getHeaders_contenttype()) || filter.getHeaders_contenttype().equals(log.getHeaders_contenttype())) &&
                (StringUtils.isEmpty(filter.getQueryParameters()) || log.getQueryParameters().contains(filter.getQueryParameters())) &&
                (StringUtils.isEmpty(filter.getRequestBody()) || log.getRequestBody().contains(filter.getRequestBody())) &&
                (StringUtils.isEmpty(filter.getClientIPAddress()) || filter.getClientIPAddress().equals(log.getClientIPAddress())) &&
                (filter.getClientPort() == 0 || filter.getClientPort() == log.getClientPort()) &&
                (StringUtils.isEmpty(filter.getProtocol()) || filter.getProtocol().equals(log.getProtocol())) &&
                (StringUtils.isEmpty(filter.getAuthenticationType()) || log.getAuthenticationType().contains(filter.getAuthenticationType())) &&
                (StringUtils.isEmpty(filter.getAcceptedContentTypes()) || log.getAcceptedContentTypes().contains(filter.getAcceptedContentTypes())) &&
                (StringUtils.isEmpty(filter.getPreferredLanguage()) || log.getPreferredLanguage().contains(filter.getPreferredLanguage())) &&
                (StringUtils.isEmpty(filter.getAcceptedCompressionTypes()) || log.getAcceptedCompressionTypes().contains(filter.getAcceptedCompressionTypes())) &&
                (StringUtils.isEmpty(filter.getAcceptedConnectionTypes()) || log.getAcceptedConnectionTypes().contains(filter.getAcceptedConnectionTypes())) &&
                (StringUtils.isEmpty(filter.getCookies()) || log.getCookies().contains(filter.getCookies())) &&
                (StringUtils.isEmpty(filter.getTimestamp()) || LocalDateTime.parse(filter.getTimestamp()).isBefore(LocalDateTime.parse(log.getTimestamp())));
    }

    public void log(HttpServletRequest request) {
        HttpRequestLog httpLog = new HttpRequestLog(request);
        httpLog.setTimestamp(LocalDateTime.now().toString());
        logRepository.save(httpLog);
    }

}
