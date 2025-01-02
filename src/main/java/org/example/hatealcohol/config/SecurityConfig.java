package org.example.hatealcohol.config;

import org.example.hatealcohol.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf((auth) -> auth.disable());

        http
            .formLogin((auth) -> auth.disable());

        http
            .httpBasic((auth) -> auth.disable());

        http
            .oauth2Login((oauth2)->oauth2
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                    .userService(customOAuth2UserService))
                .successHandler((request, response, authentication) -> {
                    System.out.println("로그인 성공! 유저 정보: " + authentication.getPrincipal());
                    response.sendRedirect("/success");
                })
                .failureHandler((request, response, exception) -> {
                    System.out.println("로그인 실패: " + exception.getMessage());
                    response.sendRedirect("/");
                }));

        http
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers("/", "/success").permitAll()
                .anyRequest().authenticated());

        http
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}