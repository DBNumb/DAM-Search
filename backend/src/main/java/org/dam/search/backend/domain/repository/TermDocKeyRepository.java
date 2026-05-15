package org.dam.search.backend.domain.repository;

import org.dam.search.backend.domain.entities.TermDocKey;
import org.dam.search.backend.domain.entities.TermDocKeyId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermDocKeyRepository extends BaseRepository<TermDocKey, TermDocKeyId> {
    @Query("select p from TermDocKey p where p.id.termId = :term")
    List<TermDocKey> findAllByTerm(String term);

    void deleteAllByIdDocumentId(Long id);
}
