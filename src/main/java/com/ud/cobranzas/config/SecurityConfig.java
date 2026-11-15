package com.ud.cobranzas.config;

import com.ud.cobranzas.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Archivos estáticos y login públicos
                        .requestMatchers("/", "/index.html", "/styles.css", "/app.js", "/favicon.ico").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // Endpoints protegidos para cobradores
                        .requestMatchers("/api/cobradores/**").authenticated()
                        .requestMatchers("/api/creditos/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
