package com.yourname.reservation.models;

import java.util.Date;

public class Abonnement {
    private String id;
    private String agentId;
    private Date dateDebut;
    private Date dateFin;
    private String type;
    private double mensualite;
    private String status;

    public Abonnement() {}

    public Abonnement(String agentId, String type, double mensualite) {
        this.agentId = agentId;
        this.type = type;
        this.mensualite = mensualite;
        this.dateDebut = new Date();
        this.status = "actif";
        // Set end date to 30 days from now
        this.dateFin = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }

    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getMensualite() { return mensualite; }
    public void setMensualite(double mensualite) { this.mensualite = mensualite; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Helper methods
    public boolean isActive() {
        Date now = new Date();
        return status.equals("actif") && now.after(dateDebut) && now.before(dateFin);
    }

    public void renouveler() {
        this.dateDebut = new Date();
        this.dateFin = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
        this.status = "actif";
    }
} 