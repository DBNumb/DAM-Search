package org.dam.search.backend.application.dtos;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class SearchDTO {
    Long documentId;
    String title;
    double score;
    String snippet;
    int matchIndex;
}
