package com.yourname.reservation.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Property implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @DocumentId
    private String id;
    private String title;
    private String description;
    private String location;
    private int price;
    private String agentId;
    private List<String> imageUrls;
    private String type;
    private int bedrooms;
    private int bathrooms;
    private boolean available;

    // Required empty constructor for Firestore
    public Property() {
        this.imageUrls = new ArrayList<>();
        this.available = true;
        this.type = "";
        this.title = "";
        this.description = "";
        this.location = "";
    }

    // Parameterized constructor
    public Property(String title, String description, String location, int price, 
                   String type, int bedrooms, int bathrooms, String agentId) {
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.location = location != null ? location : "";
        this.price = price;
        this.type = type != null ? type : "";
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.agentId = agentId != null ? agentId : "";
        this.available = true;
        this.imageUrls = new ArrayList<>();
    }

    @Exclude
    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public String getLocation() {
        return location != null ? location : "";
    }

    public void setLocation(String location) {
        this.location = location != null ? location : "";
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getAgentId() {
        return agentId != null ? agentId : "";
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId != null ? agentId : "";
    }

    public List<String> getImageUrls() {
        return imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
    }

    public String getType() {
        return type != null ? type : "";
    }

    public void setType(String type) {
        this.type = type != null ? type : "";
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(int bathrooms) {
        this.bathrooms = bathrooms;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Property{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", price=" + price +
                ", type='" + type + '\'' +
                ", bedrooms=" + bedrooms +
                ", bathrooms=" + bathrooms +
                ", available=" + available +
                '}';
    }
}