# Euphony · Backend

Plataforma de streaming de audio construida en Java 21 y Spring Boot, con
autenticación centralizada e infraestructura propia de despliegue.

## Qué resuelve

Reproducción y catálogo de audio con usuarios, roles y sesiones gestionados
fuera de la aplicación: la autenticación vive en **Keycloak**, de modo que
ningún servicio inventa su propio manejo de sesión ni almacena credenciales.

## Stack

| Área | Tecnología |
| --- | --- |
| Lenguaje | Java 21 |
| Framework | Spring Boot · Spring Security |
| Autenticación | Keycloak (OAuth2 / OpenID Connect) |
| Base de datos | PostgreSQL |
| Documentación de API | Swagger / OpenAPI |
| Despliegue | Servidor privado |

## Puesta en marcha

Requisitos: **JDK 21**, una instancia de **PostgreSQL** y un **Keycloak**
accesible con el realm del proyecto configurado.

```bash
git clone https://github.com/TheJose24/EuphonyApp-Backend.git
cd EuphonyApp-Backend
./mvnw spring-boot:run
```

La API queda documentada en `/swagger-ui.html` una vez arrancada.

## Frontend

El cliente vive en un repositorio aparte:
[euphony-front](https://github.com/TheJose24/euphony-front) — Angular 21 con
componentes standalone, detección de cambios sin zone.js y señales.

## Contribuir

El flujo de ramas, la convención de mensajes de commit y el proceso de revisión
están en [CONTRIBUTING.md](CONTRIBUTING.md).

## Contexto

Proyecto desarrollado en equipo, con liderazgo técnico y de gestión aplicando
PMBOK: planificación de entregas, reparto de trabajo y revisión de código.
