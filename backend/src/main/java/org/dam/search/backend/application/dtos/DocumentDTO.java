package org.dam.search.backend.application.dtos;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class DocumentDTO {

    public Long id;
    public String title;
    public String path;
}
