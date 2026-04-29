# DAM-Search

Motor de busqueda documental en Java, organizado como proyecto Maven multi-modulo.

## Descripcion

`DAM-Search` separa la solucion en dos modulos principales:

- `backend`: API REST y logica de indexacion/busqueda (Spring Boot + JPA + PostgreSQL).
- `frontend`: cliente de escritorio (JavaFX) que consume la API por HTTP.

## Estructura del repositorio

```text
search-engine/
  pom.xml                  # Proyecto padre (packaging pom)
  backend/                 # API REST + indexacion + ranking
  frontend/                # Cliente JavaFX
  src/                     # Modulo raiz generado
  docs/                    # Documentacion tecnica
```

## Tecnologias

- Maven Wrapper (`mvnw.cmd`)
- Spring Boot `4.0.5`
- Spring Data JPA
- Spring Web MVC
- PostgreSQL
- JavaFX `21.0.6`
- Lombok (backend)

## Requisitos previos

- JDK instalado (el proyecto tiene configuraciones de Java 21/26 segun modulo)
- PostgreSQL en ejecucion

## Configuracion de base de datos

La conexion se define en `backend/src/main/resources/application.yaml`.

Por defecto:

- URL: `jdbc:postgresql://localhost:5432/dam_search`
- Usuario: `postgres`
- Password: `david`

Ajusta esos valores antes de ejecutar en tu entorno.

## Como ejecutar

### 1) Backend

```powershell
Set-Location "F:\School\Final project\search-engine\backend"
.\mvnw.cmd spring-boot:run
```

Si trabajas con Java 21 y no con 26:

```powershell
Set-Location "F:\School\Final project\search-engine\backend"
.\mvnw.cmd "-Djava.version=21" spring-boot:run
```

### 2) Frontend

```powershell
Set-Location "F:\School\Final project\search-engine\frontend"
.\mvnw.cmd javafx:run
```

## Estado funcional actual

- Importacion de documentos PDF y DOCX.
- Normalizacion de texto (`TextNormalizer`) y tokenizacion (`Tokenizer`).
- Indexacion con tabla intermedia termino-documento (`TermDocKey`).
- Recalculo de frecuencia documental (`df`) por termino.
- Busqueda por ranking TF-IDF en `TfIdfService`.
- Operaciones de frontend: listar, importar, reindexar, eliminar y buscar.

## Prueba E2E frontend -> backend

Se incluye un runner simple para validar conexion real desde frontend:

- `frontend/src/test/java/org/dam/search/frontend/http/BackendClientE2ERunner.java`

Ejemplo:

```powershell
Set-Location "F:\School\Final project\search-engine\backend"
.\mvnw.cmd "-Djava.version=21" spring-boot:run "-Dspring-boot.run.arguments=--spring.jpa.hibernate.ddl-auto=update"
```

```powershell
Set-Location "F:\School\Final project\search-engine\frontend"
.\mvnw.cmd -q -DskipTests test-compile
java -cp "target\test-classes;target\classes" org.dam.search.frontend.http.BackendClientE2ERunner
```

Salida esperada:

- `E2E_OK docs=<n>`

## Documentacion de algoritmos

La explicacion de los algoritmos implementados esta en:

- `docs/ALGORITMOS.md`
