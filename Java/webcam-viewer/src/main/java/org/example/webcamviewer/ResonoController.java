package org.example.webcamviewer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResonoController {
    private Webcam webcam;
    @FXML private ImageView webcamView;
    private boolean isRunning = true;
    @FXML Label emotion;

    private final Map<String, Integer> emotionCounts = new HashMap<>();
    @FXML private PieChart pieChart = new PieChart();
    private final List<String> emotions = Arrays.asList(
            "Happy", "Sad", "Angry", "Surprise", "Neutral", "Fear", "Disgust"
    );

    @FXML private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    @FXML private Button playPauseButton;

    @FXML Button chooseVideo = new Button("Select Video");
    FileChooser fileChooser = new FileChooser();
    File selectedVideo;

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

        initializeChart();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> updateChart()));
        timeline.setCycleCount(Timeline.INDEFINITE); // Repeat forever
        timeline.play();

        //loadVideo();
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

    private void initializeChart() {
        for (String emotionType : emotions) {
            emotionCounts.put(emotionType, 1); // Start with 1 for smoother animation
        }

        for (String emotionType : emotions) {
            pieChart.getData().add(new PieChart.Data(emotionType, 1));
        }
    }

    private void updateChart() {
        String detectedEmotion = emotion.getText();

        if (detectedEmotion == null || detectedEmotion.trim().isEmpty() || !emotionCounts.containsKey(detectedEmotion)) {
            detectedEmotion = "Neutral";
        }

        Integer currentCount = emotionCounts.get(detectedEmotion);
        if (currentCount != null) {
            emotionCounts.put(detectedEmotion, currentCount + 1);
        }

        Platform.runLater(() -> {
            for (PieChart.Data data : pieChart.getData()) {
                String emotionType = data.getName();
                Integer count = emotionCounts.get(emotionType);
                if (count != null) {
                    data.setPieValue(count); // Update chart
                }
            }
        });
    }

    @FXML
    private void handleChooseVideo() {
        fileChooser.setTitle("Select Video");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*mkv")
        );

        selectedVideo = fileChooser.showOpenDialog(chooseVideo.getScene().getWindow());

        if (selectedVideo != null) {
            loadVideo(selectedVideo);
            sendVideoToFlask(selectedVideo);
        }
    }

    static File convertVideo(File inputFile) throws IOException, InterruptedException {
        Path outputDir = Paths.get("src/main/resources/videos");

        String outputFileName = getOutputFileName(inputFile);
        Path outputPath = outputDir.resolve(outputFileName);

        if (Files.exists(outputPath)) {
            System.out.println("Already converted: " + outputPath);
            return outputPath.toFile();
        }

        // Comprehensive logging of input parameters
        System.out.println("Input File: " + inputFile.getAbsolutePath());
        System.out.println("Input File Exists: " + inputFile.exists());
        System.out.println("Input File is Readable: " + inputFile.canRead());
        System.out.println("Output Path: " + outputPath);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", inputFile.getAbsolutePath(),
                "-c:v", "libx264",
                "-preset", "medium",
                "-crf", "23",
                "-c:a", "aac",
                "-b:a", "128k",
                "-movflags", "+faststart",
                outputPath.toString()
        );

        // Capture both standard error and standard output
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            // Read the process output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    System.out.println("FFmpeg Output: " + line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Video converted successfully: " + outputPath);
                return outputPath.toFile();
            } else {
                System.err.println("Video conversion failed. Exit Code: " + exitCode);
                System.err.println("FFmpeg Output:\n" + output);
                return null;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Exception during video conversion:");
            e.printStackTrace();
            throw e;
        }
    }

    private static String getOutputFileName(File inputFile) {
        String baseName = inputFile.getName();
        int dotIndex = baseName.lastIndexOf('.');
        baseName = baseName.substring(0, dotIndex);

        return baseName + ".mp4";
    }

     void loadVideo(File file) {
        videoExecutor.execute(() -> {
            try {
                File convertedFile = convertVideo(file);

                if (convertedFile == null) {
                    return;
                }

                Media video = new Media(convertedFile.toURI().toString());
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

    private void sendVideoToFlask(File videoFile) {
        httpExecutor.execute(() -> {
            try {
                // Create a multipart request body for file upload
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                                "file",
                                videoFile.getName(),
                                RequestBody.create(videoFile, MediaType.parse("video/mp4"))
                        )
                        .build();

                // Create the request
                Request request = new Request.Builder()
                        .url("http://127.0.0.1:5001/process")
                        .post(requestBody)
                        .build();

                // Execute the request asynchronously
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Platform.runLater(() -> {
                            System.err.println("Failed to upload video: " + e.getMessage());
                            // You might want to update the UI to show the error
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful()) {
                                System.out.println("Video processed successfully: " + response.code());

                                // Handle the SRT file that's returned
                                ResponseBody responseBody = response.body();
                                if (responseBody != null) {
                                    // Save the returned SRT file
                                    String fileName = videoFile.getName().replaceFirst("[.][^.]+$", "") + ".srt";
                                    File outputFile = new File("subtitles/" + fileName);

                                    // Ensure the directory exists
                                    outputFile.getParentFile().mkdirs();

                                    // Write the response to file
                                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                        fos.write(responseBody.bytes());
                                    }

                                    Platform.runLater(() -> {
                                        System.out.println("SRT file saved to: " + outputFile.getAbsolutePath());
                                        // Update UI if needed
                                    });
                                }
                            } else {
                                Platform.runLater(() -> {
                                    System.err.println("Server error: " + response.code() + " " + response.message());
                                    // Update UI to show error
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            response.close();
                        }
                    }
                });
            } catch (Exception e) {
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