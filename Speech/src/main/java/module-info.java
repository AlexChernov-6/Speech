module com.example.speech {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires java.mail;

    opens com.example.speech to javafx.fxml;
    exports com.example.speech.control;
    opens com.example.speech.control to javafx.fxml;
    exports com.example.speech.util;
    opens com.example.speech.util to javafx.fxml;
}