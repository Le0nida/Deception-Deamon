package io.swagger.log.logmodel;

import java.util.List;

public class LogResponse {

    private String message;

    private List<HttpRequestLog> logs;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<HttpRequestLog> getLogs() {
        return logs;
    }

    public void setLogs(List<HttpRequestLog> logs) {
        this.logs = logs;
    }

    public LogResponse() {
    }

    public LogResponse(String message, List<HttpRequestLog> logs) {
        this.message = message;
        this.logs = logs;
    }
}
