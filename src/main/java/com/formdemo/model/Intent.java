package com.formdemo.model;

public class Intent {
    private String type; // HOTEL, FLIGHT, TRAIN, etc.
    private double confidence; // 0.0 to 1.0
    private boolean needsClarification;
    private String clarificationQuestion;
    private String extractedName;
    private String extractedDestination;
    private String extractedDate;

    public Intent() {
    }

    public Intent(String type, double confidence, boolean needsClarification, String clarificationQuestion) {
        this.type = type;
        this.confidence = confidence;
        this.needsClarification = needsClarification;
        this.clarificationQuestion = clarificationQuestion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public boolean isNeedsClarification() {
        return needsClarification;
    }

    public void setNeedsClarification(boolean needsClarification) {
        this.needsClarification = needsClarification;
    }

    public String getClarificationQuestion() {
        return clarificationQuestion;
    }

    public void setClarificationQuestion(String clarificationQuestion) {
        this.clarificationQuestion = clarificationQuestion;
    }

    public String getExtractedName() {
        return extractedName;
    }

    public void setExtractedName(String extractedName) {
        this.extractedName = extractedName;
    }

    public String getExtractedDestination() {
        return extractedDestination;
    }

    public void setExtractedDestination(String extractedDestination) {
        this.extractedDestination = extractedDestination;
    }

    public String getExtractedDate() {
        return extractedDate;
    }

    public void setExtractedDate(String extractedDate) {
        this.extractedDate = extractedDate;
    }
}
