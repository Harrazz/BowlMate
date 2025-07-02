package com.example.bowlmate;

import com.google.firebase.Timestamp;

public class Game {
    private String id;
    private String title;
    private int score;
    private int totalStrikes;
    private int totalSpares;
    private String notes;
    private Timestamp timestamp;

    public Game() {} // Required for Firestore deserialization

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalStrikes() {
        return totalStrikes;
    }

    public void setTotalStrikes(int totalStrikes) {
        this.totalStrikes = totalStrikes;
    }

    public int getTotalSpares() {
        return totalSpares;
    }

    public void setTotalSpares(int totalSpares) {
        this.totalSpares = totalSpares;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getTimestamp() { // Getter for Firebase Timestamp
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) { // Setter for Firebase Timestamp
        this.timestamp = timestamp;
    }
}