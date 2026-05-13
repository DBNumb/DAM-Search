package org.dam.search.backend.application.rest;

import org.dam.search.backend.application.dtos.SearchDTO;
import org.dam.search.backend.domain.enums.SearchSelector;
import org.dam.search.backend.domain.services.KMPService;
import org.dam.search.backend.domain.services.TfIdfService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final TfIdfService tfIdfSearchService;
    final KMPService kMPService;

    public SearchController(TfIdfService tfIdfSearchService, KMPService kMPService) {
        this.tfIdfSearchService = tfIdfSearchService;
        this.kMPService = kMPService;
    }

    @GetMapping
    public List<SearchDTO> search(
            @RequestParam("engine") SearchSelector engine,
            @RequestParam("q")  String q,
            @RequestParam(value = "limit", defaultValue = "200") int limit
    ) {
        return switch (engine) {
            case TF_IDF -> tfIdfSearchService.search(q, limit);
            case KMP -> kMPService.search(q, limit);
            default -> throw new IllegalArgumentException("Unsupported search engine: " + engine);
        };
    }
}
