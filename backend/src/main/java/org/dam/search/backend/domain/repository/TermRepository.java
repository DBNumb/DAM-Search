package org.dam.search.backend.domain.repository;

import org.dam.search.backend.domain.entities.Term;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends BaseRepository<Term, Long> {
}
