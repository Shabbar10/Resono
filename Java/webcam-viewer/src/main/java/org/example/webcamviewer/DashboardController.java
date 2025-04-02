package org.example.webcamviewer;

import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class DashboardController {

    @FXML private Label welcomeMessage;
    @FXML private Label subheading;
    @FXML private JFXButton uploadButton;

    public void initialize() {
        // Fade-in animation for the welcome message
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), welcomeMessage);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
}
