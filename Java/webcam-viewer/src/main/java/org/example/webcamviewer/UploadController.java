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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.example.webcamviewer.ResonoController;

public class UploadController {

    @FXML private Label welcomeMessage;
    @FXML private Label subheading;
    @FXML private Button uploadButton;
    @FXML private MediaView mediaView;
    @FXML private Button selectFileButton;
    @FXML private VBox uploadContainer;
    @FXML private Button playPauseButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;


    private MediaPlayer mediaPlayer;
    static final String VIDEO_FOLDER = "src/main/resources/videos";
    static final String TRANSCRIPT_FOLDER = "src/main/resources/transcripts/";
    private static final String BACKEND_URL = "http://127.0.0.1:5001/process";

    @FXML private void initialize() {
        // Ensure the required directories exist
        createDirectoryIfNotExists(VIDEO_FOLDER);
        createDirectoryIfNotExists(TRANSCRIPT_FOLDER);

        // Initialize UI components
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        if (statusLabel != null) {
            statusLabel.setText("");
        }
    }

    private void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Created directory: " + directoryPath);
            } else {
                System.err.println("Failed to create directory: " + directoryPath);
            }
        }
    }

    @FXML
    private void handleUploadButton(ActionEvent event) throws IOException {
        Parent uploadPage = FXMLLoader.load(getClass().getResource("upload.fxml"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(new Scene(uploadPage, 1600, 900));
        stage.setTitle("Upload Video");
        stage.show();
    }

    @FXML
    private void handleDashboardButton(ActionEvent event) throws IOException {
        Parent uploadPage = FXMLLoader.load(getClass().getResource("dashboard.fxml"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(new Scene(uploadPage, 1600, 900));
        stage.setTitle("Upload Video");
        stage.show();
    }

    @FXML
    private void handleTranscriptionButton(ActionEvent event) throws IOException {
        Parent uploadPage = FXMLLoader.load(getClass().getResource("transcript.fxml"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(new Scene(uploadPage, 1600, 900));
        stage.setTitle("Upload Video");
        stage.show();
    }


    @FXML
    private void handleSettingsButton(ActionEvent event) throws IOException {
        Parent uploadPage = FXMLLoader.load(getClass().getResource("dashboard.fxml"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(new Scene(uploadPage, 1600, 900));
        stage.setTitle("Upload Video");
        stage.show();
    }

    @FXML
    private void playPauseVideo() {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseButton.setText("Play");
            } else {
                mediaPlayer.play();
                playPauseButton.setText("Pause");
            }
        }
    }

    @FXML private void handleFileSelect() throws IOException, InterruptedException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mkv")
        );

        File selectedFile = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedFile = ResonoController.convertVideo(selectedFile);
            saveAndProcessVideo(selectedFile);
            playPauseButton.setVisible(true);
        }
    }

    private void saveAndProcessVideo(File file) {
        try {
            // Update UI to show processing
            if (statusLabel != null) {
                statusLabel.setText("Processing video...");
            }
            if (progressIndicator != null) {
                progressIndicator.setVisible(true);
            }

            // Define target path for video
            File targetFile = new File(VIDEO_FOLDER + file.getName());

            // Copy file to 'videos/' directory
            Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Play the video
            Media media = new Media(targetFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setAutoPlay(true);

            // Process the video asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    // Send to backend and get transcript
                    File transcriptFile = sendVideoToBackend(targetFile);

                    // Update UI on JavaFX thread when complete
                    javafx.application.Platform.runLater(() -> {
                        if (progressIndicator != null) {
                            progressIndicator.setVisible(false);
                        }
                        if (statusLabel != null) {
                            statusLabel.setText("Transcript generated: " + transcriptFile.getName());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    // Update UI on error
                    javafx.application.Platform.runLater(() -> {
                        if (progressIndicator != null) {
                            progressIndicator.setVisible(false);
                        }
                        if (statusLabel != null) {
                            statusLabel.setText("Error: " + e.getMessage());
                        }
                    });
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Error: " + e.getMessage());
            }
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }
        }
    }

    /**
     * Sends the video file to the Python backend for processing
     * @param videoFile The video file to process
     * @return The transcript file saved in the transcripts folder
     */
    private File sendVideoToBackend(File videoFile) throws IOException {
        System.out.println("Sending file " + videoFile.toString() + " to flask.");
        String boundary = UUID.randomUUID().toString();
        String fileName = videoFile.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        File transcriptFile = new File(TRANSCRIPT_FOLDER + baseName + ".srt");

        // Create URL connection
        URL url = new URL(BACKEND_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setConnectTimeout(60000); // 60 second timeout for connection
        connection.setReadTimeout(300000);   // 5 minute timeout for read

        // Write multipart form data
        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true)) {

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(videoFile.getName()).append("\"\r\n");
            writer.append("Content-Type: application/octet-stream\r\n\r\n");
            writer.flush();

            // Write the file data
            try (FileInputStream fileInputStream = new FileInputStream(videoFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            writer.append("\r\n--").append(boundary).append("--\r\n");
            writer.flush();
        }

        // Get the response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Save the response (SRT file) to the transcripts folder
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(transcriptFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }
            return transcriptFile;
        } else {
            // Handle error response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream()))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new IOException("Backend error (code " + responseCode +
                        "): " + errorResponse.toString());
            }
        }
    }

    @FXML private void navigateToTranscriptions(ActionEvent event) throws IOException {
        // Navigate to the transcription view (if you have one)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/webcamviewer/transcription-view.fxml"));
        Parent transcriptionView = loader.load();

        Scene currentScene = ((Node) event.getSource()).getScene();
        Stage stage = (Stage) currentScene.getWindow();

        stage.setScene(new Scene(transcriptionView));
        stage.show();
    }
}