module org.dam.search.frontend {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.dam.search.frontend to javafx.fxml;
    exports org.dam.search.frontend;
}