package com.yourname.reservation.models;

/**
 * Model class to represent a chat contact (agent or client) with reservation details
 */
public class ChatContact {
    private String userId;
    private String name;
    private String propertyTitle;
    private String reservationId;

    public ChatContact() {
        // Required empty constructor for Firebase
    }

    public ChatContact(String userId, String name, String propertyTitle, String reservationId) {
        this.userId = userId;
        this.name = name;
        this.propertyTitle = propertyTitle;
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }
} 