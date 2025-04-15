package com.yourname.reservation.models;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Offre {
    private String id;
    private String title;
    private String description;
    private String userId;
    private double price;
    private String location;
    private int rooms;
    private double superficie;
    private List<String> images;
    private String status;
    @ServerTimestamp
    private Timestamp createdAt;

    // Required empty constructor for Firestore
    public Offre() {
        this.images = new ArrayList<>();
    }

    public Offre(String title, String description, String userId, double price, String location, int rooms) {
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.price = price;
        this.location = location;
        this.rooms = rooms;
        this.status = "available";
        this.images = new ArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUserId() { return userId; }
    public double getPrice() { return price; }
    public String getLocation() { return location; }
    public int getRooms() { return rooms; }
    public double getSuperficie() { return superficie; }
    public List<String> getImages() { return images; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setPrice(double price) { this.price = price; }
    public void setLocation(String location) { this.location = location; }
    public void setRooms(int rooms) { this.rooms = rooms; }
    public void setSuperficie(double superficie) { this.superficie = superficie; }
    public void setImages(List<String> images) { this.images = images; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public void addImage(String imageUrl) {
        if (images == null) images = new ArrayList<>();
        images.add(imageUrl);
    }
} 