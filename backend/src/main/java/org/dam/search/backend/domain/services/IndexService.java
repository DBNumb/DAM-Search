package org.dam.search.backend.domain.services;

import org.dam.search.backend.domain.entities.Document;
import org.dam.search.backend.domain.entities.Term;
import org.dam.search.backend.domain.entities.TermDocKey;
import org.dam.search.backend.domain.entities.TermDocKeyId;
import org.dam.search.backend.domain.projections.ImportedDocument;
import org.dam.search.backend.domain.repository.DocumentRepository;
import org.dam.search.backend.domain.repository.TermDocKeyRepository;
import org.dam.search.backend.domain.repository.TermRepository;
import org.dam.search.backend.utils.TextNormalizer;
import org.dam.search.backend.utils.Tokenizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class IndexService {
    private final DocumentRepository documentRepository;
    private final TermRepository termRepository;
    private final TermDocKeyRepository termDocKeyRepository;
    private final DocumentService documentService;
    public IndexService(DocumentRepository documentRepository,
                        TermRepository termRepository,
                        TermDocKeyRepository termDocKeyRepository,
                        DocumentService documentService) {
        this.documentRepository = documentRepository;
        this.termRepository = termRepository;
        this.termDocKeyRepository = termDocKeyRepository;
        this.documentService = documentService;
    }

    @Transactional
    public Document upsertAndIndexDocument(ImportedDocument document) {
        Document entity = documentRepository.findByPath(document.getPath()).orElseGet(Document::new);
        entity.setTitle(document.getTitle());
        entity.setPath(document.getPath());
        entity.setContent(document.getRawText());
        entity.setContentHash(document.getContentHash());

        var saved = documentRepository.save(entity);
        termDocKeyRepository.deleteAllByIdDocumentId(saved.getId());

        Map<String, Integer> termFrecuency = new HashMap<>();
        for(String tok : Tokenizer.tokenize(document.getNormalizedText())) {
            termFrecuency.merge(tok, 1,Integer::sum);
        }

        for(var e : termFrecuency.entrySet()) {
            termDocKeyRepository.save(
                    TermDocKey.builder()
                            .id(new TermDocKeyId(e.getKey(), saved.getId()))
                            .termFrecuency(e.getValue())
                            .build()
            );
        }
        recomputeDf();
        return saved;
    }

    private void recomputeDf() {
        termRepository.deleteAll();
        Map<String, HashSet<Long>> seen = new HashMap<>();
        for (TermDocKey tdk : termDocKeyRepository.findAll()) {
            seen.computeIfAbsent(tdk.getId().getTermId(), k -> new HashSet<>()).add(tdk.getId().getDocumentId());
        }
        for (var e : seen.entrySet()) {
            termRepository.save(Term.builder().id(e.getKey()).term(e.getKey()).docFrecuency(e.getValue().size()).build());
        }
    }

    public int countDocuments() {
        return (int) documentRepository.count();
    }

    public int getDfForTerm(String term) {
        return termRepository.findByTerm(term).map(Term::getDocFrecuency).orElse(0);
    }

    public List<TermDocKey> getTermDocKeyForTerm(String term) {
        return termDocKeyRepository.findAllByTerm(term);
    }

    public Optional<Document> findDocumentById(long id) {
        return documentRepository.findById(id);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Transactional
    public void reindexAll() {
        List<Document> docs = documentRepository.findAll();
        for (Document d : docs) {
            ImportedDocument imported = ImportedDocument.builder()
                    .title(d.getTitle())
                    .path(d.getPath())
                    .contentHash(d.getContentHash())
                    .rawText(d.getContent())
                    .normalizedText(TextNormalizer.normalizeText(d.getContent()))
                    .build();
            upsertAndIndexDocument(imported);
        }
    }
}