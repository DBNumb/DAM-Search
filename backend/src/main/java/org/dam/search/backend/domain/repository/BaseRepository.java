package org.dam.search.backend.domain.repository;

import org.dam.search.backend.domain.entities.BaseIdObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface for entities with an ID.
 * Provides common CRUD operations and ensures that repositories extending this interface
 * are not instantiated directly.
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseIdObject> extends JpaRepository<T, Integer> {
}
