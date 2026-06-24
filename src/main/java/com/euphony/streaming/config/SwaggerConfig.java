package com.euphony.streaming.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.http.HttpHeaders;


@OpenAPIDefinition(
        info = @Info(
                title = "Euphony API - Plataforma de Streaming de Música",
                description = "API RESTful para la gestión de una plataforma de streaming de música. "
                        + "Permite a los usuarios explorar canciones, reproducir música en tiempo real, "
                        + "crear y gestionar listas de reproducción, y a los artistas administrar "
                        + "su contenido en la plataforma.",
                termsOfService = "https:euphony.devbyjose.studio/terminos_y_condiciones",
                version = "1.0.0",
                contact = @Contact(
                        name = "Jose Sanchez",
                        url = "https://www.devbyjose.studio/contacto",
                        email = "devbyjose@gmail.com"
                ),
                license = @License(
                        name = "Apache License 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html",
                        identifier = "Apache-2.0"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Servidor de desarrollo local para pruebas y desarrollo"
                ),
                @Server(
                        url = "https://euphony.devbyjose.studio",
                        description = "Servidor de producción en el que la API está disponible públicamente"
                )
        },
        security = @SecurityRequirement(
                name = "Bearer Authentication"
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        description = "Esquema de autenticación basado en tokens JWT. El token se debe incluir en el encabezado "
                + HttpHeaders.AUTHORIZATION + " como 'Bearer {token}'.",
        type = SecuritySchemeType.HTTP,
        paramName = HttpHeaders.AUTHORIZATION,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig { }
