package org.dam.search.frontend.http;

import org.dam.search.frontend.model.DocumentDTO;
import org.dam.search.frontend.model.SearchResultDTO;
import org.dam.search.frontend.model.SearchSelector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BackendClient {
    private final HttpClient http;

    private final URI baseUri;

    public BackendClient(URI baseUri) {
        this.http = HttpClient.newHttpClient();
        this.baseUri = baseUri;
    }

    public List<DocumentDTO> listDocuments() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(baseUri.resolve("/api/documents/all")).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensureOk(res);
        return Json.parseDocuments(res.body());
    }

    public String getDocumentContent(long id) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(baseUri.resolve("/api/documents/" + id)).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensureOk(res);
        return Json.extractContent(res.body());
    }

    public void deleteDocument(long id) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(baseUri.resolve("/api/documents/" + id)).DELETE().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensureOk(res);
    }

    public void reindexAll() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(baseUri.resolve("/api/documents/reindex"))
                                     .POST(HttpRequest.BodyPublishers.noBody())
                                     .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensureOk(res);
    }

    public void importFile(Path file) throws IOException, InterruptedException {
        String boundary = "----JavaFXBoundary" + System.currentTimeMillis();
        byte[] body = multipartFileBody(boundary, "file", file);

        HttpRequest req = HttpRequest.newBuilder(baseUri.resolve("/api/documents/import"))
                                     .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                                     .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                                     .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensureOk(res);
    }

    public List<SearchResultDTO> search(SearchSelector engine, String q, int limit) throws IOException, InterruptedException {
        String uri = "/api/search?engine=" + engine.name()
                + "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8)
                + "&limit=" + limit;
        HttpRequest req = HttpRequest.newBuilder(baseUri.resolve(uri)).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensureOk(res);
        return Json.parseResults(res.body());
    }

    private static void ensureOk(HttpResponse<?> res) throws IOException {
        int sc = res.statusCode();
        if (sc >= 200 && sc < 300) return;
        throw new IOException("Backend respondió " + sc);
    }

    private static byte[] multipartFileBody(String boundary, String fieldName, Path file) throws IOException {
        String filename = file.getFileName().toString();
        String partHeader = ""
                + "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n";
        String end = "\r\n--" + boundary + "--\r\n";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(partHeader.getBytes(StandardCharsets.UTF_8));
        baos.write(Files.readAllBytes(file));
        baos.write(end.getBytes(StandardCharsets.UTF_8));
        return baos.toByteArray();
    }

    public boolean login(String username, String password) throws IOException, InterruptedException {
        String body = Json.loginRequestBody(username, password);

        HttpRequest req = HttpRequest.newBuilder(baseUri.resolve("/api/user/login"))
                                     .header("Content-Type", "application/json")
                                     .POST(HttpRequest.BodyPublishers.ofString(body))
                                     .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200 || res.statusCode() == 401) {
            return Json.extractBooleanData(res.body());
        }
        throw new IOException("Backend respondió " + res.statusCode());
    }

    public boolean register(String username, String password) throws IOException, InterruptedException {
        String body = Json.userCreateBody(username, password);

        HttpRequest req = HttpRequest.newBuilder(baseUri.resolve("/api/user/"))
                                     .header("Content-Type", "application/json")
                                     .POST(HttpRequest.BodyPublishers.ofString(body))
                                     .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());


        if (res.statusCode() == 200 || res.statusCode() == 201) {
            return true;
        }


        if (res.statusCode() == 400 || res.statusCode() == 409) {
            return false;
        }

        throw new IOException("Backend respondió " + res.statusCode());
    }
}
