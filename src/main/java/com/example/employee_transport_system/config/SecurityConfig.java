package com.example.employee_transport_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .csrf(csrf -> csrf.disable())

                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))


                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // public endpoints
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/employees", "/employees/**").permitAll()
                        .requestMatchers("/routes", "/routes/**").permitAll()
                        .requestMatchers("/booking", "/booking/**").permitAll()

                        // admin endpoints require authentication
                        .requestMatchers("/admin", "/admin/**").authenticated()

                        // everything else requires authentication
                        .anyRequest().authenticated()
                )

                .httpBasic();

        return http.build();
    }
}
