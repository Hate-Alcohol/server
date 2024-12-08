package org.example.hatealcohol.config;

import org.example.hatealcohol.jwt.JWTFilter;
import org.example.hatealcohol.jwt.JWTUtil;
import org.example.hatealcohol.oauth2.CustomSuccessHandler;
import org.example.hatealcohol.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;
    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, CustomSuccessHandler customSuccessHandler, JWTUtil jwtUtil) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //csrf disable -> jwt를 발급해서 stateless상태로 세션을 관리할거기 때문에 disable
        http
            .csrf((auth) -> auth.disable());
        //From 로그인 방식 disable -> jwt와 ouath를 사용하여 로그인을 진행하기에 disable
        http
            .formLogin((auth) -> auth.disable());
        //HTTP Basic 인증 방식 disable -> jwt와 ouath를 사용하여 로그인을 진행하기에 disable
        http
            .httpBasic((auth) -> auth.disable());
        //JWTFilter 추가
        http
            .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        //oauth2
        http
            .oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                    .userService(customOAuth2UserService))
                .successHandler(customSuccessHandler));
        //경로별 인가 작업 -> 일단은 / 경로만
        http
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers("/").permitAll()
                .anyRequest().authenticated());
        //세션 설정 : STATELESS -> jwt를 발급하고 jwt를 통해서 인증, 인가를 진행할거기 때문에 stateless
        http
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}