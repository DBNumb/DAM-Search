package org.dam.search.backend.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Document implements BaseIdObject<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    String path;

    @Column(nullable = false, length = 512)
    String title;

    @Column(nullable = false, name = "content_hash", length = 64)
    String contentHash;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(nullable = false, name = "added_at")
    Instant addedAt;

    @Column(nullable = false, name = "updated_at")
    Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if(addedAt == null) addedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdated() {
        updatedAt = Instant.now();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
