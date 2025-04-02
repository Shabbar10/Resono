package org.example.webcamviewer;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class UploadController {

    @FXML private Label welcomeMessage;
    @FXML private Label subheading;
    @FXML private Button uploadButton;
    @FXML private MediaView mediaView;
    @FXML private Button selectFileButton;
    @FXML private VBox uploadContainer;

    private MediaPlayer mediaPlayer;
    static final String VIDEO_FOLDER = "src/main/resources/videos/";

    @FXML private void initialize() {
        // Ensure the 'videos' directory exists
        File videoDir = new File(VIDEO_FOLDER);
        if (!videoDir.exists()) {
            videoDir.mkdir();
        }
    }

    @FXML private void handleFileSelect() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mkv"));

        File selectedFile = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());
        if (selectedFile != null) {
            saveAndPlayVideo(selectedFile);
        }
    }

    private void saveAndPlayVideo(File file) {
        try {
            // Define target path
            File targetFile = new File(VIDEO_FOLDER + file.getName());

            // Copy file to 'videos/' directory
            Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Play the video
            Media media = new Media(targetFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setAutoPlay(true); // Automatically play video
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}