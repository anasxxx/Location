package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourname.reservation.R;

public class ClientDashboardActivity extends AppCompatActivity {
    private static final String TAG = "ClientDashboardActivity";
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private TextView reservationsCountText;
    private MaterialButton searchPropertiesButton, viewReservationsButton, chatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tableau de bord");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        // Initialize views
        initializeViews();
        
        // Set up click listeners
        setupClickListeners();

        // Load client data
        loadClientData();
    }

    private void initializeViews() {
        reservationsCountText = findViewById(R.id.reservationsCountText);
        searchPropertiesButton = findViewById(R.id.searchPropertiesButton);
        viewReservationsButton = findViewById(R.id.viewReservationsButton);
        chatButton = findViewById(R.id.chatButton);
    }

    private void setupClickListeners() {
        searchPropertiesButton.setOnClickListener(v -> 
            startActivity(new Intent(this, SearchPropertiesActivity.class))
        );

        viewReservationsButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, ClientReservationsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting ClientReservationsActivity", e);
                Toast.makeText(this, "Erreur lors de l'ouverture des réservations", Toast.LENGTH_SHORT).show();
            }
        });

        chatButton.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Chat button clicked, opening chat selection for client: " + userId);
                Intent intent = new Intent(this, ChatSelectionActivity.class);
                intent.putExtra("userType", "client");
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting ChatSelectionActivity", e);
                Toast.makeText(this, "Erreur lors de l'ouverture du chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClientData() {
        try {
            // Load reservations count
            db.collection("reservations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    reservationsCountText.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reservations count", e);
                    reservationsCountText.setText("0");
                });
        } catch (Exception e) {
            Log.e(TAG, "Error loading client data", e);
            Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_client_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}