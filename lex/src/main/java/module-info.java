module lex.application {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.desktop;
    opens lex.application to javafx.fxml;
    exports lex.application;
}
