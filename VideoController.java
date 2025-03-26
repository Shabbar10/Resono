package com.example.resono;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class VideoController implements Initializable{

    @FXML
    private MediaView mediaView;

    @FXML
    private Button playPauseButton, rewindButton, forwardButton, fullscreenButton;

    @FXML
    private Slider volumeSlider, progressSlider;

    @FXML
    private HBox controlsContainer;

    private File file;
    private Media media;
    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            File file = new File("C:\\Users\\purva\\IdeaProjects\\Resono\\src\\main\\resources\\com\\example\\resono\\bowl.mp4");

            if (!file.exists()) {
                System.out.println("ERROR: Video file NOT FOUND at: " + file.getAbsolutePath());
                return;
            } else {
                System.out.println("Video file found: " + file.getAbsolutePath());
            }

            URL resource = getClass().getResource("/com/example/resono/bowl.mp4");
            if (resource == null) {
                System.out.println("ERROR: Video file not found using getResource()");
                return;
            } else {
                System.out.println("Video file found using getResource()");
            }

            media = new Media(resource.toString());
            mediaPlayer = new MediaPlayer(media);

            // Debugging error messages
            mediaPlayer.setOnError(() -> System.out.println("MediaPlayer Error: " + mediaPlayer.getError().getMessage()));

            // Ensure media is only played when it's fully ready
            mediaPlayer.setOnReady(() -> {
                System.out.println("Media is ready, playing video...");
                Platform.runLater(() -> mediaPlayer.play());
            });

            Platform.runLater(() -> mediaView.setMediaPlayer(mediaPlayer));

            playPauseButton.setOnAction(e -> togglePlayPause());
            rewindButton.setOnAction(e -> mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(5))));
            forwardButton.setOnAction(e -> mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(5))));

//            fullscreenButton.setOnAction(e -> {
//                Stage stage = (Stage) fullscreenButton.getScene().getWindow();
//                stage.setFullScreen(true);
//
//                stage.fullScreenProperty().addListener((obs, wasFullScreen, isNowFullScreen) -> {
//                    if (isNowFullScreen) {
//                        mediaView.fitWidthProperty().bind(stage.widthProperty().multiply(0.8));
//                        mediaView.fitHeightProperty().bind(stage.heightProperty().multiply(0.8));
//                        mediaView.layoutXProperty().bind(stage.widthProperty().subtract(mediaView.fitWidthProperty()).divide(2));
//                        mediaView.layoutYProperty().bind(stage.heightProperty().subtract(mediaView.fitHeightProperty()).divide(2));
//
//                        controlsContainer.layoutXProperty().bind(stage.widthProperty().subtract(controlsContainer.widthProperty()).divide(2));
//                        controlsContainer.layoutYProperty().bind(mediaView.layoutYProperty().add(mediaView.fitHeightProperty()).add(20));
//
//                    } else {
//                        mediaView.fitWidthProperty().unbind();
//                        mediaView.fitHeightProperty().unbind();
//                        mediaView.setFitWidth(600); // Reset to original size
//                        mediaView.setFitHeight(300);
//
//                        controlsContainer.layoutXProperty().unbind();
//                        controlsContainer.layoutYProperty().unbind();
//                        controlsContainer.setLayoutX(0);
//                        controlsContainer.setLayoutY(0);
//                    }
//                });
//            });

//            volumeSlider.setValue(50);
//            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
//            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> mediaPlayer.setVolume(newVal.doubleValue() / 100));

            mediaPlayer.setOnReady(() -> progressSlider.setMax(mediaPlayer.getTotalDuration().toSeconds()));
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> progressSlider.setValue(newTime.toSeconds()));
            progressSlider.setOnMouseReleased(e -> mediaPlayer.seek(Duration.seconds(progressSlider.getValue())));

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("Play");
        } else {
            mediaPlayer.play();
            playPauseButton.setText("Pause");
        }
    }


}
