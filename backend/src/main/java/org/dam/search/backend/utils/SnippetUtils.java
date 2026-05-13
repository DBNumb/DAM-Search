package org.dam.search.backend.utils;

public final class SnippetUtils {

    public static String buildSnippet(String content, int matchIndex, int maxLen) {
        if (content == null || content.isBlank()) return "";
        if (content.length() <= maxLen) return content;

        int start;
        if (matchIndex < 0) {
            start = 0;
        } else {
            start = Math.max(0, matchIndex - (maxLen / 3));
        }
        int end = Math.min(content.length(), start + maxLen);

        String prefix = start > 0 ? "..." : "";
        String suffix = end < content.length() ? "..." : "";
        return prefix + content.substring(start, end).trim() + suffix;
    }
}

