package com.example.firestorewriteread;

// Simple POJO for Firestore serialization/deserialization
public class User {
    private String uid;
    private String name;
    private String email;
    private int score;

    // Required no-arg constructor for Firestore
    public User() {}

    public User(String uid, String name, String email, int score) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.score = score;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}