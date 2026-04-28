package org.dam.search.backend.application.rest;

import org.dam.search.backend.domain.entities.Document;
import org.dam.search.backend.domain.services.DocumentService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentController extends BaseCrudController<Document, Long>{
    public DocumentController(DocumentService service) {
        super(service);
    }
}
