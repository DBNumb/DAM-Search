module org.dam.search.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires java.net.http;

    opens org.dam.search.frontend to javafx.fxml;
    exports org.dam.search.frontend;
    exports org.dam.search.frontend.ui;
    opens org.dam.search.frontend.ui to javafx.fxml;
}