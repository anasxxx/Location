package com.yourname.reservation.models;

import androidx.annotation.Keep;

@Keep
public class Chat {
    private String messageId;
    private String senderId;   // from
    private String receiverId; // to
    private String message;    // text
    private long timestamp;    // time
    private String reservationId;

    // Required empty constructor for Firebase
    public Chat() {
        this.timestamp = System.currentTimeMillis();
    }

    public Chat(String senderId, String receiverId, String message, long timestamp, String reservationId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.reservationId = reservationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }
    
    @Override
    public String toString() {
        return "Chat{" +
                "messageId='" + messageId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", reservationId='" + reservationId + '\'' +
                '}';
    }
}
