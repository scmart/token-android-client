package com.tokenbrowser.model.local;

public class Review {
    private int rating;
    private String reviewee;
    private String review;

    public Review setRating(int rating) {
        this.rating = rating;
        return this;
    }

    public Review setReviewee(String reviewee) {
        this.reviewee = reviewee;
        return this;
    }

    public Review setReview(String review) {
        this.review = review;
        return this;
    }
}
