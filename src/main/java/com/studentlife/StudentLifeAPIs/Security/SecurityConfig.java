package com.studentlife.StudentLifeAPIs.Security;

import com.studentlife.StudentLifeAPIs.Jwt.JwtAccessDeniedHandler;
import com.studentlife.StudentLifeAPIs.Jwt.JwtAuthFilter;
import com.studentlife.StudentLifeAPIs.Jwt.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthFilter authFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                                .requestMatchers("/api/v1/me").authenticated()
                                .requestMatchers("/api/v1/schedule/**").hasRole("student")
                                .requestMatchers("/api/v1/admin/**").hasRole("admin")

//                                .anyRequest().permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // allow all origin to access this server
        config.addAllowedOriginPattern("*");

//        config.setAllowedOrigins(List.of(
//                "http://localhost:3000",
//                "http://localhost:5173"
//        ));

        String[] allowedMethods = {
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        };
        for (String method : allowedMethods) {
            config.addAllowedMethod(method);
        }

        // allow all header
        config.addAllowedHeader("*");
        config.addExposedHeader("*");

//        config.setAllowedHeaders(List.of(
//                "Authorization", "Content-Type"
//        ));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
