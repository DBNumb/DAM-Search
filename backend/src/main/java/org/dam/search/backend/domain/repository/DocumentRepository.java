package org.dam.search.backend.domain.repository;

import org.dam.search.backend.domain.entities.Document;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends BaseRepository<Document,Long>{

    Optional<Document> findByPath(String path);
}
