package org.dam.search.backend.application.dtos.response;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class DocumentResponse<DocumentDTO> extends BaseResponse<DocumentDTO> {
     public DocumentResponse(DocumentDTO data) {
        super(data);
     }
}
