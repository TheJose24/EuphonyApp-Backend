---
name: Git Commit Helper
description: Genera mensajes de commit descriptivos en español analizando el diff de git. Úsala cuando el usuario pida ayuda para escribir mensajes de commit o revisar cambios staged.
hooks:
  PostToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: "echo \"[$(date)] Git Commit Helper: Analyzed git diff for commit message\" >> ~/.claude/git-commit-helper.log"
---

# Git Commit Helper

## Reglas obligatorias (este proyecto)

1. **El mensaje de commit SIEMPRE se escribe en español.** Tipo/scope de conventional
   commits se mantienen en inglés (`feat`, `fix`, …); la descripción y el cuerpo van en español.
2. **NUNCA agregues un trailer de coautoría.** Nada de `Co-Authored-By:` (ni de Claude ni de nadie).
3. **NUNCA menciones que el commit lo generó Claude, una IA o una herramienta.** Prohibido
   cualquier `Generated with Claude Code`, enlaces a claude.com, emojis tipo 🤖, o frases similares
   en el footer o el cuerpo. El commit debe leerse como escrito por la persona que lo hace.

El mensaje contiene solo: la línea de resumen, el cuerpo opcional y, si aplica, notas de breaking change.

## Inicio rápido

Analiza los cambios staged y genera el mensaje:

```bash
# Ver cambios staged
git diff --staged
```

## Formato del mensaje

Sigue conventional commits:

```
<tipo>(<scope>): <descripción en español>

[cuerpo opcional]

[footer opcional]
```

### Tipos

- **feat**: Nueva funcionalidad
- **fix**: Corrección de bug
- **docs**: Cambios de documentación
- **style**: Cambios de estilo de código (formato, puntos y comas)
- **refactor**: Refactorización
- **test**: Agregar o actualizar pruebas
- **chore**: Tareas de mantenimiento

### Ejemplos

**Commit de funcionalidad:**
```
feat(auth): agrega autenticación JWT

Implementa el sistema de autenticación basado en JWT con:
- Endpoint de login con generación de token
- Middleware de validación de token
- Soporte de refresh token
```

**Corrección de bug:**
```
fix(api): maneja valores nulos en el perfil de usuario

Evita fallos cuando los campos del perfil son nulos.
Agrega comprobaciones de nulos antes de acceder a propiedades anidadas.
```

**Refactor:**
```
refactor(database): simplifica el query builder

Extrae patrones de consulta comunes a funciones reutilizables.
Reduce la duplicación de código en la capa de datos.
```

## Analizar los cambios

Revisa qué se está commiteando:

```bash
# Archivos modificados
git status

# Cambios detallados
git diff --staged

# Estadísticas
git diff --staged --stat

# Cambios de un archivo específico
git diff --staged path/to/file
```

## Guía del mensaje

**SÍ:**
- Usa modo imperativo ("agrega función", no "agregada función")
- Mantén la primera línea bajo 50 caracteres
- Capitaliza la primera letra
- Sin punto al final del resumen
- Explica el PORQUÉ, no solo el QUÉ, en el cuerpo

**NO:**
- Mensajes vagos como "update" o "arregla cosas"
- Detalles de implementación técnica en el resumen
- Párrafos en la línea de resumen
- Tiempo pasado
- Trailers de coautoría ni menciones a Claude/IA (ver reglas obligatorias)

## Commits con varios archivos

Cuando commitees varios cambios relacionados:

```
refactor(core): reestructura el módulo de autenticación

- Mueve la lógica de auth de los controladores a la capa de servicio
- Extrae la validación a validadores separados
- Actualiza las pruebas para usar la nueva estructura
- Agrega pruebas de integración del flujo de auth

Breaking change: el servicio de auth ahora requiere un objeto de config
```

## Ejemplos de scope

**Frontend:**
- `feat(ui): agrega spinner de carga al dashboard`
- `fix(form): valida el formato de email`

**Backend:**
- `feat(api): agrega endpoint de perfil de usuario`
- `fix(db): resuelve la fuga del pool de conexiones`

**Infraestructura:**
- `chore(ci): actualiza la versión de Node a 24`
- `feat(docker): agrega build multi-stage`

## Breaking changes

Indica los breaking changes con claridad:

```
feat(api)!: reestructura el formato de respuesta de la API

BREAKING CHANGE: todas las respuestas siguen ahora la spec JSON:API

Formato anterior:
{ "data": {...}, "status": "ok" }

Formato nuevo:
{ "data": {...}, "meta": {...} }

Guía de migración: actualiza el cliente para manejar la nueva estructura
```

## Flujo de trabajo

1. **Revisar cambios**: `git diff --staged`
2. **Identificar tipo**: ¿es feat, fix, refactor, etc.?
3. **Determinar scope**: ¿qué parte del código?
4. **Escribir resumen**: breve, en imperativo y en español
5. **Agregar cuerpo**: explica el porqué y el impacto
6. **Anotar breaking changes**: si aplica

## Staging selectivo

Usa `git add -p` para stagear por partes:

```bash
# Stagear cambios interactivamente
git add -p

# Revisar lo staged
git diff --staged

# Commitear con mensaje
git commit -m "tipo(scope): descripción"
```

## Enmendar commits

Corregir el último mensaje de commit:

```bash
# Solo el mensaje
git commit --amend

# Enmendar y agregar más cambios
git add archivo-olvidado.ts
git commit --amend --no-edit
```

## Buenas prácticas

1. **Commits atómicos** - Un cambio lógico por commit
2. **Probar antes de commitear** - Asegúrate de que el código funcione
3. **Referenciar issues** - Incluye números de issue si aplica
4. **Mantenerlo enfocado** - No mezcles cambios no relacionados
5. **Escribir para humanos** - El yo del futuro lo va a leer

## Checklist del mensaje

- [ ] El mensaje está en español
- [ ] El tipo es apropiado (feat/fix/docs/etc.)
- [ ] El scope es específico y claro
- [ ] El resumen está bajo 50 caracteres
- [ ] El resumen usa modo imperativo
- [ ] El cuerpo explica el PORQUÉ, no solo el QUÉ
- [ ] Los breaking changes están marcados con claridad
- [ ] NO hay trailer de coautoría ni mención a Claude/IA
