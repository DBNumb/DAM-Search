package org.dam.search.backend.domain.repository;

import org.dam.search.backend.domain.entities.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User, Long>{
    Optional<User> findByUsername(String username);
}
