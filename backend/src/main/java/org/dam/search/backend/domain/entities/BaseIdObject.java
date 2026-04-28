package org.dam.search.backend.domain.entities;

import jakarta.persistence.Id;
import lombok.Data;



public interface BaseIdObject<T> {
    public T getId();
    public void setId(T id);
}
