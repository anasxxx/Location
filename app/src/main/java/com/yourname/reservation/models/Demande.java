package com.yourname.reservation.models;

import java.util.Date;

public class Demande {
    private String id;
    private String clientId;
    private String offreId;
    private Date date;
    private String status;
    private String situation_familiale;
    private int nombre_enfants;
    private String situation_professionnelle;
    private double revenu;
    private String message;

    public Demande() {}

    public Demande(String clientId, String offreId, String situation_familiale,
                  int nombre_enfants, String situation_professionnelle,
                  double revenu, String message) {
        this.clientId = clientId;
        this.offreId = offreId;
        this.date = new Date();
        this.status = "en_attente";
        this.situation_familiale = situation_familiale;
        this.nombre_enfants = nombre_enfants;
        this.situation_professionnelle = situation_professionnelle;
        this.revenu = revenu;
        this.message = message;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getOffreId() { return offreId; }
    public void setOffreId(String offreId) { this.offreId = offreId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSituation_familiale() { return situation_familiale; }
    public void setSituation_familiale(String situation_familiale) {
        this.situation_familiale = situation_familiale;
    }

    public int getNombre_enfants() { return nombre_enfants; }
    public void setNombre_enfants(int nombre_enfants) {
        this.nombre_enfants = nombre_enfants;
    }

    public String getSituation_professionnelle() { return situation_professionnelle; }
    public void setSituation_professionnelle(String situation_professionnelle) {
        this.situation_professionnelle = situation_professionnelle;
    }

    public double getRevenu() { return revenu; }
    public void setRevenu(double revenu) { this.revenu = revenu; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
} 