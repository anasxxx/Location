package com.yourname.reservation.models;

import java.util.ArrayList;
import java.util.List;

public class Client extends User {
    private List<Demande> demandes;
    private List<Evaluation> evaluations;
    private List<String> listeFavoris;

    public Client() {
        super();
        this.demandes = new ArrayList<>();
        this.evaluations = new ArrayList<>();
        this.listeFavoris = new ArrayList<>();
    }

    public Client(String email, String password, String nom, String prenom,
                 int age, String ville, String adresse, String telephone) {
        super(email, password, nom, prenom, age, ville, adresse, telephone);
        this.demandes = new ArrayList<>();
        this.evaluations = new ArrayList<>();
        this.listeFavoris = new ArrayList<>();
    }

    // Getters and Setters
    public List<Demande> getDemandes() { return demandes; }
    public void setDemandes(List<Demande> demandes) { this.demandes = demandes; }

    public List<Evaluation> getEvaluations() { return evaluations; }
    public void setEvaluations(List<Evaluation> evaluations) { this.evaluations = evaluations; }

    public List<String> getListeFavoris() { return listeFavoris; }
    public void setListeFavoris(List<String> listeFavoris) { this.listeFavoris = listeFavoris; }

    // Helper methods
    public void addDemande(Demande demande) {
        if (demandes == null) demandes = new ArrayList<>();
        demandes.add(demande);
    }

    public void addEvaluation(Evaluation evaluation) {
        if (evaluations == null) evaluations = new ArrayList<>();
        evaluations.add(evaluation);
    }

    public void addFavori(String offreId) {
        if (listeFavoris == null) listeFavoris = new ArrayList<>();
        listeFavoris.add(offreId);
    }
} 