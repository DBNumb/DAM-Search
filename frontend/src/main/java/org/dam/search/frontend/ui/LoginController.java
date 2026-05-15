package org.dam.search.frontend.ui;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.dam.search.frontend.SearchEngineApp;
import org.dam.search.frontend.http.BackendClient;

import java.io.IOException;
import java.net.URI;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private ProgressIndicator progress;

    private final BackendClient client = new BackendClient(URI.create("http://localhost:8080"));

    @FXML
    public void onLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (isBlank(username) || isBlank(password)) {
            setStatus("Usuario y contraseña son obligatorios.", true);
            return;
        }

        setBusy(true);
        setStatus("Validando...", false);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return client.login(username, password);
            }
        };

        task.setOnSucceeded(e -> {
            setBusy(false);
            if (Boolean.TRUE.equals(task.getValue())) {
                openMainView(event);
            } else {
                setStatus("Credenciales incorrectas.", true);
            }
        });

        task.setOnFailed(e -> {
            setBusy(false);
            Throwable ex = task.getException();
            String msg = ex == null ? "Error desconocido" : ex.getMessage();
            setStatus("Error: " + msg, true);
        });

        Thread t = new Thread(task, "login-worker");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void onRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (isBlank(username) || isBlank(password)) {
            setStatus("Usuario y contraseña son obligatorios.", true);
            return;
        }

        setBusy(true);
        setStatus("Registrando...", false);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return client.register(username, password);
            }
        };

        task.setOnSucceeded(e -> {
            setBusy(false);
            if (Boolean.TRUE.equals(task.getValue())) {
                setStatus("Usuario registrado. Ahora pulsa Entrar.", false);
            } else {
                setStatus("No se pudo registrar (usuario existente o datos inválidos).", true);
            }
        });

        task.setOnFailed(e -> {
            setBusy(false);
            Throwable ex = task.getException();
            String msg = ex == null ? "Error desconocido" : ex.getMessage();
            setStatus("Error: " + msg, true);
        });

        Thread t = new Thread(task, "register-worker");
        t.setDaemon(true);
        t.start();
    }

    private void openMainView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(SearchEngineApp.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(SearchEngineApp.class.getResource("styles.css").toExternalForm());

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Buscador documental (Frontend JavaFX)");
        } catch (IOException ex) {
            setStatus("No se pudo abrir la pantalla principal.", true);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void setBusy(boolean busy) {
        loginButton.setDisable(busy);
        registerButton.setDisable(busy);
        progress.setVisible(busy);
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error ? "-fx-text-fill: #b00020;" : "");
    }
}