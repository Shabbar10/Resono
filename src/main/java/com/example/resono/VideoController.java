package com.example.resono;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class VideoController implements Initializable{

    @FXML
    private VBox videoContainer;

    @FXML
    private MediaView mediaView;

    @FXML
    private Button playPauseButton, rewindButton, forwardButton, fullScreenButton, logoutButton;

    @FXML
    private Slider volumeSlider, progressSlider;

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

            media = new Media(file.toURI().toString());

            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnError(() -> System.out.println("MediaPlayer Error: " + mediaPlayer.getError().getMessage()));

            mediaPlayer.setOnReady(() -> {
                System.out.println("Media is ready, playing video...");
                Platform.runLater(() -> mediaPlayer.play());
            });

            Platform.runLater(() -> mediaView.setMediaPlayer(mediaPlayer));

            playPauseButton.setOnAction(e -> togglePlayPause());
            rewindButton.setOnAction(e -> mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(5))));
            forwardButton.setOnAction(e -> mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(5))));

            mediaPlayer.setOnReady(() -> progressSlider.setMax(mediaPlayer.getTotalDuration().toSeconds()));
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> progressSlider.setValue(newTime.toSeconds()));
            progressSlider.setOnMouseReleased(e -> mediaPlayer.seek(Duration.seconds(progressSlider.getValue())));

            setupFullScreenListener();

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void setupFullScreenListener() {
        Stage stage = (Stage) fullScreenButton.getScene().getWindow();
//        stage.setFullScreen(true);

        stage.fullScreenProperty().addListener((obs, wasFullScreen, isNowFullScreen) -> {
            if (isNowFullScreen) {
                System.out.println("Entered Fullscreen");
                // Resize VBox to fit screen
                videoContainer.prefWidthProperty().bind(stage.widthProperty());
                videoContainer.prefHeightProperty().bind(stage.heightProperty());

                // Allow MediaView to grow dynamically
                VBox.setVgrow(mediaView, javafx.scene.layout.Priority.ALWAYS);

                // Resize MediaView to take 80% of screen size
                mediaView.fitWidthProperty().bind(stage.widthProperty().multiply(0.8));
                mediaView.fitHeightProperty().bind(stage.heightProperty().multiply(0.8));

                // Resize progress slider to 90% width
                progressSlider.prefWidthProperty().bind(stage.widthProperty().multiply(0.9));

            } else {
                System.out.println("Exited Fullscreen");

                // Reset layout when exiting fullscreen
                videoContainer.prefWidthProperty().unbind();
                videoContainer.prefHeightProperty().unbind();
                mediaView.fitWidthProperty().unbind();
                mediaView.fitHeightProperty().unbind();
                progressSlider.prefWidthProperty().unbind();

                mediaView.setFitWidth(600);
                mediaView.setFitHeight(300);
                progressSlider.setPrefWidth(600);
            }
        });
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

    public void setFullScreen() {
        Stage stage = (Stage) fullScreenButton.getScene().getWindow();
        stage.setFullScreen(true);

        stage.fullScreenProperty().addListener((obs, wasFullScreen, isNowFullScreen) -> {
            if (isNowFullScreen) {
                // Resize VBox to fit screen
                videoContainer.prefWidthProperty().bind(stage.widthProperty());
                videoContainer.prefHeightProperty().bind(stage.heightProperty());

                // Allow MediaView to grow dynamically
                VBox.setVgrow(mediaView, javafx.scene.layout.Priority.ALWAYS);

                // Resize MediaView to take 80% of screen size
                mediaView.fitWidthProperty().bind(stage.widthProperty().multiply(0.8));
                mediaView.fitHeightProperty().bind(stage.heightProperty().multiply(0.8));

                // Resize progress slider to 90% width
                progressSlider.prefWidthProperty().bind(stage.widthProperty().multiply(0.9));

            } else {
                // Reset layout when exiting fullscreen
                videoContainer.prefWidthProperty().unbind();
                videoContainer.prefHeightProperty().unbind();
                mediaView.fitWidthProperty().unbind();
                mediaView.fitHeightProperty().unbind();
                progressSlider.prefWidthProperty().unbind();

                mediaView.setFitWidth(600);
                mediaView.setFitHeight(300);
                progressSlider.setPrefWidth(600);
            }
        });
    }

    public void logout(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resono-login.fxml"));
        Scene scene = new Scene(loader.load(), 600, 600);
        stage.setScene(scene);
        stage.setTitle("Resono - Video Player");
        stage.show();
    }

//    public void setFullScreen() {
//        Stage stage = (Stage) fullScreenButton.getScene().getWindow();
//        stage.setFullScreen(true);
//
//        stage.fullScreenProperty().addListener((obs, wasFullScreen, isNowFullScreen) -> {
//            if (isNowFullScreen) {
//                // Bind VBox to fit the entire screen
//                videoContainer.prefWidthProperty().bind(stage.widthProperty());
//                videoContainer.prefHeightProperty().bind(stage.heightProperty());
//
//                // Resize MediaView to take 80% of screen width and height
//                mediaView.fitWidthProperty().bind(stage.widthProperty().multiply(0.8));
//                mediaView.fitHeightProperty().bind(stage.heightProperty().multiply(0.8));
//
//                // Resize progress slider to 90% of screen width
//                progressSlider.prefWidthProperty().bind(stage.widthProperty().multiply(0.9));
//            } else {
//                // Reset sizes when exiting fullscreen
//                videoContainer.prefWidthProperty().unbind();
//                videoContainer.prefHeightProperty().unbind();
//                mediaView.fitWidthProperty().unbind();
//                mediaView.fitHeightProperty().unbind();
//                progressSlider.prefWidthProperty().unbind();
//
//                mediaView.setFitWidth(600);
//                mediaView.setFitHeight(300);
//                progressSlider.setPrefWidth(600);
//            }
//        });
//    }

}
