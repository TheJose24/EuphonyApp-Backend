package com.euphony.streaming.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de CORS centralizada para toda la API.
 *
 * <p>Los orígenes permitidos se leen de la propiedad {@code app.cors.allowed-origins}
 * (lista separada por comas), por lo que se pueden añadir/quitar sin recompilar. El bean
 * {@link CorsConfigurationSource} lo consume Spring Security a través de {@code http.cors(...)}
 * en {@code SecurityConfig}, de modo que CORS se aplica dentro de la cadena de seguridad
 * (incluido el preflight {@code OPTIONS}) y no como una capa MVC separada.</p>
 */
@Configuration
public class WebConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        // Lista explícita de orígenes (no comodín): obligatorio porque allowCredentials=true.
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // Cabeceras útiles para el reproductor de audio (streaming con Range).
        config.setExposedHeaders(List.of("Content-Disposition", "Content-Range", "Accept-Ranges", "Content-Length"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
