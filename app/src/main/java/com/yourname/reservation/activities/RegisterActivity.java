package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourname.reservation.R;
import com.yourname.reservation.models.Agent;
import com.yourname.reservation.models.Client;
import com.yourname.reservation.models.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    
    private TextInputEditText emailInput, passwordInput, nomInput, prenomInput,
            telephoneInput, villeInput, adresseInput;
    private RadioGroup userTypeGroup;
    private MaterialButton registerButton;
    private TextView loginText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        initializeViews();

        // Set click listeners
        registerButton.setOnClickListener(v -> registerUser());
        loginText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        nomInput = findViewById(R.id.nomInput);
        prenomInput = findViewById(R.id.prenomInput);
        telephoneInput = findViewById(R.id.telephoneInput);
        villeInput = findViewById(R.id.villeInput);
        adresseInput = findViewById(R.id.adresseInput);
        userTypeGroup = findViewById(R.id.userTypeGroup);
        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void registerUser() {
        // Get input values
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String nom = nomInput.getText().toString().trim();
        String prenom = prenomInput.getText().toString().trim();
        String telephone = telephoneInput.getText().toString().trim();
        String ville = villeInput.getText().toString().trim();
        String adresse = adresseInput.getText().toString().trim();
        
        // Validate inputs
        if (!validateInputs(email, password, nom, prenom, telephone, ville, adresse)) {
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserDataToFirestore(user, email, nom, prenom, telephone, ville, adresse);
                        } else {
                            Log.e(TAG, "User is null after successful creation");
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this,
                                    "Erreur lors de la création du compte",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        progressBar.setVisibility(View.GONE);
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : 
                                "Erreur inconnue";
                        Toast.makeText(RegisterActivity.this,
                                "Échec de l'inscription: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDataToFirestore(FirebaseUser user, String email, String nom, 
                                       String prenom, String telephone, String ville, 
                                       String adresse) {
        boolean isAgent = userTypeGroup.getCheckedRadioButtonId() == R.id.agentRadio;
        String userType = isAgent ? "agent" : "client";
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("nom", nom);
        userData.put("prenom", prenom);
        userData.put("telephone", telephone);
        userData.put("ville", ville);
        userData.put("adresse", adresse);
        userData.put("type", userType);
        userData.put("createdAt", new Date());
        userData.put("lastLogin", new Date());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved successfully");
                    progressBar.setVisibility(View.GONE);
                    
                    // Redirect based on user type
                    Intent intent;
                    if (isAgent) {
                        intent = new Intent(RegisterActivity.this, AgentDashboardActivity.class);
                    } else {
                        intent = new Intent(RegisterActivity.this, ClientDashboardActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this,
                            "Erreur lors de l'enregistrement des données: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    // Delete the authentication user if we couldn't save their data
                    user.delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User auth deleted after failed data save");
                                } else {
                                    Log.e(TAG, "Error deleting auth user after failed data save", 
                                            task.getException());
                                }
                                mAuth.signOut();
                            });
                });
    }

    private boolean validateInputs(String email, String password, String nom,
                                 String prenom, String telephone, String ville,
                                 String adresse) {
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("L'email est requis");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Email invalide");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Le mot de passe est requis");
            return false;
        }
        if (password.length() < 6) {
            passwordInput.setError("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }
        if (TextUtils.isEmpty(nom)) {
            nomInput.setError("Le nom est requis");
            return false;
        }
        if (TextUtils.isEmpty(prenom)) {
            prenomInput.setError("Le prénom est requis");
            return false;
        }
        if (TextUtils.isEmpty(telephone)) {
            telephoneInput.setError("Le téléphone est requis");
            return false;
        }
        if (TextUtils.isEmpty(ville)) {
            villeInput.setError("La ville est requise");
            return false;
        }
        if (TextUtils.isEmpty(adresse)) {
            adresseInput.setError("L'adresse est requise");
            return false;
        }
        if (userTypeGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Veuillez sélectionner un type d'utilisateur", 
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
} 