package org.dam.search.backend.domain.services;

import org.dam.search.backend.domain.entities.Document;
import org.dam.search.backend.domain.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class DocumentService extends BaseCRUDService<Document, Long> {

    public DocumentService(DocumentRepository repository) {
        super(repository);
    }
}
