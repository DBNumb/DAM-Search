package org.dam.search.frontend.http;

import org.dam.search.frontend.model.DocumentDTO;
import org.dam.search.frontend.model.SearchResultDTO;

import java.util.ArrayList;
import java.util.List;

public final class Json {
    private Json() {}

    public static List<DocumentDTO> parseDocuments(String json) {
        List<DocumentDTO> out = new ArrayList<>();
        for (String obj : splitObjects(json)) {
            long id = Long.parseLong(extractNumber(obj, "id"));
            String path = extractString(obj, "path");
            String title = extractString(obj, "title");
            out.add(new DocumentDTO(id, title, path));
        }
        return out;
    }

    public static List<SearchResultDTO> parseResults(String json) {
        List<SearchResultDTO> out = new ArrayList<>();
        for (String obj : splitObjects(json)) {
            long docId = Long.parseLong(extractNumber(obj, "documentId"));
            String title = extractString(obj, "title");
            double score = Double.parseDouble(extractNumber(obj, "score"));
            String snippet = extractString(obj, "snippet");
            int matchIndex = Integer.parseInt(extractNumber(obj, "matchIndex"));
            out.add(new SearchResultDTO(docId, title, score, snippet, matchIndex));
        }
        return out;
    }

    public static String extractContent(String jsonObj) {
        return extractString(jsonObj, "content");
    }

    public static String loginRequestBody(String username, String password) {
        return "{\"username\":\"" + escapeJson(username) + "\",\"password\":\"" + escapeJson(password) + "\"}";
    }

    public static String userCreateBody(String username, String password) {
        return "{\"username\":\"" + escapeJson(username) + "\",\"password\":\"" + escapeJson(password) + "\"}";
    }

    public static boolean extractBooleanData(String jsonObj) {
        String needle = "\"data\":";
        int i = jsonObj.indexOf(needle);
        if (i < 0) return false;
        i += needle.length();
        while (i < jsonObj.length() && Character.isWhitespace(jsonObj.charAt(i))) i++;
        if (jsonObj.startsWith("true", i)) return true;
        if (jsonObj.startsWith("false", i)) return false;
        return false;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    private static List<String> splitObjects(String jsonArray) {
        String s = jsonArray == null ? "" : jsonArray.trim();
        if (s.length() < 2) return List.of();
        if (s.charAt(0) != '[') return List.of(s);
        if (s.equals("[]")) return List.of();
        List<String> objs = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inStr = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inStr = !inStr;
            if (inStr) continue;
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objs.add(s.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objs;
    }

    private static String extractString(String obj, String key) {
        String needle = "\"" + key + "\":";
        int i = obj.indexOf(needle);
        if (i < 0) return "";
        i += needle.length();
        while (i < obj.length() && Character.isWhitespace(obj.charAt(i))) i++;
        if (i >= obj.length() || obj.charAt(i) != '"') return "";
        i++;
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (; i < obj.length(); i++) {
            char c = obj.charAt(i);
            if (esc) {
                sb.append(switch (c) {
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case '"' -> '"';
                    case '\\' -> '\\';
                    default -> c;
                });
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String extractNumber(String obj, String key) {
        String needle = "\"" + key + "\":";
        int i = obj.indexOf(needle);
        if (i < 0) return "0";
        i += needle.length();
        while (i < obj.length() && Character.isWhitespace(obj.charAt(i))) i++;
        int j = i;
        while (j < obj.length()) {
            char c = obj.charAt(j);
            if ((c >= '0' && c <= '9') || c == '.' || c == '-' || c == '+') j++;
            else break;
        }
        return obj.substring(i, j);
    }
}