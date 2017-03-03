package com.tokenbrowser.model.network;

public class App {
    private int review_count;
    private boolean featured;
    private Double reputation_score;
    private String name;
    private String description;
    private String token_id;
    private Manifest manifest;

    public int getReviewCount() {
        return review_count;
    }

    public boolean isFeatured() {
        return featured;
    }

    public Double getReputationScore() {
        return reputation_score;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTokenId() {
        return token_id;
    }

    public Manifest getManifest() {
        return manifest;
    }
}
