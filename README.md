# DAM-Search

Motor de busqueda documental en Java, organizado como proyecto Maven multi-modulo.

## Descripcion

DAM-Search separa la solucion en dos modulos:

- `backend`: API REST y logica de negocio (Spring Boot + JPA + PostgreSQL).
- `frontend`: cliente de escritorio (JavaFX).

## Arquitectura

Flujo general:

1. El usuario interactua con el frontend.
2. El frontend envia peticiones HTTP al backend.
3. El backend gestiona documentos y persiste datos en PostgreSQL.
4. El backend aplica preprocesado de texto (normalizacion y tokenizacion).
5. Como evolucion, se incorpora ranking por relevancia (TF-IDF).

## Estructura del repositorio

```text
search-engine/
  pom.xml                  # Proyecto padre (packaging pom)
  backend/                 # Spring Boot backend
  frontend/                # JavaFX frontend
  src/                     # Modulo raiz generado (base)
```

## Tecnologias

- Java 26
- Maven
- Spring Boot 4.0.5
- Spring Data JPA
- Spring Web MVC
- PostgreSQL
- JavaFX 21.0.6
- Lombok

## Requisitos previos

- JDK 26 instalado
- Maven (o usar `mvnw`)
- PostgreSQL en ejecucion

## Configuracion de base de datos

El backend usa `backend/src/main/resources/application.yaml` para la conexion a PostgreSQL.
Ajusta URL/usuario/password segun tu entorno local antes de ejecutar.

## Ejecucion

### 1) Backend

```powershell
cd "F:\School\Final project\search-engine\backend"
.\mvnw.cmd spring-boot:run
```

### 2) Frontend

```powershell
cd "F:\School\Final project\search-engine\frontend"
.\mvnw.cmd javafx:run
```

## Estado actual

- Backend con base CRUD generica para `Document`.
- Utilidades de texto: `TextNormalizer` y `Tokenizer`.
- Servicio `TfIdfService` creado como base para evolucion.
- Frontend JavaFX en version inicial.

## Siguientes pasos sugeridos

- Completar endpoints REST especificos de busqueda.
- Implementar TF-IDF y ordenacion por relevancia.
- Conectar frontend con backend para flujo completo.
- Ampliar pruebas unitarias y de integracion.
