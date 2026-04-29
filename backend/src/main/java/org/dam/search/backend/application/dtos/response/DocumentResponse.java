package org.dam.search.backend.application.dtos.response;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class DocumentResponse<DocumentDTO> extends BaseResponse<DocumentDTO> {
     public DocumentResponse(DocumentDTO data) {
        super(data);
     }
}
