package org.dam.search.backend.domain.services;

import org.dam.search.backend.application.dtos.SearchDTO;
import org.dam.search.backend.domain.entities.Document;
import org.dam.search.backend.domain.repository.DocumentRepository;
import org.dam.search.backend.utils.TextNormalizer;
import org.springframework.data.web.OffsetScrollPositionArgumentResolver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.dam.search.backend.utils.SnippetUtils.buildSnippet;

@Service
public class KMPService implements BaseService {
    final DocumentRepository documentRepository;
    private final OffsetScrollPositionArgumentResolver offsetScrollPositionArgumentResolver;

    public KMPService(DocumentRepository documentRepository, OffsetScrollPositionArgumentResolver offsetScrollPositionArgumentResolver) {
        this.documentRepository = documentRepository;
        this.offsetScrollPositionArgumentResolver = offsetScrollPositionArgumentResolver;
    }

    public List<SearchDTO> search(String query, int limit) {

        String queryNorm = TextNormalizer.normalizeText(query);
        if (queryNorm.isBlank()) return List.of();

        List<SearchDTO> result = new ArrayList<>();

        for (Document doc : documentRepository.findAll()) {
            String contentNorm = TextNormalizer.normalizeText(doc.getContent());
            List<Integer> matches = searchAll(contentNorm, queryNorm);
            if (matches.isEmpty()) continue;
            int firstMatchIndex = matches.get(0);
            int score = matches.size();
            String snippet = buildSnippet(contentNorm, firstMatchIndex, 180);
            result.add(SearchDTO.builder()
                                .documentId(doc.getId())
                                .title(doc.getTitle())
                                .score((double) matches.size())
                                .snippet(snippet)
                                .matchIndex(firstMatchIndex)
                                .build());
        }
        result.sort(Comparator.comparing(SearchDTO::getScore).reversed()
        .thenComparing(SearchDTO::getTitle, String.CASE_INSENSITIVE_ORDER));
        if(limit > 0 && result.size() > limit) {
            return result.subList(0, limit);
        }


        return result;
    }

    private List<Integer> searchAll(String contentNorm, String queryNorm) {
        List<Integer> positions = new ArrayList<>();
        if (contentNorm.isBlank()) return positions;

        int longestPrefixSuffix[] = buildLPS(queryNorm);

        int textIndex = 0;
        int patternIndex = 0;

        while (textIndex < contentNorm.length()) {
            if (contentNorm.charAt(textIndex) == queryNorm.charAt(patternIndex)) {
                textIndex++;
                patternIndex++;
                if (patternIndex == queryNorm.length()) {
                    positions.add(textIndex - patternIndex);
                    patternIndex = longestPrefixSuffix[patternIndex - 1];
                }

            } else {
                if (patternIndex != 0) {
                    patternIndex = longestPrefixSuffix[patternIndex - 1];
                }
                else{
                    textIndex++;
                }
            }


        }

        return positions;
    }

    public int[] buildLPS(String pattern) {
        int[] longestPrefixSuffix = new int[pattern.length()];
        int prefixLength = 0;
        int currentIndex = 1;

        while (currentIndex < pattern.length()) {
            if (pattern.charAt(currentIndex) == pattern.charAt(prefixLength)) {
                prefixLength++;
                longestPrefixSuffix[currentIndex] = prefixLength;
                currentIndex++;
            } else if (prefixLength != 0) {
                prefixLength = longestPrefixSuffix[prefixLength - 1];
            } else {
                longestPrefixSuffix[currentIndex] = 0;
                currentIndex++;
            }
        }
        return longestPrefixSuffix;
    }

}
