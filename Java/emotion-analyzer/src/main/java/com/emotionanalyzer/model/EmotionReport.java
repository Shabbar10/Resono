package com.emotionanalyzer.model;

import java.util.Map;
import java.util.List;

public class EmotionReport {
    private String genre;
    private double duration;
    private int totalFrames;
    private int processedFrames;
    private int totalEmotionsDetected;
    private Map<String, Double> emotionDistribution;
    private double genreMatchScore;
    private String primaryEmotion;
    private List<String> expectedEmotions;
    private SyncAnalysis syncAnalysis;
    private List<EmotionTimelineEntry> emotionTimeline;

    // Nested class for sync analysis
    public static class SyncAnalysis {
        private double score;
        private String interpretation;
        private String details;

        // Getters and Setters
        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public String getInterpretation() {
            return interpretation;
        }

        public void setInterpretation(String interpretation) {
            this.interpretation = interpretation;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }

    // Nested class for emotion timeline entries
    public static class EmotionTimelineEntry {
        private double timestamp;
        private String emotion;
        private double confidence;

        // Getters and Setters
        public double getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(double timestamp) {
            this.timestamp = timestamp;
        }

        public String getEmotion() {
            return emotion;
        }

        public void setEmotion(String emotion) {
            this.emotion = emotion;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }

    // Getters and Setters for main class
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public void setTotalFrames(int totalFrames) {
        this.totalFrames = totalFrames;
    }

    public int getProcessedFrames() {
        return processedFrames;
    }

    public void setProcessedFrames(int processedFrames) {
        this.processedFrames = processedFrames;
    }

    public int getTotalEmotionsDetected() {
        return totalEmotionsDetected;
    }

    public void setTotalEmotionsDetected(int totalEmotionsDetected) {
        this.totalEmotionsDetected = totalEmotionsDetected;
    }

    public Map<String, Double> getEmotionDistribution() {
        return emotionDistribution;
    }

    public void setEmotionDistribution(Map<String, Double> emotionDistribution) {
        this.emotionDistribution = emotionDistribution;
    }

    public double getGenreMatchScore() {
        return genreMatchScore;
    }

    public void setGenreMatchScore(double genreMatchScore) {
        this.genreMatchScore = genreMatchScore;
    }

    public String getPrimaryEmotion() {
        return primaryEmotion;
    }

    public void setPrimaryEmotion(String primaryEmotion) {
        this.primaryEmotion = primaryEmotion;
    }

    public List<String> getExpectedEmotions() {
        return expectedEmotions;
    }

    public void setExpectedEmotions(List<String> expectedEmotions) {
        this.expectedEmotions = expectedEmotions;
    }

    public SyncAnalysis getSyncAnalysis() {
        return syncAnalysis;
    }

    public void setSyncAnalysis(SyncAnalysis syncAnalysis) {
        this.syncAnalysis = syncAnalysis;
    }

    public List<EmotionTimelineEntry> getEmotionTimeline() {
        return emotionTimeline;
    }

    public void setEmotionTimeline(List<EmotionTimelineEntry> emotionTimeline) {
        this.emotionTimeline = emotionTimeline;
    }

    @Override
    public String toString() {
        StringBuilder report = new StringBuilder();
        report.append("Emotion Analysis Report\n");
        report.append("=====================\n");
        report.append(String.format("Genre: %s\n", genre));
        report.append(String.format("Duration: %.2f seconds\n", duration));
        report.append(String.format("Total Frames: %d\n", totalFrames));
        report.append(String.format("Processed Frames: %d\n", processedFrames));
        report.append(String.format("Total Emotions Detected: %d\n", totalEmotionsDetected));
        report.append(String.format("Primary Emotion: %s\n", primaryEmotion));
        report.append(String.format("Genre Match Score: %.3f\n", genreMatchScore));
        
        report.append("\nEmotion Distribution:\n");
        if (emotionDistribution != null) {
            emotionDistribution.forEach((emotion, percentage) -> 
                report.append(String.format("%s: %.2f%%\n", emotion, percentage)));
        }

        report.append("\nSync Analysis:\n");
        if (syncAnalysis != null) {
            report.append(String.format("Score: %.3f\n", syncAnalysis.getScore()));
            report.append(String.format("Interpretation: %s\n", syncAnalysis.getInterpretation()));
            report.append(String.format("Details: %s\n", syncAnalysis.getDetails()));
        }

        return report.toString();
    }
}