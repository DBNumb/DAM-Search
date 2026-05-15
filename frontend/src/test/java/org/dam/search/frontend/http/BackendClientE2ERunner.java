package org.dam.search.frontend.http;

import org.dam.search.frontend.model.DocumentDTO;

import java.net.URI;
import java.util.List;

public class BackendClientE2ERunner {
    public static void main(String[] args) throws Exception {
        BackendClient client = new BackendClient(URI.create("http://localhost:8080"));
        List<DocumentDTO> docs = client.listDocuments();
        System.out.println("E2E_OK docs=" + docs.size());
    }
}

