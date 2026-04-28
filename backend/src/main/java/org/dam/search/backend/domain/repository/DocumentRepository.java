package org.dam.search.backend.domain.repository;

import org.dam.search.backend.domain.entities.Document;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends BaseRepository<Document,Long>{

}
