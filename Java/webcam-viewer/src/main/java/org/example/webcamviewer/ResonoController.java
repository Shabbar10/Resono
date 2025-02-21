package org.example.webcamviewer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResonoController {
    private Webcam webcam;
    @FXML private ImageView webcamView;
    private boolean isRunning = true;
    @FXML Label emotion;

    @FXML private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    @FXML private Button playPauseButton;

    private ExecutorService webcamExecutor;
    private ExecutorService videoExecutor;
    private ExecutorService httpExecutor;

    private final OkHttpClient client = new OkHttpClient();

    @FXML
    public void initialize() {
        webcamExecutor = Executors.newSingleThreadExecutor();
        videoExecutor = Executors.newSingleThreadExecutor();
        httpExecutor = Executors.newFixedThreadPool(2);
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

                while (isRunning) {
                    if (webcam.isOpen()) {
                        BufferedImage frame = webcam.getImage();

                        if (frame != null) {
                            Image fxImage = convertToFxImage(frame);
                            Platform.runLater(() -> webcamView.setImage(fxImage));

                            sendFrameToFlask(frame);
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

    private void sendFrameToFlask(BufferedImage image) {
        httpExecutor.execute(() -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                byte[] imageBytes = baos.toByteArray();

                RequestBody requestBody = RequestBody.create(imageBytes, MediaType.parse("image/png"));

                Request request = new Request.Builder()
                        .url("http://127.0.0.1:5000/")
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        System.err.println("Failed to send frame: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful()) {
                                System.out.println("Flask server response: " + response.code());
                                String responseBody = response.body().string();
                                Platform.runLater(() -> emotion.setText(responseBody));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            response.close();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void playPauseVideo() {
        if (mediaPlayer != null)    {
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
        isRunning = false;

        if (httpExecutor != null)
            httpExecutor.shutdownNow();

        if (webcamExecutor != null)
            webcamExecutor.shutdownNow();

        if (videoExecutor != null)
            videoExecutor.shutdownNow();

        if (mediaPlayer != null)
            mediaPlayer.dispose();

        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            System.out.println("Webcam closed...");
        }

        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}