# Algoritmos implementados

Este documento resume los algoritmos que actualmente usa el proyecto `DAM-Search`.

## 1. Extraccion de texto por tipo de archivo

**Ubicacion:** `backend/src/main/java/org/dam/search/backend/domain/services/DocumentService.java`

- Para `pdf`: usa `PDFBox` (`PDFTextStripper`) para extraer texto plano.
- Para `docx`: usa `Apache POI` (`XWPFWordExtractor`).
- Para extensiones no soportadas: lanza `IllegalArgumentException`.

**Entrada:** `Path` del fichero.
**Salida:** texto bruto (`rawText`).

## 2. Normalizacion de texto

**Ubicacion:** `backend/src/main/java/org/dam/search/backend/utils/TextNormalizer.java`

Pipeline aplicado en `normalizeText`:

1. Convertir a minusculas.
2. Quitar tildes/diacriticos con `Normalizer` (forma `NFD` + regex `\\p{M}`).
3. Eliminar puntuacion (`\\p{Punct}+`).
4. Colapsar espacios (`\\s+`) y hacer `trim()`.

**Objetivo:** que terminos equivalentes tengan una forma canonica.

## 3. Tokenizacion

**Ubicacion:** `backend/src/main/java/org/dam/search/backend/utils/Tokenizer.java`

- Se divide por cualquier separador que no sea letra ni digito:
  - regex de split: `[^\\p{IsAlphabetic}\\p{IsDigit}]+`
- Retorna lista de tokens para indexacion y consulta.

**Nota tecnica:** la condicion actual de filtrado en `Tokenizer` puede descartar mas tokens de los esperados por su logica (`if(!trimmedToken.isBlank() || trimmedToken.length() <= 1) continue;`).

## 4. Hash de contenido (deteccion de cambios)

**Ubicacion:** `DocumentService.sha256`

- Se calcula `SHA-256` sobre el texto normalizado.
- Se guarda como `contentHash` para representar el estado textual del documento.

## 5. Indexacion (indice invertido)

**Ubicacion:** `backend/src/main/java/org/dam/search/backend/domain/services/IndexService.java`

Metodo principal: `upsertAndIndexDocument(ImportedDocument)`.

Pasos:

1. Upsert de `Document` por `path`.
2. Borrado de postings previos del documento (`deleteAllByIdDocumentId`).
3. Conteo de frecuencia de termino en documento (`termFrecuency`) con `HashMap<String, Integer>`.
4. Insercion en `TermDocKey` de pares `(termId, documentId)` con `tf`.
5. Recalculo global de `df` por termino (`recomputeDf`).

### Recalculo de DF

`recomputeDf` construye un mapa `term -> set(documentId)` para contar en cuantos documentos aparece cada termino.

- `df(term) = numero de documentos distintos que contienen term`
- Se persiste en entidad `Term` (`id` y `term` como `String`, `docFrecuency` como `int`).

## 6. Ranking de busqueda (TF-IDF)

**Ubicacion:** `backend/src/main/java/org/dam/search/backend/domain/services/TfIdfService.java`

Metodo principal: `search(String query, int limit)`.

Pasos:

1. Normalizar y tokenizar consulta.
2. Tomar terminos unicos (`Set.copyOf`).
3. Para cada termino:
   - Obtener `df`.
   - Calcular `idf = ln((N + 1) / (df + 1)) + 1`.
   - Recuperar postings (`TermDocKey`) y usar:
     - `weightedTf = 1 + ln(tf)`.
   - Acumular score por documento:
     - `score(doc) += weightedTf * idf`.
4. Ordenar documentos por score descendente.
5. Devolver top `limit` en `SearchDTO`.

Donde:

- `N` es el numero total de documentos.
- `tf` es frecuencia del termino en el documento.
- `df` es frecuencia documental del termino.

## 7. Reindexacion completa

**Ubicacion:** `IndexService.reindexAll()`

- Recorre todos los documentos persistidos.
- Reimporta desde su `path`.
- Reejecuta `upsertAndIndexDocument` para recalcular indice.

## Complejidad aproximada

- **Indexacion de un documento:**
  - Tokenizacion + conteo: `O(T)` donde `T` es numero de tokens.
  - Insercion de postings: `O(U)` donde `U` es numero de terminos unicos.
- **Recompute DF global:**
  - Recorre todos los postings: `O(P)`.
- **Busqueda TF-IDF:**
  - Depende de postings recuperados por termino de consulta: aprox. `O(sum(postings(term)))` + ordenacion final.

## Limitaciones actuales

- No hay stemming ni lematizacion.
- No hay stop-words.
- El snippet en resultados todavia se devuelve vacio (`""`).
- No hay actualizacion incremental de `df`: se recalcula globalmente.

