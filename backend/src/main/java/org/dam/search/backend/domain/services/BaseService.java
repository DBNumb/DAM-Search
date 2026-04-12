package org.dam.search.backend.domain.services;


import org.dam.search.backend.domain.entities.BaseIdObject;
import org.dam.search.backend.domain.repository.BaseRepository;

import java.util.Optional;

public class BaseService<T extends BaseIdObject> {
    BaseRepository<T> repository;

    public BaseService(BaseRepository<T> repository) {
        this.repository = repository;
    }

    public T save(T object) {
        return repository.save(object);
    }

    public T findById(Integer id) {
        Optional<T> optional = repository.findById(id);
        return optional.orElse(null);
    }

    public Iterable<T> findAll() {
        return repository.findAll();
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public T create(T object) {
        if(findById(object.getId()) != null) {
            throw new IllegalArgumentException("Object with ID " + object.getId() + " already exists.");
        }
        return save(object);
    }
}
