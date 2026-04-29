package org.dam.search.frontend.model;

public class SearchResultDTO {
    private long documentId;
    private String title;
    private double score;
    private String snippet;
    private int matchIndex;

    public SearchResultDTO() {
    }

    public SearchResultDTO(long documentId, String title, double score, String snippet, int matchIndex) {
        this.documentId = documentId;
        this.title = title;
        this.score = score;
        this.snippet = snippet;
        this.matchIndex = matchIndex;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(int matchIndex) {
        this.matchIndex = matchIndex;
    }
}
