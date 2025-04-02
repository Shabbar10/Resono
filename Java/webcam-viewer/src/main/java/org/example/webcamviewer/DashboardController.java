package org.example.webcamviewer;

//import com.jfoenix.controls.JFXButton;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.Parent;
import javafx.util.Duration;

public class DashboardController {
    @FXML
    public Button btnDashboard;
    @FXML
    private Label welcomeMessage;
    @FXML
    private Label subheading;
    @FXML
    private Button uploadButton;
    @FXML
    private Button uploadChangeButton;
    @FXML
    private Button btnTranscription;
    @FXML
    private BorderPane mainLayout;

    @FXML
    private void handleUploadButton(ActionEvent event) {
        try {
            System.out.println("Loading Upload Page...");
            Parent uploadPage = FXMLLoader.load(getClass().getResource("upload.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(uploadPage, 1600, 900));
            stage.setTitle("Upload Video");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleDashboardButton(ActionEvent event) throws IOException {
        Parent uploadPage = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("dashboard.fxml")));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(new Scene(uploadPage, 1600, 900));
        stage.setTitle("Upload Video");
        stage.show();
    }

    @FXML
    private void handleTranscriptionButton(ActionEvent event) throws IOException {
        Parent uploadPage = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("transcript.fxml")));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(new Scene(uploadPage, 1600, 900));
        stage.setTitle("Upload Video");
        stage.show();
    }


    @FXML
    private void handleSettingsButton(ActionEvent event) throws IOException {
        Parent uploadPage = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("dashboard.fxml")));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(new Scene(uploadPage, 1600, 900));
        stage.setTitle("Upload Video");
        stage.show();
    }

    public void initialize() {
        // Fade-in animation for the welcome message
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), welcomeMessage);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
}
