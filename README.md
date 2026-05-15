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

### Backend
- **Maven**: Wrapper (`mvnw.cmd`)
- **Spring Boot**: `4.0.5`
  - Spring Data JPA
  - Spring Web MVC
  - Spring Security
  - Spring DevTools
- **Base de datos**: PostgreSQL
- **Procesamiento de documentos**:
  - Apache PDFBox `3.0.4` (extracción de PDF)
  - Apache POI `5.4.1` (extracción de DOCX)
- **Utilidades**: Lombok

### Frontend
- **JavaFX**: `21.0.6`
- **AtlantaFX**: `2.1.0` (tema moderno)
- **Testing**: JUnit `5.12.1`

### Versiones Java
- **Backend**: Java `26`
- **Frontend**: Java `21`
- **Proyecto raíz**: Java `26`

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

### 1) Backend (Java 26)

```powershell
cd "F:\School\Final project\search-engine\backend"
.\mvnw.cmd spring-boot:run
```

Si necesitas usar Java 21 en lugar de 26:

```powershell
cd "F:\School\Final project\search-engine\backend"
.\mvnw.cmd "-Djava.version=21" spring-boot:run
```

Para ejecutar con inicialización de base de datos:

```powershell
cd "F:\School\Final project\search-engine\backend"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.jpa.hibernate.ddl-auto=update"
```

### 2) Frontend (Java 21)

```powershell
cd "F:\School\Final project\search-engine\frontend"
.\mvnw.cmd javafx:run
```

## Estado funcional actual

- Importación de documentos PDF y DOCX con extracción automática de texto.
- Normalización de texto (`TextNormalizer`) y tokenización (`Tokenizer`).
- Indexación con tabla intermedia término-documento (`TermDocKey`).
- Recálculo de frecuencia documental (`df`) por término.
- Búsqueda por ranking TF-IDF en `TfIdfService`.
- Operaciones del frontend: listar, importar, reindexar, eliminar y buscar.
- Seguridad: Spring Security integrado en backend.
- Interfaz moderna con tema AtlantaFX en frontend.

## Prueba E2E frontend -> backend

Se incluye un runner simple para validar conexión real desde frontend:

- `frontend/src/test/java/org/dam/search/frontend/http/BackendClientE2ERunner.java`

### Ejecución con Java 21

**Terminal 1 - Backend:**

```powershell
cd "F:\School\Final project\search-engine\backend"
.\mvnw.cmd "-Djava.version=21" spring-boot:run "-Dspring-boot.run.arguments=--spring.jpa.hibernate.ddl-auto=update"
```

**Terminal 2 - Frontend E2E:**

```powershell
cd "F:\School\Final project\search-engine\frontend"
.\mvnw.cmd -q -DskipTests test-compile
java -cp "target\test-classes;target\classes" org.dam.search.frontend.http.BackendClientE2ERunner
```

**Salida esperada:**

```
E2E_OK docs=<n>
```

## Documentacion de algoritmos

La explicacion de los algoritmos implementados esta en:

- `docs/ALGORITMOS.md`
