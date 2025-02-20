module org.example.webcamviewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires webcam.capture;
    requires java.desktop;
    requires javafx.media;
    requires okhttp3;
    requires annotations;

    opens org.example.webcamviewer to javafx.fxml;
    exports org.example.webcamviewer;
}