package org.example.webcamviewer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class TranscriptionController {

    @FXML private ComboBox<String> filterOptions;
    @FXML private TextField searchField;
    @FXML private ListView<String> videoListView;
    @FXML private TextArea transcriptArea;
    @FXML private TextArea summaryArea;
    @FXML private Button accessTranscriptButton;
    @FXML private Button generateSummaryButton;
    @FXML private Button downloadTranscriptButton;
    @FXML private Button downloadSummaryButton;

    private final TranscriptService transcriptService = new TranscriptService();
    private ObservableList<String> videoList = FXCollections.observableArrayList();
    private ObservableList<String> filteredList = FXCollections.observableArrayList();

    private final String VIDEO_FOLDER = "src/main/resources/videos";
    private final String TRANSCRIPT_FOLDER = "src/main/resources/transcripts"; // Path to transcripts

    public void initialize() {
        loadVideoFiles();
        filteredList.addAll(videoList);
        videoListView.setItems(filteredList);
    }

    private void loadVideoFiles() {
        File folder = new File(VIDEO_FOLDER);
        System.out.println("Looking for videos in: " + folder.getAbsolutePath());

        if (folder.exists() && folder.isDirectory()) {
            String[] files = folder.list((dir, name) ->
                    name.toLowerCase().endsWith(".mp4") ||
                            name.toLowerCase().endsWith(".avi") ||
                            name.toLowerCase().endsWith(".mov") ||
                            name.toLowerCase().endsWith(".wav")
            );

            if (files == null || files.length == 0) {
                System.out.println("No videos found in " + folder.getAbsolutePath());
                showAlert("No videos found in the folder.");
                return;
            }

            videoList.setAll(files);
            System.out.println("Videos loaded: " + videoList);
        } else {
            System.out.println("Video folder does not exist: " + folder.getAbsolutePath());
            showAlert("Video folder not found!");
        }

        videoListView.setItems(videoList);
    }


    @FXML
    private void onSearch(KeyEvent event) {
        String searchText = searchField.getText().toLowerCase();
        filteredList.clear();
        for (String video : videoList) {
            if (video.toLowerCase().contains(searchText)) {
                filteredList.add(video);
            }
        }
        videoListView.setItems(filteredList);
    }

    @FXML
    private void accessTranscript() {
        String selectedVideo = videoListView.getSelectionModel().getSelectedItem();
        if (selectedVideo != null) {
            System.out.println("videos" + selectedVideo.substring(0, selectedVideo.length() - 3) + "srt");
            File transcriptFile = new File(TRANSCRIPT_FOLDER, "videos" + selectedVideo.substring(0, selectedVideo.length() - 3) + "srt");
            if (transcriptFile.exists()) {
                try {
                    String content = Files.readString(Path.of(transcriptFile.getAbsolutePath()));
                    transcriptArea.setText(content);
                } catch (IOException e) {
                    showAlert("Error loading transcript: " + e.getMessage());
                }
            } else {
                System.out.println("Transcript for vid generated: ");
                showAlert("No transcript found for this video.");
            }
        } else {
            showAlert("Please select a video to access its transcript.");
        }
    }

    @FXML
    private void generateSummary() {
        String selectedVideo = videoListView.getSelectionModel().getSelectedItem();
        if (selectedVideo != null) {
            String summary = transcriptService.fetchSummary(selectedVideo);
            summaryArea.setText(summary);

            // Save summary to file
            saveToFile(selectedVideo + "_summary.txt", summary);
        } else {
            showAlert("Please select a video to generate its summary.");
        }
    }

    @FXML
    private void downloadTranscript() {
        String selectedVideo = videoListView.getSelectionModel().getSelectedItem();
        if (selectedVideo != null) {
            saveToFile(selectedVideo + "_transcript.srt", transcriptArea.getText());
        } else {
            showAlert("Select a video to download its transcript.");
        }
    }

    @FXML
    private void downloadSummary() {
        String selectedVideo = videoListView.getSelectionModel().getSelectedItem();
        if (selectedVideo != null) {
            saveToFile(selectedVideo + "_summary.txt", summaryArea.getText());
        } else {
            showAlert("Select a video to download its summary.");
        }
    }

    private void saveToFile(String fileName, String content) {
        if (content == null || content.isBlank()) {
            showAlert("No content to save.");
            return;
        }

        File transcriptDir = new File(TRANSCRIPT_FOLDER);
        if (!transcriptDir.exists()) {
            transcriptDir.mkdirs();
        }

        File file = new File(transcriptDir, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            showAlert("File saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert("Error saving file: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }
}
