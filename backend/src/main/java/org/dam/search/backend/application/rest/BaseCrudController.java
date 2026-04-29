package org.dam.search.backend.application.rest;

import org.dam.search.backend.domain.entities.BaseIdObject;
import org.dam.search.backend.domain.services.BaseCRUDService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


public class BaseCrudController<T extends BaseIdObject, ID> {
    BaseCRUDService<T, ID> service;

    public BaseCrudController(BaseCRUDService<T, ID> service) {
        this.service = service;
    }

    @PostMapping("/")
    public ResponseEntity<T> add(@RequestBody T object) {
        T savedObject = service.create(object);
        return ResponseEntity.ok(savedObject);
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> get(@PathVariable ID id) {
        T object = service.findById(id);
        return ResponseEntity.ok(object);
    }

    @PutMapping("/")
    public ResponseEntity<T> update(@RequestBody T object) {
        T updatedObject = service.save(object);
        return ResponseEntity.ok(updatedObject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<T> delete(@PathVariable ID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<T>> getAll() {
        Iterable<T> objects = service.findAll();
        return ResponseEntity.ok(objects);
    }
}
