package org.dam.search.backend.application.rest;

import org.dam.search.backend.domain.entities.BaseIdObject;
import org.dam.search.backend.domain.services.BaseCRUDService;
import org.springframework.http.ResponseEntity;


public class BaseCrudController<T extends BaseIdObject, ID> {
    BaseCRUDService<T, ID> service;

    public BaseCrudController(BaseCRUDService<T, ID> service) {
        this.service = service;
    }

    public ResponseEntity<T> add(T object) {
        T savedObject = service.create(object);
        return ResponseEntity.ok(savedObject);
    }

    public ResponseEntity<T> get(ID id) {
        T object = service.findById(id);
        return ResponseEntity.ok(object);
    }

    public ResponseEntity<T> update(T object) {
        T updatedObject = service.save(object);
        return ResponseEntity.ok(updatedObject);
    }

    public ResponseEntity<T> delete(ID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Iterable<T>> getAll() {
        Iterable<T> objects = service.findAll();
        return ResponseEntity.ok(objects);
    }
}
