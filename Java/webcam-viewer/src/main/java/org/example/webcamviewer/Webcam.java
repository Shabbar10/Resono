package org.example.webcamviewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Webcam extends Application {
    private WebcamController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("webcam.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1600, 900);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Resono Dashboard");
        primaryStage.show();

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