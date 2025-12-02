package com.huzaifaproject.apigateway.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity serverHttpSecurity) {
        serverHttpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchange ->
                        exchange.pathMatchers("/eureka/**").permitAll()
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/auth/signup").permitAll()
                                // Admin-only product operations
                                .pathMatchers(HttpMethod.POST, "/api/product").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/api/product/**").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/api/product/**").hasRole("ADMIN")
                                // Anyone authenticated can view products
                                .pathMatchers(HttpMethod.GET, "/api/product/**").authenticated()
                                .pathMatchers(HttpMethod.GET, "/api/product").authenticated()
                                // Order and inventory access
                                .pathMatchers("/api/order/**").authenticated()
                                .pathMatchers("/api/inventory/**").authenticated()
                                .anyExchange().authenticated())
                .oauth2ResourceServer(spec -> spec.jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())));
        return serverHttpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
