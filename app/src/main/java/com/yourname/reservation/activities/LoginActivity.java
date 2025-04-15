package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourname.reservation.R;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private TextView registerText, forgotPasswordText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerText = findViewById(R.id.registerText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        loginButton.setOnClickListener(v -> loginUser());
        registerText.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        forgotPasswordText.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input
        if (!validateInputs(email, password)) {
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserTypeAndRedirect(user);
                        } else {
                            Log.e(TAG, "User is null after successful login");
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this,
                                    "Erreur de connexion",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        progressBar.setVisibility(View.GONE);
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Erreur inconnue";
                        Toast.makeText(LoginActivity.this,
                                "Échec de la connexion: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
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
        return true;
    }

    private void checkUserTypeAndRedirect(FirebaseUser user) {
        Log.d(TAG, "Checking user type for UID: " + user.getUid());
        
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        handleExistingUser(documentSnapshot, user);
                    } else {
                        Log.d(TAG, "User document doesn't exist for UID: " + user.getUid() + ", creating new one");
                        createNewUserDocument(user);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user data: " + e.getMessage(), e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this,
                            "Erreur lors de la récupération des données: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                });
    }

    private void handleExistingUser(DocumentSnapshot documentSnapshot, FirebaseUser user) {
        String userType = documentSnapshot.getString("type");
        Log.d(TAG, "User type found: " + userType);
        
        if (userType == null) {
            Log.w(TAG, "User type is null, creating default user document");
            createNewUserDocument(user);
            return;
        }

        // Update last login time
        db.collection("users").document(user.getUid())
                .update("lastLogin", new Date())
                .addOnFailureListener(e -> Log.w(TAG, "Error updating last login time", e));
        
        Intent intent;
        if ("agent".equals(userType)) {
            intent = new Intent(LoginActivity.this, AgentDashboardActivity.class);
        } else if ("client".equals(userType)) {
            intent = new Intent(LoginActivity.this, ClientDashboardActivity.class);
        } else {
            Log.e(TAG, "Invalid user type: " + userType);
            Toast.makeText(LoginActivity.this,
                    "Type d'utilisateur non valide",
                    Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            progressBar.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.GONE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void createNewUserDocument(FirebaseUser user) {
        Log.d(TAG, "Creating new user document for: " + user.getUid());
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("type", "client"); // Default type
        userData.put("createdAt", new Date());
        userData.put("lastLogin", new Date());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document created successfully");
                    Intent intent = new Intent(LoginActivity.this, ClientDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    progressBar.setVisibility(View.GONE);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user document: " + e.getMessage(), e);
                    Toast.makeText(LoginActivity.this,
                            "Erreur lors de la création du profil: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    mAuth.signOut();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already signed in, checking type");
            checkUserTypeAndRedirect(currentUser);
        }
    }
} 