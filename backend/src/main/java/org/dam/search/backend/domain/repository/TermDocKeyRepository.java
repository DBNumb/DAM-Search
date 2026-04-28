package org.dam.search.backend.domain.repository;

import org.dam.search.backend.domain.entities.TermDocKey;
import org.dam.search.backend.domain.entities.TermDocKeyId;
import org.springframework.stereotype.Repository;

@Repository
public interface TermDocKeyRepository extends BaseRepository<TermDocKey, TermDocKeyId> {
}
