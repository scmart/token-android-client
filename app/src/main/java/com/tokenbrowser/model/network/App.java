package com.tokenbrowser.model.network;

public class App {
    private int review_count;
    private Double reputation_score;
    private String token_id;
    private Custom custom;
    private String payment_address;
    private boolean is_app;

    public int getReviewCount() {
        return review_count;
    }

    public Double getReputationScore() {
        return reputation_score;
    }


    public String getTokenId() {
        return token_id;
    }

    public Custom getCustom() {
        return custom;
    }

    public String getPaymentAddress() {
        return payment_address;
    }

    public boolean isApp() {
        return is_app;
    }
}
