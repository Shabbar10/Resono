package org.example.webcamviewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ResonoApplication extends Application {
    private ResonoController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ResonoApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("Resono!");
        stage.setScene(scene);
        stage.show();

        controller = fxmlLoader.getController();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.stopWebcam();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}