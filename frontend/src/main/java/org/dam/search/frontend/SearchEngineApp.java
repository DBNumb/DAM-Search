package org.dam.search.frontend;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class SearchEngineApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Image icon = new Image(
                getClass().getResourceAsStream("/icon/text-search-icon.png")
        );

        // Aplicar icono
        stage.getIcons().add(icon);

        stage.setTitle("DAM-Search");

        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        FXMLLoader fxmlLoader = new FXMLLoader(SearchEngineApp.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
        scene.getStylesheets().add(SearchEngineApp.class.getResource("styles.css").toExternalForm());
        stage.setTitle("Buscador documental (Frontend JavaFX)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
