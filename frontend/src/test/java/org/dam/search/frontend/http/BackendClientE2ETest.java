package org.dam.search.frontend.http;

import org.dam.search.frontend.model.DocumentDTO;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BackendClientE2ETest {

    @Test
    void listDocuments_callsBackendSuccessfully() throws Exception {
        BackendClient client = new BackendClient(URI.create("http://localhost:8080"));
        List<DocumentDTO> docs = client.listDocuments();
        assertNotNull(docs);
    }
}

