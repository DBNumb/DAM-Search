package org.dam.search.backend.application.rest;

import org.antlr.v4.runtime.misc.NotNull;
import org.dam.search.backend.application.dtos.DocumentDTO;
import org.dam.search.backend.application.dtos.response.DocumentResponse;
import org.dam.search.backend.domain.entities.Document;
import org.dam.search.backend.domain.services.DocumentService;
import org.dam.search.backend.domain.services.IndexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/documents")
public class DocumentController extends BaseCrudController<Document, Long> {
    private final DocumentService service;
    private final IndexService indexService;

    public DocumentController(DocumentService service, IndexService indexService) {
        super(service);
        this.service = service;
        this.indexService = indexService;
    }

    @PostMapping("/import")
    public ResponseEntity<DocumentResponse> importDocuments(@RequestParam("file") MultipartFile file) throws IOException {
        Path tempDir = Files.createTempDirectory("documents");
        try {
            Path tmp = tempDir.resolve(Path.of(file.getOriginalFilename() == null ?
                                               "upload" :
                                               file.getOriginalFilename()).getFileName());
            file.transferTo(tmp);
            var imported = service.importDocument(tmp);
            Document saved = indexService.upsertAndIndexDocument(imported);
            return ResponseEntity.ok(DocumentResponse
                    .builder()
                    .data(DocumentDTO.builder()
                                     .id(saved.getId())
                                     .title(saved.getTitle())
                                     .path(saved.getPath())
                                     .build())
                    .build()
            );
        } finally {
            try (Stream<Path> paths = Files.walk(tempDir)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }
    @PostMapping("/reindex")
    public void reindexAll() {
        indexService.reindexAll();
    }
}
