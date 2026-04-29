package org.dam.search.backend.domain.services;

import org.dam.search.backend.application.dtos.SearchDTO;
import org.dam.search.backend.domain.entities.TermDocKey;
import org.dam.search.backend.utils.TextNormalizer;
import org.dam.search.backend.utils.Tokenizer;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TfIdfService {

    private final IndexService indexService;

    public TfIdfService(IndexService indexService) {
        this.indexService = indexService;
    }

    public List<SearchDTO> search(String query, int limit) {
        String qNorm = TextNormalizer.normalizeText(query);
        List<String> qTerms = Tokenizer.tokenize(qNorm);

        if (qTerms.isEmpty()) return List.of();

        int countDocs = indexService.countDocuments();

        if (countDocs == 0) return List.of();

        Set<String> uniqueTerms = Set.copyOf(qTerms);

        Map<Long, Double> tfIdfScores = new HashMap<>();

        for (String term : uniqueTerms) {
            int df = indexService.getDfForTerm(term);
            double idf = Math.log((countDocs + 1.0) / (df + 1.0)) + 1.0;
            List<TermDocKey> termDocKeys = indexService.getTermDocKeyForTerm(term);

            for (TermDocKey termDocKey : termDocKeys) {
                int termFrecuency = termDocKey.getTermFrecuency();
                double weightedTermFrequency = 1.0 + Math.log(termFrecuency);
                tfIdfScores.merge(termDocKey.getId().getDocumentId(), weightedTermFrequency * idf, Double::sum);
            }
        }
        List<Map.Entry<Long, Double>> rankedResults = new ArrayList<>(tfIdfScores.entrySet());
        rankedResults.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<SearchDTO> results = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, rankedResults.size()); i++) {
            long docId = rankedResults.get(i).getKey();
            double score = rankedResults.get(i).getValue();
            indexService.findDocumentById(docId).ifPresent(document -> {
                results.add(SearchDTO.builder()
                                     .documentId(document.getId())
                                     .title(document.getTitle())
                                     .score(score)
                                     .snippet("")
                                     .build());
            });
        }
        return results;
    }
}
