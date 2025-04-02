package org.example.webcamviewer;

public class Transcript {
    private String videoName;
    private String transcriptText;

    public Transcript(String videoName, String transcriptText) {
        this.videoName = videoName;
        this.transcriptText = transcriptText;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getTranscriptText() {
        return transcriptText;
    }

    public void setTranscriptText(String transcriptText) {
        this.transcriptText = transcriptText;
    }
}
