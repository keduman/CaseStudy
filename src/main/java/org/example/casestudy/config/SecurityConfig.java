package org.example.casestudy.config;

import org.example.casestudy.filter.BasicAuthFilter;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final BasicAuthFilter basicAuthFilter;

    public SecurityConfig( BasicAuthFilter basicAuthFilter) {
        this.basicAuthFilter = basicAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/auth").permitAll()
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers("/api/customers").hasAuthority("ROLE_ADMIN")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(basicAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())); // For H2 Console
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // bcrypt-encoded passwords
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:testdb")
                .username("sa")
                .password("")
                .driverClassName("org.h2.Driver")
                .build();
    }
}