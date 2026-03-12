package com.gary.assistant.model;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Rating {
    private Double score;
    private Integer reviewCount;

    protected Rating() {
    }

    public Rating(Double score, Integer reviewCount) {
        if (score != null && (score < 0 || score > 5)) {
            throw new IllegalArgumentException("Rating score must be between 0 and 5");
        }
        this.score = score;
        this.reviewCount = reviewCount != null ? reviewCount : 0;
    }

    public Double getScore() {
        return score;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public boolean isReliable() {
        return reviewCount != null && reviewCount >= 10;
    }

    public int compareReliability(Rating other) {
        if (this.reviewCount == null || other.reviewCount == null) {
            return 0;
        }
        return this.reviewCount.compareTo(other.reviewCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return Objects.equals(score, rating.score) &&
               Objects.equals(reviewCount, rating.reviewCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, reviewCount);
    }

    @Override
    public String toString() {
        return score != null ? score + "/5 (" + reviewCount + " reviews)" : "No rating";
    }
}
