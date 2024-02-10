package io.swagger.log.logmodel;

import java.util.List;

public class LogResponse {

    private String message;

    private List<HttpRequestLog> logs;

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLogs(List<HttpRequestLog> logs) {
        this.logs = logs;
    }
}
