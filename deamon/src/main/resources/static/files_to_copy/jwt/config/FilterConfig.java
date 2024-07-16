package io.swagger.configuration.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RandomRedirectFilter> loggingFilter() {
        FilterRegistrationBean<RandomRedirectFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new RandomRedirectFilter());
        registrationBean.addUrlPatterns("/passwords/*");

        return registrationBean;
    }
}

