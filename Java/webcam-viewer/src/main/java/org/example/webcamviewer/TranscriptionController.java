package org.example.webcamviewer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyEvent;
import java.util.List;

public class TranscriptionController {

    public ComboBox<String> filterOptions;
    public TextField searchField;
    @FXML
    private ListView<String> videoListView;

    @FXML
    private TextArea transcriptArea;

    @FXML
    private TextArea summaryArea;

    @FXML
    private Button accessTranscriptButton;

    @FXML
    private Button generateSummaryButton;

    @FXML
    private Button downloadTranscriptButton;

    @FXML
    private Button downloadSummaryButton;

    private final TranscriptService transcriptService = new TranscriptService();

    private ObservableList<String> videoList = FXCollections.observableArrayList();
    private ObservableList<String> filteredList = FXCollections.observableArrayList();

    public void initialize() {
        // Example video list
        videoList.addAll("Video 1", "Tutorial on AI", "JavaFX Basics", "Deep Learning Intro", "React Guide");
        filteredList.addAll(videoList);
        videoListView.setItems(filteredList);
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
            Transcript transcript = transcriptService.fetchTranscript(selectedVideo);
            transcriptArea.setText(transcript.getTranscriptText());
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
        } else {
            showAlert("Please select a video to generate its summary.");
        }
    }

    @FXML
    private void downloadTranscript() {
        String transcriptText = transcriptArea.getText();
        transcriptService.saveToFile("transcript.txt", transcriptText);
    }

    @FXML
    private void downloadSummary() {
        String summaryText = summaryArea.getText();
        transcriptService.saveToFile("summary.txt", summaryText);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }
}
