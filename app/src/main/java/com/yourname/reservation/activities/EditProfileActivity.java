package com.yourname.reservation.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourname.reservation.R;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    private TextInputEditText nameInput;
    private TextInputEditText phoneInput;
    private TextInputEditText agencyInput;
    private MaterialButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_edit_profile);

            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            userId = auth.getCurrentUser().getUid();

            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Modifier le profil");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            // Initialize views
            initializeViews();
            
            // Load current profile data
            loadProfileData();
            
            // Set up click listeners
            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        agencyInput = findViewById(R.id.agencyInput);
        saveButton = findViewById(R.id.saveButton);
    }

    private void loadProfileData() {
        db.collection("agents").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    nameInput.setText(documentSnapshot.getString("name"));
                    phoneInput.setText(documentSnapshot.getString("phone"));
                    agencyInput.setText(documentSnapshot.getString("agency"));
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading profile data: ", e);
                Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
            });
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String agency = agencyInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Le nom est requis");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("agency", agency);

        db.collection("agents").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating profile: ", e);
                Toast.makeText(this, "Erreur lors de la mise à jour du profil", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 