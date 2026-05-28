module org.example.webcamviewer {
    requires javafx.web;
    requires javafx.media;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires webcam.capture;
    requires java.desktop;
    requires okhttp3;
    requires annotations;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires jbcrypt;

    opens org.example.webcamviewer to javafx.fxml;
    exports org.example.webcamviewer;
}