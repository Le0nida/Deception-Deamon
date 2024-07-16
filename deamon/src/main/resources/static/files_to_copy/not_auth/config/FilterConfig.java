package io.swagger.configuration.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Value("${not_auth.patterns}")
    private String notAuthPatterns;

    @Bean
    public FilterRegistrationBean<RandomRedirectFilter> loggingFilter() {
        FilterRegistrationBean<RandomRedirectFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new RandomRedirectFilter());
        registrationBean.addUrlPatterns(notAuthPatterns);

        return registrationBean;
    }
}

