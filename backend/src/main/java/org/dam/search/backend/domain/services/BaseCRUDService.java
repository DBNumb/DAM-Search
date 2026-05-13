package org.dam.search.backend.domain.services;


import org.dam.search.backend.domain.entities.BaseIdObject;
import org.dam.search.backend.domain.repository.BaseRepository;

import java.util.Objects;
import java.util.Optional;

public class BaseCRUDService<T extends BaseIdObject,ID> implements BaseService {
    private final BaseRepository<T,ID> repository;

    public BaseCRUDService(BaseRepository<T,ID> repository) {
        this.repository = repository;
    }

    public T save(T object) {
        if (object == null) {
            throw new IllegalArgumentException("object no puede ser null");
        }
        try {
            return repository.save(object);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Error al guardar la entidad", e);
        }
    }

    public T findById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser null");
        }
        Optional<T> optional = repository.findById(id);
        return optional.orElse(null);
    }

    public Iterable<T> findAll() {
        return repository.findAll();
    }

    public void delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser null");
        }
        try {
            repository.deleteById(id);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Error al eliminar la entidad con id " + id, e);
        }
    }

    public void deleteAll() {
        try {
            repository.deleteAll();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Error al eliminar todas las entidades", e);
        }
    }

    public T create(T object) {
        if (object == null) {
            throw new IllegalArgumentException("object no puede ser null");
        }
        if (object.getId() == null) {
            throw new IllegalArgumentException("object.getId() no puede ser null");
        }

        ID id = castId(object.getId());
        if (repository.existsById(id)) {
            throw new IllegalArgumentException("Object with ID " + id + " already exists.");
        }

        return save(object);
    }

    @SuppressWarnings("unchecked")
    private ID castId(Object rawId) {
        try {
            return (ID) rawId;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Tipo de ID invalido: " + rawId, e);
        }
    }
}
