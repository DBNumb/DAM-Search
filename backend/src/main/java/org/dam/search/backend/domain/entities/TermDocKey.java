package org.dam.search.backend.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "term_doc", indexes ={
        @Index(name = "term_doc_index", columnList = "term"),
        @Index(name = "doc_index", columnList = "doc_id")
})
@Data
public class TermDocKey implements BaseIdObject<TermDocKeyId> {
    @EmbeddedId
    TermDocKeyId id;

    @Column(nullable = false)
    int termFrecuency;

    @Override
    public TermDocKeyId getId() {
        return id;
    }

    @Override
    public void setId(TermDocKeyId id) {
        this.id = id;
    }
}
