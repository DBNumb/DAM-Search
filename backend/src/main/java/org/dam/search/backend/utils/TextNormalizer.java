package org.dam.search.backend.utils;


import java.text.Normalizer;

public class TextNormalizer {

    public static String normalizeText(String text) {
        if(text == null){
            return null;
        }
        String normalizedText = text.toLowerCase();
        normalizedText = Normalizer.normalize(normalizedText, Normalizer.Form.NFD)
                .replaceAll("\\p{M}","").replaceAll("\\p{Punct}+","")
                .replaceAll("\\s+", " ").trim();
        return normalizedText;
    }
}
