package com.yourname.reservation.models;

public class User {
    private String email;
    private String password;
    private String nom;
    private String prenom;
    private int age;
    private String ville;
    private String adresse;
    private String telephone;

    // Default constructor
    public User() {}

    // Parameterized constructor
    public User(String email, String password, String nom, String prenom, 
                int age, String ville, String adresse, String telephone) {
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.ville = ville;
        this.adresse = adresse;
        this.telephone = telephone;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
} 