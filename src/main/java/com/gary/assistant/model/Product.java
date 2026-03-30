package com.gary.assistant.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_platform_product_id", columnList = "platform,platformProductId"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(nullable = false)
    private String platformProductId;

    @Column(nullable = false)
    private String url;

    @Embedded
    private Price price;

    @Embedded
    private Rating rating;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private boolean available;

    protected Product() {
    }

    public Product(String name, Platform platform, String platformProductId, String url) {
        this.name = name;
        this.platform = platform;
        this.platformProductId = platformProductId;
        this.url = url;
        this.available = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void updatePrice(Price newPrice) {
        this.price = newPrice;
        this.updatedAt = Instant.now();
    }

    public void updateRating(Rating newRating) {
        this.rating = newRating;
        this.updatedAt = Instant.now();
    }

    public void markUnavailable() {
        this.available = false;
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getPlatformProductId() {
        return platformProductId;
    }

    public String getUrl() {
        return url;
    }

    public Price getPrice() {
        return price;
    }

    public Rating getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isAvailable() {
        return available;
    }

    // Setters for JPA
    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
               "name='" + name + '\'' +
               ", platform=" + platform +
               ", price=" + price +
               ", rating=" + rating +
               '}';
    }
}
