package io.swagger.configuration;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .oauth2Login()
                .tokenEndpoint()
                .accessTokenResponseClient(implicitAccessTokenResponseClient())
                .and()
                .defaultSuccessUrl("/").and().csrf().disable();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> implicitAccessTokenResponseClient() {
        return authorizationGrantRequest -> {
            HttpServletRequest httpRequest = ((ServletWebRequest) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

            String accessToken = httpRequest.getParameter("access_token");
            String expiresIn = httpRequest.getParameter("expires_in");
            String[] scopesArray = httpRequest.getParameterValues("scope");
            Set<String> scopes = new HashSet<>(Arrays.asList(scopesArray));

            return OAuth2AccessTokenResponse.withToken(accessToken)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(Long.parseLong(expiresIn))
                    .scopes(scopes)
                    .build();
        };
    }

}
