# **Flujo de Trabajo con Gitflow Simplificado**

## **Introducción**
Este documento describe el flujo de trabajo con Gitflow simplificado que utilizaremos en nuestro proyecto. La estructura está diseñada para mantener el desarrollo organizado y asegurar que todos los cambios se integren de manera controlada y sin conflictos.

## **Estructura de Ramas**

1. **`master`**
    - **Propósito**: Contiene el código que está listo para ser lanzado o entregado. Esta rama debe mantenerse siempre estable.
    - **Reglas**:
        - No se realizan cambios directos en `master`.
        - Solo se fusionan cambios desde `develop` cuando el proyecto está listo para una versión final.
        - Cada fusión en `master` debe ir acompañada de un tag para marcar la versión.

2. **`develop`**
    - **Propósito**: Es la rama de integración principal donde se combinan todas las nuevas funcionalidades antes de ser lanzadas.
    - **Reglas**:
        - Todos los cambios nuevos se integran primero en `develop`.
        - `develop` se mantiene lo más estable posible, asegurando que siempre sea funcional aunque incluya código en desarrollo.

3. **Ramas de `feature`**
    - **Propósito**: Se utilizan para desarrollar nuevas funcionalidades, mejoras o correcciones específicas.
    - **Reglas**:
        - Cada nueva funcionalidad se desarrolla en su propia rama `feature`, que se crea a partir de `develop`.
        - Una vez que la funcionalidad está completa y probada, se fusiona de nuevo en `develop` y la rama `feature` se elimina.
    - **Convención para nombrar ramas**:
      ```bash
      feature/nombre-corto-descriptivo
      ```
      Ejemplos:
        - `feature/autenticacion-usuarios`
        - `feature/mejora-ux-formulario`

## **Flujo de Trabajo**

1. **Crear una nueva rama `feature`**:
    - **Comandos**:
      ```bash
      git checkout develop
      git pull origin develop
      git checkout -b feature/nombre-corto-descriptivo
      ```

2. **Desarrollar la funcionalidad**:
    - Realiza cambios en la rama `feature`.
    - Haz commits frecuentes y descriptivos.

3. **Nombrar commits de manera estandarizada**:
    - **Estructura del mensaje**:
      ```
      tipo: Descripción corta del cambio (Issue #número)
      ```
    - **Tipos de commits**:
        - `feat`: Añadir una nueva funcionalidad.
        - `fix`: Corregir un bug.
        - `docs`: Cambios en la documentación.
        - `style`: Cambios que no afectan la lógica (formato, espacios).
        - `refactor`: Cambios en el código que no afectan la funcionalidad.
        - `test`: Añadir o modificar pruebas.
        - `chore`: Tareas de mantenimiento o configuraciones.
    - **Ejemplos**:
      ```
      feat: Añadir validación de email (Issue #15)
      fix: Corregir error en la carga de imágenes (Issue #12)
      ```

4. **Fusionar la rama `feature` en `develop`**:
   - Antes de fusionar, se debe realizar un **pull request**.
   - **Yo** sere el encargado de revisar los pull requests y dar el visto bueno para fusionar la rama en `develop`.

    - **Comandos**:
      ```bash
      git checkout develop
      git pull origin develop
      git merge --no-ff feature/nombre-corto-descriptivo
      git push origin develop
      git branch -d feature/nombre-corto-descriptivo
      ```

5. **Preparación para una versión final** (yo me encargaré de este paso):
    - Cuando el código en `develop` esté listo para una nueva versión:
        - Fusiona `develop` en `master`:
          ```bash
          git checkout master
          git pull origin master
          git merge --no-ff develop
          ```
        - **Taggear la versión**:
          ```bash
          git tag -a vX.X.X -m "Versión X.X.X"
          git push origin master --tags
          ```
        - **Mantener `develop` actualizado**:
          ```bash
          git checkout develop
          git merge master
          git push origin develop
          ```

## **Estándares y Buenas Prácticas**

- **Commit frecuentemente**: Hacer commits pequeños y frecuentes ayuda a mantener un historial claro y facilita la resolución de problemas.
- **Revisiones de código**: Antes de fusionar una rama `feature` en `develop` en el repositorio remoto, debes hacer un pull request para que **yo** revise el código. Ningún cambio se fusionará sin una revisión previa.
- **Naming Convention**: Utiliza nombres descriptivos y consistentes para las ramas y commits.
- **Mantén el repositorio limpio**: Elimina las ramas `feature` después de fusionarlas para evitar desorden en el repositorio.

## **Ejemplo Práctico**

1. **Crear una nueva funcionalidad**:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/autenticacion-usuarios
   ```
   
2. **Hacer commits**:
   ```bash
   git add .
   git commit -m "feat: Implementar autenticación básica de usuarios (Issue #2)"
   ```

3. **Fucionar en `develop`**:
   ```bash
   git checkout develop
   git merge --no-ff feature/autenticacion-usuarios
   git push origin develop
   git branch -d feature/autenticacion-usuarios
   ```
   
4. **Preparar para una nueva versión**:
   ```bash
   git checkout master
   git pull origin master
   git merge --no-ff develop
   git tag -a v1.0.0 -m "Versión 1.0.0"
   git push origin master --tags
   git checkout develop
   git merge master
   git push origin develop
   ```
