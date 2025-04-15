package com.yourname.reservation.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Reservation {
    @DocumentId
    private String id;
    private String propertyId;
    private String userId;
    private String agentId;  
    private String status;
    private String message;
    private Timestamp visitDate;
    
    @ServerTimestamp
    private Timestamp timestamp;  

    // Required empty constructor for Firestore
    public Reservation() {
        this.status = "pending";
        this.message = "";
        this.timestamp = Timestamp.now();
        // Initialize other fields to avoid null
        this.propertyId = "";
        this.userId = "";
        this.agentId = "";
    }

    public Reservation(String propertyId, String userId, String agentId, Timestamp visitDate, String message) {
        this.propertyId = propertyId != null ? propertyId : "";
        this.userId = userId != null ? userId : "";
        this.agentId = agentId != null ? agentId : "";
        this.visitDate = visitDate != null ? visitDate : Timestamp.now();
        this.message = message != null ? message : "";
        this.status = "pending";
        this.timestamp = Timestamp.now();
    }

    // Additional constructor for Date parameter
    public Reservation(String propertyId, String userId, String agentId, Date visitDate, String message) {
        this(propertyId, userId, agentId, new Timestamp(visitDate), message);
    }

    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPropertyId() {
        return propertyId != null ? propertyId : "";
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAgentId() {
        return agentId != null ? agentId : "";
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getStatus() {
        return status != null ? status : "pending";
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "pending";
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message != null ? message : "";
    }

    public Timestamp getVisitDate() {
        return visitDate != null ? visitDate : Timestamp.now();
    }

    public void setVisitDate(Timestamp visitDate) {
        this.visitDate = visitDate;
    }

    public Timestamp getTimestamp() {
        return timestamp != null ? timestamp : Timestamp.now();
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + (id != null ? id : "null") + '\'' +
                ", propertyId='" + (propertyId != null ? propertyId : "null") + '\'' +
                ", userId='" + (userId != null ? userId : "null") + '\'' +
                ", agentId='" + (agentId != null ? agentId : "null") + '\'' +
                ", status='" + (status != null ? status : "null") + '\'' +
                ", message='" + (message != null ? message : "null") + '\'' +
                ", visitDate=" + (visitDate != null ? visitDate.toDate() : "null") +
                '}';
    }
}