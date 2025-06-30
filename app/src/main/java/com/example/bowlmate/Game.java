package com.example.bowlmate;

public class Game {
    private String id;
    private int score;
    private int totalStrikes;
    private int totalSpares;
    private String notes;

    public Game() {} // Firestore needs empty constructor

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalStrikes() { return totalStrikes; }
    public void setTotalStrikes(int totalStrikes) { this.totalStrikes = totalStrikes; }

    public int getTotalSpares() { return totalSpares; }
    public void setTotalSpares(int totalSpares) { this.totalSpares = totalSpares; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}