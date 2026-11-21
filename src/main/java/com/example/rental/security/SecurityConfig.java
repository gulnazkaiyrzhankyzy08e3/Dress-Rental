package com.example.rental.security;

import com.example.rental.handler.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // 1. Password hashing

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    // 2. STAFF and MANAGER users

    @Bean
    @Profile("dev")
    UserDetailsService devUsers(PasswordEncoder encoder) {

        return new InMemoryUserDetailsManager(

                User.withUsername("staff")
                        .password(encoder.encode("staff-dev"))
                        .roles("STAFF")
                        .build(),

                User.withUsername("manager")
                        .password(encoder.encode("manager-dev"))
                        .roles("MANAGER")
                        .build()
        );
    }

    // 3. Authentication Manager

    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService users,
            PasswordEncoder encoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);

        return new ProviderManager(provider);
    }

    // 4. JWT Secret Key

    @Bean
    SecretKey jwtKey(
            @Value("${app.jwt.secret}") String secret) {

        byte[] bytes = secret.getBytes(
                StandardCharsets.UTF_8
        );

        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 UTF-8 bytes"
            );
        }

        return new SecretKeySpec(
                bytes,
                "HmacSHA256"
        );
    }

    // 5. JWT Encoder

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) {

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(key)
        );
    }

    // 6. JWT Decoder

    @Bean
    JwtDecoder jwtDecoder(SecretKey key) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(key)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        TokenService.ISSUER
                )
        );

        return decoder;
    }

    // 7. JWT Role Converter

    @Bean
    JwtAuthenticationConverter rolesFromToken() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return converter;
    }

    // 8. Correlation ID

    private static String correlation(
            HttpServletRequest request) {

        String id = request.getHeader(
                "X-Correlation-Id"
        );

        return id == null || id.isBlank()
                ? UUID.randomUUID().toString()
                : id;
    }

    // 9. Security error response

    private static void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            ObjectMapper mapper,
            int status,
            String code,
            String message)
            throws java.io.IOException {

        response.setStatus(status);

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        mapper.writeValue(
                response.getOutputStream(),
                new ApiError(
                        code,
                        message,
                        correlation(request)
                )
        );
    }

    // 10. Security Filter Chain

    @Bean
    SecurityFilterChain api(
            HttpSecurity http,
            ObjectMapper mapper) throws Exception {

        AuthenticationEntryPoint unauth =
                (req, res, ex) -> writeError(
                        req,
                        res,
                        mapper,
                        401,
                        "UNAUTHENTICATED",
                        "valid token required"
                );

        AccessDeniedHandler forbidden =
                (req, res, ex) -> writeError(
                        req,
                        res,
                        mapper,
                        403,
                        "FORBIDDEN",
                        "not allowed for your role"
                );

        return http

                .authorizeHttpRequests(r -> r

                        // Public website

                        .requestMatchers(
                                HttpMethod.GET,
                                "/",
                                "/index.html",
                                "/style.css",
                                "/app.js",
                                "/favicon.ico"
                        ).permitAll()

                        // Internal error handling

                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()

                        // Public login endpoint

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login"
                        ).permitAll()

                        // Health check

                        .requestMatchers(
                                HttpMethod.GET,
                                "/actuator/health"
                        ).permitAll()

                        // Swagger

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // MANAGER only
                        // RENTED -> RETURNED

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/rentals/*/complete"
                        ).hasRole("MANAGER")

                        // STAFF and MANAGER
                        // BOOKED -> RENTED

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/rentals/*/start"
                        ).hasAnyRole(
                                "STAFF",
                                "MANAGER"
                        )

                        // Protected API

                        .requestMatchers(
                                "/api/v1/**",
                                "/api/me"
                        ).hasAnyRole(
                                "STAFF",
                                "MANAGER"
                        )

                        // Block other endpoints

                        .anyRequest().denyAll()
                )

                // Stateless JWT authentication

                .csrf(c -> c.disable())

                .sessionManagement(s ->
                        s.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .formLogin(f -> f.disable())

                .httpBasic(b -> b.disable())

                // 401 and 403 responses

                .exceptionHandling(e -> e
                        .authenticationEntryPoint(unauth)
                        .accessDeniedHandler(forbidden)
                )

                // JWT Resource Server

                .oauth2ResourceServer(o -> o
                        .jwt(j ->
                                j.jwtAuthenticationConverter(
                                        rolesFromToken()
                                )
                        )
                        .authenticationEntryPoint(unauth)
                        .accessDeniedHandler(forbidden)
                )

                .build();
    }
}