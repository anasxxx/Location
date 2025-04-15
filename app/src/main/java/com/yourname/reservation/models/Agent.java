package com.yourname.reservation.models;

import java.util.ArrayList;
import java.util.List;

public class Agent extends User {
    private String address_agence;
    private String ville_agence;
    private String nom_agence;
    private String email_agence;
    private List<Offre> offres;
    private List<Abonnement> abonnements;

    public Agent() {
        super();
        this.offres = new ArrayList<>();
        this.abonnements = new ArrayList<>();
    }

    public Agent(String email, String password, String nom, String prenom,
                int age, String ville, String adresse, String telephone,
                String address_agence, String ville_agence, String nom_agence,
                String email_agence) {
        super(email, password, nom, prenom, age, ville, adresse, telephone);
        this.address_agence = address_agence;
        this.ville_agence = ville_agence;
        this.nom_agence = nom_agence;
        this.email_agence = email_agence;
        this.offres = new ArrayList<>();
        this.abonnements = new ArrayList<>();
    }

    // Getters and Setters
    public String getAddress_agence() { return address_agence; }
    public void setAddress_agence(String address_agence) { this.address_agence = address_agence; }

    public String getVille_agence() { return ville_agence; }
    public void setVille_agence(String ville_agence) { this.ville_agence = ville_agence; }

    public String getNom_agence() { return nom_agence; }
    public void setNom_agence(String nom_agence) { this.nom_agence = nom_agence; }

    public String getEmail_agence() { return email_agence; }
    public void setEmail_agence(String email_agence) { this.email_agence = email_agence; }

    public List<Offre> getOffres() { return offres; }
    public void setOffres(List<Offre> offres) { this.offres = offres; }

    public List<Abonnement> getAbonnements() { return abonnements; }
    public void setAbonnements(List<Abonnement> abonnements) { this.abonnements = abonnements; }

    // Helper methods
    public void addOffre(Offre offre) {
        if (offres == null) offres = new ArrayList<>();
        offres.add(offre);
    }

    public void addAbonnement(Abonnement abonnement) {
        if (abonnements == null) abonnements = new ArrayList<>();
        abonnements.add(abonnement);
    }
} 