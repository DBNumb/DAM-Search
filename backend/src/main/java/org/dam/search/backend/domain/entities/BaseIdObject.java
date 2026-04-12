package org.dam.search.backend.domain.entities;

import jakarta.persistence.Id;
import lombok.Data;



public interface BaseIdObject {
    public Integer getId();
    public void setId(Integer id);
}
