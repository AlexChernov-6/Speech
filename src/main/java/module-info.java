module com.example.speech {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires java.mail;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires org.postgresql.jdbc;
    requires javafx.base;
    requires java.rmi;

    opens com.example.speech to javafx.fxml;
    exports com.example.speech.control;
    opens com.example.speech.control to javafx.fxml;
    exports com.example.speech.util;
    opens com.example.speech.util to javafx.fxml;
    exports com.example.speech.model;
    opens com.example.speech.model to org.hibernate.orm.core;
}