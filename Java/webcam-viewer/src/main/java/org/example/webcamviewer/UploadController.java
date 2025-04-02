package org.example.webcamviewer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class UploadController {

    @FXML
    private Label welcomeMessage;
    @FXML
    private Label subheading;
    @FXML
    private Button uploadButton;
    @FXML
    private MediaView mediaView;
    @FXML
    private Button selectFileButton;
    @FXML
    private VBox uploadContainer;

    private MediaPlayer mediaPlayer;
    private static final String VIDEO_FOLDER = "Autobot/Java/webcam-viewer/src/main/resources/videos/";

    @FXML
    private void initialize() {
        // Ensure the 'videos' directory exists
        File videoDir = new File(VIDEO_FOLDER);
        if (!videoDir.exists()) {
            videoDir.mkdir();
        }
    }

    @FXML
    private void handleFileSelect() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mkv"));

        File selectedFile = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());
        if (selectedFile != null) {
            saveAndPlayVideo(selectedFile);
        }
    }

    private void saveAndPlayVideo(File sourceFile) {
        try {
            File videoDir = new File(VIDEO_FOLDER);
            if (!videoDir.exists()) {
                videoDir.mkdirs();
            }

            Path destinationPath = Paths.get(VIDEO_FOLDER, sourceFile.getName());

            System.out.println("Attempting to copy from: " + sourceFile.getAbsolutePath());
            System.out.println("Destination path: " + destinationPath.toAbsolutePath());

            // 🔴 Stop and release media player before replacing file
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;  // Release the reference
                System.gc(); // Force garbage collection to release the file lock
            }

            if (Files.exists(destinationPath)) {
                Files.delete(destinationPath); // Now it should not be locked
            }

            Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Video saved successfully at: " + destinationPath.toAbsolutePath());

            Media media = new Media(destinationPath.toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setAutoPlay(true);
        } catch (IOException e) {
            System.out.println("❌ Error copying file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}