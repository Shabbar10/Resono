package com.emotionanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.media.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import okhttp3.*;
import org.json.JSONObject;
import java.io.*;
import java.util.Base64;
import javafx.application.Platform;
import javafx.scene.image.Image;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainController {
    @FXML private MediaView mediaView;
    @FXML private ImageView webcamView;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private TextArea reportArea;

    private MediaPlayer mediaPlayer;
    private final OkHttpClient client = new OkHttpClient();
    private final AtomicBoolean isAnalyzing = new AtomicBoolean(false);

    @FXML
    public void initialize() {
        genreComboBox.getItems().addAll(
            "comedy", "horror", "drama", "action", "romance", "thriller"
        );
    }

    @FXML
    private void chooseVideo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mkv")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Media media = new Media(file.toURI().toString());
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
        }
    }

    @FXML
    private void startAnalysis() {
        if (mediaPlayer == null || genreComboBox.getValue() == null) {
            showAlert("Error", "Please select both a video and genre first.");
            return;
        }

        isAnalyzing.set(true);
        mediaPlayer.play();

        new Thread(() -> {
            while (isAnalyzing.get()) {
                try {
                    // Simulate webcam frame capture (replace with actual webcam capture)
                    // In a real implementation, you would capture from webcam here
                    byte[] frameData = new byte[0]; // Replace with actual frame data
                    String base64Frame = Base64.getEncoder().encodeToString(frameData);

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("genre", genreComboBox.getValue());
                    requestBody.put("frame", base64Frame);
                    requestBody.put("timestamp", mediaPlayer.getCurrentTime().toSeconds());

                    RequestBody body = RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                    );

                    Request request = new Request.Builder()
                        .url("http://localhost:5000/analyze")
                        .post(body)
                        .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            String processedFrame = jsonResponse.getString("processedFrame");
                            
                            // Update UI with processed frame
                            Platform.runLater(() -> {
                                byte[] imageData = Base64.getDecoder().decode(processedFrame);
                                Image image = new Image(new ByteArrayInputStream(imageData));
                                webcamView.setImage(image);
                            });
                        }
                    }

                    Thread.sleep(100); // Adjust frame rate as needed
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @FXML
    private void stopAnalysis() {
        isAnalyzing.set(false);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        // Generate final report
        JSONObject requestBody = new JSONObject();
        requestBody.put("genre", genreComboBox.getValue());

        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
            .url("http://localhost:5000/generate-report")
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String report = response.body().string();
                Platform.runLater(() -> reportArea.setText(report));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
