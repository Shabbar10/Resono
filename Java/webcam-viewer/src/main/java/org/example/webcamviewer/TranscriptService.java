package org.example.webcamviewer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TranscriptService {

    public List<String> getUploadedVideos() {
        // Placeholder: Fetch the list of uploaded video names from backend
        return List.of("Video1.mp4", "Lecture2.mp4", "Presentation3.mp4");
    }

    public Transcript fetchTranscript(String videoName) {
        // Placeholder: Fetch the transcript from backend
        return new Transcript(videoName, "Transcript for " + videoName);
    }

    public String fetchSummary(String videoName) {
        // Placeholder: Generate and fetch summary from backend
        return "Summary for " + videoName;
    }

    public void saveToFile(String fileName, String content) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content);
            System.out.println("File saved: " + fileName);
        } catch (IOException e) {
            System.out.println("Error saving file: " + fileName);
        }
    }
}