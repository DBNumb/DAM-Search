package org.dam.search.backend.utils;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    public static List<String> tokenize(String normalizedText){
        if(normalizedText == null || normalizedText.isBlank()){
            return List.of();
        }
        String[] tokens = normalizedText.split("[^\\p{IsAlphabetic}\\p{IsDigit}]+");
        List<String> tokenizedText = new ArrayList<>();
        for(String token : tokens){
            if(token == null){
                continue;
            }
            String trimmedToken = token.trim();
            if(trimmedToken.isBlank() || trimmedToken.length() <= 1){
                continue;
            }
            tokenizedText.add(trimmedToken);
        }
        return tokenizedText;
    }
}
