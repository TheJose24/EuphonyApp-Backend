package com.euphony.streaming.util;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientMappingsRepresentation;

import java.util.Map;

/**
 * Proveedor de servicios de Keycloak para gestionar la interacción con la API de Keycloak.
 * Uso:
 * Esta clase no puede ser instanciada directamente, ya que está diseñada como una clase de utilidad.
 * Provee métodos estáticos para obtener recursos de Keycloak utilizando las credenciales y configuraciones definidas en variables de entorno.
 */
@Slf4j
public class KeycloakProvider {

    // Constructor privado para evitar la instanciacion de esta clase de utilidad
    private KeycloakProvider() {
        throw new IllegalStateException("Clase de utilidad");
    }

    // Variables de entorno necesarias para conectarse a Keycloak
    private static final String SERVER_URL = System.getenv("KEYCLOAK_SERVER_URL");
    private static final String REALM_NAME = System.getenv("KEYCLOAK_REALM_NAME");
    private static final String REALM_MASTER = System.getenv("KEYCLOAK_REALM_MASTER");
    private static final String ADMIN_CLI = System.getenv("KEYCLOAK_CLIENT_ADMIN_CLI");
    private static final String USER_CONSOLE = System.getenv("KEYCLOAK_USER_CONSOLE");
    private static final String PASSWORD_CONSOLE = System.getenv("KEYCLOAK_PASSWORD_CONSOLE");
    private static final String CLIENT_SECRET = System.getenv("KEYCLOAK_CLIENT_SECRET");

    private static final Keycloak keycloak;

    static  {
        keycloak = KeycloakBuilder.builder()
                .serverUrl(SERVER_URL)                // URL del servidor Keycloak
                .realm(REALM_MASTER)                  // Realm maestro (para autenticación)
                .clientId(ADMIN_CLI)                  // Cliente admin (Admin CLI)
                .username(USER_CONSOLE)               // Usuario admin
                .password(PASSWORD_CONSOLE)           // Contraseña admin
                .clientSecret(CLIENT_SECRET)          // Secreto del cliente
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(20)       // Tamaño del pool de conexiones para mejorar la concurrencia
                        .build())
                .build();

        // liberar la conexión al terminar la aplicación
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (keycloak != null) {
                keycloak.close();
                log.info("Conexión de Keycloak cerrada.");
            }
        }));
    }

    /**
     * Obtiene el recurso del realm definido en las variables de entorno.
     * Este metodo crea una instancia de Keycloak para conectarse a la API de Keycloak y acceder a los recursos del realm.
     *
     * @return RealmResource que permite acceder a los recursos del realm.
     */
    public static RealmResource getRealmResource() {
        return keycloak.realm(REALM_NAME);
    }

    /**
     * Obtiene el recurso de usuarios del realm.
     * Este recurso permite gestionar usuarios dentro del realm especificado.
     *
     * @return UsersResource que permite realizar operaciones CRUD sobre los usuarios.
     */
    public static UsersResource getUsersResource() {
        RealmResource realmResource = getRealmResource();
        return realmResource.users();
    }

    /**
     * Obtiene los mapeos de roles de cliente para un usuario específico en el realm.
     * Este metodo permite obtener los roles asignados a un usuario en el contexto de diferentes clientes de Keycloak.
     *
     * @param userId El ID del usuario para el que se desea obtener los roles.
     * @return Mapa que contiene los mapeos de roles de cliente del usuario.
     */
    public static Map<String, ClientMappingsRepresentation> getUserRolesResource(String userId) {
        RealmResource realmResource = getRealmResource();
        return realmResource.users().get(userId).roles().getAll().getClientMappings();
    }
}
