package com.yourname.reservation.models;

import java.util.Date;

public class Evaluation {
    private String id;
    private String clientId;
    private String agentId;
    private String offreId;
    private Date date;
    private int score;
    private String commentaire;

    public Evaluation() {}

    public Evaluation(String clientId, String agentId, String offreId,
                     int score, String commentaire) {
        this.clientId = clientId;
        this.agentId = agentId;
        this.offreId = offreId;
        this.date = new Date();
        this.score = score;
        this.commentaire = commentaire;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getOffreId() { return offreId; }
    public void setOffreId(String offreId) { this.offreId = offreId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
} 