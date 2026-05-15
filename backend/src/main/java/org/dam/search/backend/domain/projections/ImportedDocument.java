package org.dam.search.backend.domain.projections;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class ImportedDocument {

   public String title;
   public String path;
   public String contentHash;
   public String rawText;
   public String normalizedText;

}
