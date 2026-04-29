package org.dam.search.backend.domain.repository;

import org.dam.search.backend.domain.entities.Term;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermRepository extends BaseRepository<Term, String> {
     Optional<Term> findByTerm(String term);
}
