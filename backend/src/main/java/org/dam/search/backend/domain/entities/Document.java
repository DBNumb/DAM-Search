package org.dam.search.backend.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.dam.search.backend.domain.enums.DocumentType;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Document implements BaseIdObject{
    @Id
    Integer id;

    String tittle;

    String content;

    DocumentType type;
}
