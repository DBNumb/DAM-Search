package org.dam.search.backend.domain.enums;

public enum SearchSelector {
    TF_IDF("TF-IDF (relevancia)"),
    KMP("KMP (exacto)");

    private final String label;

    SearchSelector(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
