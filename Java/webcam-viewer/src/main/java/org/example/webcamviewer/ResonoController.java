package org.example.webcamviewer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResonoController {
    @FXML private ImageView webcamView;

    @FXML private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    @FXML private Button playPauseButton;

    private Webcam webcam;
    private ExecutorService webcamExecutor;
    private ExecutorService videoExecutor;

    @FXML
    public void initialize() {
        webcamExecutor = Executors.newSingleThreadExecutor();
        videoExecutor = Executors.newSingleThreadExecutor();
        webcamView.setScaleX(-1);

        loadVideo();
        startWebcam();
    }

    private void startWebcam() {
        webcamExecutor.execute(() -> {
            try {
                webcam = Webcam.getDefault();
                webcam.setViewSize(new Dimension(640, 480));
                webcam.open();
                System.out.println("Webcam opened...");

                while (true) {
                    if (webcam.isOpen()) {
                        BufferedImage frame = webcam.getImage();

                        if (frame != null) {
                            Image fxImage = convertToFxImage(frame);
                            Platform.runLater(() -> webcamView.setImage(fxImage));
                        }
                    } else {
                        break;
                    }
                }
            } catch (WebcamException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadVideo() {
        videoExecutor.execute(() -> {
            try {
                /*
                System.out.println("Starting video loading process");
                var resource = getClass().getResource("/videos/hello.mp4");
                if (resource == null) {
                    System.err.println("Video not found");
                    return;
                }

                String videoPath = resource.toExternalForm();
                System.out.println("Video Path: " + videoPath);
                */

                //File videoFile = new File("C:\\Dev\\webcam-viewer\\src\\main\\resources\\videos\\hello.mp4");
                File videoFile = new File("src/main/resources/videos/hello.mp4");

                Media video = new Media(videoFile.toURI().toString());
                System.out.println("Media object created");

                mediaPlayer = new MediaPlayer(video);
                System.out.println("Media player created");

                mediaPlayer.setOnReady(() -> {
                    System.out.println("MediaPlayer is ready");
                });

                mediaPlayer.setOnError(() -> {
                    System.err.println("MediaPlayer error: " + mediaPlayer.getError().getMessage());
                });

                mediaPlayer.setOnPlaying(() -> {
                    System.out.println("Media playback started");
                });

                mediaPlayer.setAutoPlay(true);

                Platform.runLater(() -> {
                    mediaView.setMediaPlayer(mediaPlayer);
                    System.out.println("MediaPlayer set on MediaView");
                    //mediaView.setMediaPlayer(mediaPlayer);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

    private Image convertToFxImage(BufferedImage frame) {
        WritableImage writableImage = new WritableImage(frame.getWidth(), frame.getHeight());
        PixelWriter pw = writableImage.getPixelWriter();

        for (int y = 0; y < frame.getHeight(); y++) {
            for (int x = 0; x < frame.getWidth(); x++) {
                pw.setArgb(x, y, frame.getRGB(x, y));
            }
        }
        return writableImage;
    }

    public void stopWebcam() {
        webcamExecutor.shutdownNow(); // Stop the capture thread
        videoExecutor.shutdownNow();
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            System.out.println("Webcam closed...");
        }
    }
}