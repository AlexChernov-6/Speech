module com.example.speech {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.desktop;

    opens com.example.speech to javafx.fxml;
    exports com.example.speech;
    exports com.example.speech.controls;
    opens com.example.speech.controls to javafx.fxml;
    exports com.example.speech.util;
    opens com.example.speech.util to javafx.fxml;
}