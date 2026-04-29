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
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.dam.search.frontend.http.BackendClient;
import org.dam.search.frontend.model.DocumentDTO;
import org.dam.search.frontend.model.SearchResultDTO;
import org.dam.search.frontend.model.SearchSelector;

import java.io.File;
import java.net.URI;
import java.util.List;


public class SearchEngineUI {
    @FXML private Label statusLabel;
    @FXML private Label footerLabel;
    @FXML private TextField queryField;
    @FXML private ComboBox<SearchSelector> engineCombo;

    @FXML private Button button;

    @FXML private ListView<DocumentDTO> docsList;
    @FXML private ListView<SearchResultDTO> resultsList;
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
                setGraphic(resultCard(item.getTitle(), item.getSnippet(), item.getScore(), fileKind(item.getTitle(),null)));
            }
        });


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
    private static Node resultCard(String title, String snippet, double score, FileKind kind) {
        Node icon = fileIcon(kind);
        Label t = new Label(title);
        t.getStyleClass().add("card-title");

        Label sn = new Label(shorten(snippet, 170));
        sn.getStyleClass().add("card-snippet");
        sn.setWrapText(true);

        Label sc = new Label(String.format("%.4f", score));
        sc.getStyleClass().add("badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, t, spacer, sc);
        VBox text = new VBox(6, top, sn);

        HBox row = new HBox(10, icon, text);
        row.getStyleClass().add("card");
        row.setPadding(new Insets(10));
        return row;
    }

    private static String shorten(String snippet, int max) {
        if(snippet == null) return "";
        String trimmed = snippet.replaceAll("\\+s"," ").trim();
        if(snippet.length() <= max) return snippet;
        return snippet.substring(0, max-1) + "...";
    }

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
            Platform.runLater(() -> docsList.setItems(FXCollections.observableArrayList(docs)));
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
}

