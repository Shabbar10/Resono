package org.example.webcamviewer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Dashboard extends Application {
    private WebcamController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("dashboard.fxml")));
        Scene scene = new Scene(root, 1600, 900);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Resono");
        Image icon = new Image("icon.jpg");
        primaryStage.getIcons().add(icon);
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
        launch(args);
    }
}