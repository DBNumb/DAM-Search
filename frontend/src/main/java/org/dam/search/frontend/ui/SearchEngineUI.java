package org.dam.search.frontend.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.dam.search.frontend.http.BackendClient;
import org.dam.search.frontend.model.DocumentDTO;
import org.dam.search.frontend.model.SearchResultDTO;
import org.dam.search.frontend.model.SearchSelector;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SearchEngineUI {
    @FXML private Label statusLabel;
    @FXML private Label footerLabel;
    @FXML private TextField queryField;
    @FXML private ComboBox<SearchSelector> engineCombo;

    @FXML private Button button;

    @FXML private ListView<DocumentDTO> docsList;
    @FXML private ListView<SearchResultDTO> resultsList;
    @FXML private SplitPane mainContentSplit;
    @FXML private VBox matchesPane;
    @FXML private Label matchesTitleLabel;
    @FXML private ListView<String> matchesListView;
    @FXML private TextArea matchesTextArea;
    @FXML private Button searchButton;
    @FXML private ProgressIndicator progress;
    @FXML private TextArea previewArea;

    private final BackendClient client = new BackendClient(URI.create("http://localhost:8080"));

    @FXML
    protected void initialize() {
        engineCombo.setItems(FXCollections.observableArrayList(SearchSelector.values()));
        engineCombo.getSelectionModel().selectFirst();

        previewArea.setEditable(false);
        previewArea.setFocusTraversable(false);
        matchesTextArea.setEditable(false);
        matchesTextArea.setFocusTraversable(false);

        docsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DocumentDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(null);
                setGraphic(documentCard(item.getTitle(), item.getPath(), fileKind(item.getTitle(), item.getPath())));
            }
        });

        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SearchResultDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(null);
                setGraphic(resultCard(item));
            }
        });

        docsList.getSelectionModel().selectedItemProperty().addListener((obs, oldDoc, newDoc) -> {
            if (newDoc == null || newDoc.getId() == null) {
                return;
            }
            loadPreview(newDoc.getId());
        });

        resultsList.getSelectionModel().selectedItemProperty().addListener((obs, oldResult, newResult) -> {
            if (newResult == null) {
                return;
            }
            loadPreview(newResult.getDocumentId());
        });
    }

    @FXML
    public void onBackFromMatches() {
        matchesPane.setVisible(false);
        matchesPane.setManaged(false);
        mainContentSplit.setVisible(true);
        mainContentSplit.setManaged(true);
    }

    @FXML
    public void onLoadPdf() {
        Stage stage = (Stage) queryField.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecciona PDFs");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null || files.isEmpty()) return;

        runBackground("Subiendo e indexando...", () -> {
            for (File f : files) client.importFile(f.toPath());
        }, () -> {
            refreshDocuments();
            setStatus("Indexación completa.", false);
        });
    }

    @FXML
    public void onLoadDocx() {
        Stage stage = (Stage) queryField.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecciona DOCX");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Word (.docx)", "*.docx")
        );
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null || files.isEmpty()) return;

        runBackground("Subiendo e indexando...", () -> {
            for (File f : files) client.importFile(f.toPath());
        }, () -> {
            refreshDocuments();
            setStatus("Indexación completa.", false);
        });
    }

    @FXML
    public void onReindexAll() {
        runBackground("Reindexando...", () -> client.reindexAll(), () -> {
            refreshDocuments();
            setStatus("Reindexación completa.", false);
        });
    }

    @FXML
    public void onRemoveSelected() {
        DocumentDTO selected = docsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quitar documento");
        confirm.setHeaderText("¿Quitar del índice?");
        confirm.setContentText(selected.getTitle());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        runBackground("Quitando...", () -> client.deleteDocument(selected.getId()), () -> {
            refreshDocuments();
            setStatus("Documento quitado.", false);
        });
    }

    @FXML
    public void onSearch() {
        String q = queryField.getText();
        if (q == null || q.isBlank()) return;

        SearchSelector engine = engineCombo.getSelectionModel().getSelectedItem();
        runBackground("Buscando...", () -> {
            long t0 = System.nanoTime();
            List<SearchResultDTO> results = client.search(engine, q, 200);
            long ms = (System.nanoTime() - t0) / 1_000_000;
            Platform.runLater(() -> {
                resultsList.setItems(FXCollections.observableArrayList(results));
                footerLabel.setText(results.size() + " resultados encontrados en " + ms + " ms");
            });
        }, () -> setStatus("Búsqueda lista.", false));
    }


    private static Node documentCard(String title, String subtitle, FileKind kind) {
        Node icon = fileIcon(kind);
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        Label s = new Label(shorten(subtitle, 52));
        s.getStyleClass().add("card-subtitle");
        VBox box = new VBox(2, t, s);
        HBox row = new HBox(10, icon, box);
        row.getStyleClass().add("card");
        row.setPadding(new Insets(10));
        return row;
    }
    private Node resultCard(SearchResultDTO result) {
        String title = result.getTitle();
        String snippet = result.getSnippet();
        double score = result.getScore();
        FileKind kind = fileKind(title, null);

        Node icon = fileIcon(kind);
        Label t = new Label(title);
        t.getStyleClass().add("card-title");

        String shownSnippet = (snippet == null || snippet.isBlank())
                ? "Sin fragmento disponible. Selecciona el resultado para abrir la vista previa."
                : snippet;

        TextFlow sn = buildSnippetFlow(shorten(shownSnippet, 340), queryField.getText());

        Label sc = new Label(String.format("%.4f", score));
        sc.getStyleClass().add("badge");

        Hyperlink matchesLink = new Hyperlink("Ver coincidencias");
        matchesLink.getStyleClass().add("link-lite");
        matchesLink.setOnAction(e -> showMatchesWindow(result));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, t, spacer, sc);
        HBox actions = new HBox(matchesLink);
        VBox text = new VBox(6, top, sn, actions);

        HBox row = new HBox(10, icon, text);
        row.getStyleClass().add("card");
        row.setPadding(new Insets(10));
        return row;
    }

    private static String shorten(String snippet, int max) {
        if(snippet == null) return "";
        String trimmed = snippet.replaceAll("\\s+"," ").trim();
        if(trimmed.length() <= max) return trimmed;
        return trimmed.substring(0, max-1) + "...";
    }

    private static TextFlow buildSnippetFlow(String snippet, String query) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("card-snippet-flow");

        if (snippet == null || snippet.isBlank()) {
            return flow;
        }

        List<String> terms = extractTerms(query);
        if (terms.isEmpty()) {
            Text plain = new Text(snippet);
            plain.getStyleClass().add("card-snippet");
            flow.getChildren().add(plain);
            return flow;
        }

        String regex = "(?i)(" + String.join("|", terms.stream().map(Pattern::quote).toList()) + ")";
        Matcher matcher = Pattern.compile(regex).matcher(snippet);

        int last = 0;
        while (matcher.find()) {
            if (matcher.start() > last) {
                Text plain = new Text(snippet.substring(last, matcher.start()));
                plain.getStyleClass().add("card-snippet");
                flow.getChildren().add(plain);
            }

            Text hit = new Text(snippet.substring(matcher.start(), matcher.end()));
            hit.getStyleClass().add("snippet-hit");
            flow.getChildren().add(hit);
            last = matcher.end();
        }

        if (last < snippet.length()) {
            Text plain = new Text(snippet.substring(last));
            plain.getStyleClass().add("card-snippet");
            flow.getChildren().add(plain);
        }
        return flow;
    }

    private static List<String> extractTerms(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String[] raw = query.toLowerCase().trim().split("\\s+");
        Set<String> terms = new LinkedHashSet<>();
        for (String t : raw) {
            if (t != null && !t.isBlank() && t.length() > 1) {
                terms.add(t);
            }
        }
        return new ArrayList<>(terms);
    }

    private void showMatchesWindow(SearchResultDTO result) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return client.getDocumentContent(result.getDocumentId());
            }
        };

        task.setOnSucceeded(e -> {
            String content = task.getValue();
            List<String> contexts = buildMatchContexts(content, extractTerms(queryField.getText()), 140, 80);
            showMatchesInSameView(result.getTitle(), content, contexts);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String msg = ex == null ? "No se pudieron cargar coincidencias" : ex.getMessage();
            setStatus("Error: " + msg, true);
        });

        Thread t = new Thread(task, "frontend-matches-worker");
        t.setDaemon(true);
        t.start();
    }

    private void showMatchesInSameView(String title, String content, List<String> contexts) {
        matchesTitleLabel.setText("Coincidencias - " + title + " (" + contexts.size() + ")");
        matchesListView.setItems(FXCollections.observableArrayList(contexts));
        matchesListView.setPlaceholder(new Label("No hay coincidencias para la consulta actual."));
        matchesListView.setCellFactory(lv -> new ListCell<>() {
            private final Label label = new Label();
            {
                label.setWrapText(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                label.setText(item);
                label.setMaxWidth(lv.getWidth() - 32);
                setGraphic(label);
                setText(null);
            }
        });
        matchesTextArea.setText(content == null ? "" : content);

        mainContentSplit.setVisible(false);
        mainContentSplit.setManaged(false);
        matchesPane.setVisible(true);
        matchesPane.setManaged(true);
    }

    private static List<String> buildMatchContexts(String content, List<String> terms, int radius, int maxMatches) {
        if (content == null || content.isBlank() || terms.isEmpty()) {
            return List.of();
        }

        String lower = content.toLowerCase();
        List<MatchHit> hits = new ArrayList<>();
        for (String term : terms) {
            String t = term.toLowerCase();
            int from = 0;
            while (true) {
                int idx = lower.indexOf(t, from);
                if (idx < 0) break;
                hits.add(new MatchHit(idx, t));
                from = idx + Math.max(1, t.length());
            }
        }

        hits.sort(Comparator.comparingInt(MatchHit::index));
        List<String> out = new ArrayList<>();
        for (int i = 0; i < Math.min(maxMatches, hits.size()); i++) {
            MatchHit hit = hits.get(i);
            int start = Math.max(0, hit.index() - radius);
            int end = Math.min(content.length(), hit.index() + hit.term().length() + radius);
            String fragment = content.substring(start, end).replaceAll("\\s+", " ").trim();
            String highlighted = fragment.replaceFirst("(?i)" + Pattern.quote(hit.term()), "[$0]");
            out.add((start > 0 ? "..." : "") + highlighted + (end < content.length() ? "..." : ""));
        }
        return out;
    }

    private record MatchHit(int index, String term) {}

    private enum FileKind { PDF, DOCX, TXT, OTHER }

    private static FileKind fileKind(String title, String path) {
        String s = (title == null ? "" : title) + " " + (path == null ? "" : path);
        String x = s.toLowerCase();
        if (x.contains(".pdf")) return FileKind.PDF;
        if (x.contains(".docx")) return FileKind.DOCX;
        if (x.contains(".txt")) return FileKind.TXT;
        return FileKind.OTHER;
    }

    private static Node fileIcon(FileKind kind) {
        // Iconos simples en SVGPath para no depender de librerías extra.
        SVGPath svg = new SVGPath();
        svg.getStyleClass().addAll("file-icon");
        svg.setContent(switch (kind) {
            case PDF -> "M4 1h8l4 4v14H4z M12 1v4h4";     // hoja con esquina
            case DOCX -> "M4 1h8l4 4v14H4z M12 1v4h4";
            case TXT -> "M4 1h8l4 4v14H4z M12 1v4h4";
            default -> "M4 1h8l4 4v14H4z M12 1v4h4";
        });
        svg.getStyleClass().add(switch (kind) {
            case PDF -> "file-icon-pdf";
            case DOCX -> "file-icon-docx";
            case TXT -> "file-icon-txt";
            default -> "file-icon-other";
        });
        return svg;
    }

    private void refreshDocuments() {
        runBackground("Cargando documentos...", () -> {
            List<DocumentDTO> docs = client.listDocuments();
            Platform.runLater(() -> {
                docsList.setItems(FXCollections.observableArrayList(docs));
                if (!docs.isEmpty()) {
                    docsList.getSelectionModel().selectFirst();
                } else {
                    previewArea.clear();
                }
            });
        }, () -> setStatus("Listo.", false));
    }

    private void runBackground(String startMessage, ThrowingRunnable work, Runnable onSuccess) {
        setBusy(true);
        setStatus(startMessage, false);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                work.run();
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            setBusy(false);
            onSuccess.run();
        });
        task.setOnFailed(e -> {
            setBusy(false);
            Throwable ex = task.getException();
            String msg = (ex == null) ? "Error desconocido" : (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            setStatus("Error: " + msg, true);
        });
        Thread t = new Thread(task, "frontend-worker");
        t.setDaemon(true);
        t.start();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private void setBusy(boolean busy) {
        searchButton.setDisable(busy);
        progress.setVisible(busy);
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        if (error) statusLabel.setStyle("-fx-text-fill: #b00020;");
        else statusLabel.setStyle("");
    }

    private void loadPreview(long documentId) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return client.getDocumentContent(documentId);
            }
        };

        task.setOnSucceeded(e -> {
            String content = task.getValue();
            previewArea.setText(content == null ? "" : content);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String msg = ex == null ? "Error cargando vista previa" : ex.getMessage();
            setStatus("Error: " + msg, true);
        });

        Thread t = new Thread(task, "frontend-preview-worker");
        t.setDaemon(true);
        t.start();
    }
}
