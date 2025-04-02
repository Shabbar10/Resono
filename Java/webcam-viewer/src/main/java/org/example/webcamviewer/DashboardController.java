package org.example.webcamviewer;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.Parent;
import javafx.util.Duration;
import java.io.IOException;

public class DashboardController {

    @FXML private Label welcomeMessage;
    @FXML private Label subheading;
    @FXML private Button uploadButton;
    @FXML private Button btnTranscription;
    @FXML private BorderPane mainLayout; // This should be the ID of your BorderPane

    public void initialize() {
        // Fade-in animation for the welcome message
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), welcomeMessage);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    @FXML
    private void openTranscriptionView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("transcript.fxml"));
            Parent transcriptionView = loader.load();

            // Set Transcription.fxml in the center of the BorderPane
            mainLayout.setCenter(transcriptionView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
