package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourname.reservation.R;

public class AgentDashboardActivity extends AppCompatActivity {
    private static final String TAG = "AgentDashboardActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    private TextView agentNameText;
    private TextView agentEmailText;
    private TextView propertiesCountText;
    private TextView reservationsCountText;
    private ImageButton editProfileButton;
    private MaterialButton addPropertyButton;
    private MaterialButton managePropertiesButton;
    private MaterialButton viewReservationsButton;
    private MaterialButton chatButton;
    private MaterialButton logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_agent_dashboard);

            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();
            
            if (currentUser == null) {
                // User not logged in, redirect to login
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            
            userId = currentUser.getUid();
            Log.d(TAG, "Agent ID: " + userId);

            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // Initialize views
            initializeViews();
            
            // Load agent data
            loadAgentData();
            
            // Set up click listeners
            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        try {
            agentNameText = findViewById(R.id.agentNameText);
            agentEmailText = findViewById(R.id.agentEmailText);
            propertiesCountText = findViewById(R.id.propertiesCountText);
            reservationsCountText = findViewById(R.id.reservationsCountText);
            editProfileButton = findViewById(R.id.editProfileButton);
            addPropertyButton = findViewById(R.id.addPropertyButton);
            managePropertiesButton = findViewById(R.id.managePropertiesButton);
            viewReservationsButton = findViewById(R.id.viewReservationsButton);
            chatButton = findViewById(R.id.chatButton);
            logoutButton = findViewById(R.id.logoutButton);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    private void setupClickListeners() {
        try {
            editProfileButton.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
            addPropertyButton.setOnClickListener(v -> startActivity(new Intent(this, AddPropertyActivity.class)));
            managePropertiesButton.setOnClickListener(v -> startActivity(new Intent(this, ManagePropertiesActivity.class)));
            
            viewReservationsButton.setOnClickListener(v -> {
                Log.d(TAG, "Starting ReservationsListActivity for agent: " + userId);
                Intent intent = new Intent(this, ReservationsListActivity.class);
                intent.putExtra("agentId", userId); // Pass the agent ID
                startActivity(intent);
            });

            chatButton.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "Chat button clicked, opening chat selection for agent: " + userId);
                    Intent intent = new Intent(this, ChatSelectionActivity.class);
                    intent.putExtra("userType", "agent");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ChatSelectionActivity", e);
                    Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                }
            });

            logoutButton.setOnClickListener(v -> {
                auth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
            throw e;
        }
    }

    private void loadAgentData() {
        try {
            // Load agent profile data
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("nom");
                        String email = documentSnapshot.getString("email");
                        
                        Log.d(TAG, "Loaded agent data - Raw doc data: " + documentSnapshot.getData());
                        Log.d(TAG, "Name field value: " + name + ", Email field value: " + email);
                        
                        if (name == null || name.isEmpty()) {
                            name = "Agent";
                            Log.d(TAG, "Using default agent name");
                        }
                        
                        agentNameText.setText(name);
                        agentEmailText.setText(email != null ? email : "");
                        Log.d(TAG, "Set agent UI fields - Name: " + name + ", Email: " + email);
                    } else {
                        Log.e(TAG, "Agent document does not exist for ID: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading agent data", e);
                    Toast.makeText(this, R.string.error_loading_data, Toast.LENGTH_SHORT).show();
                });

            // Load properties count
            db.collection("properties")
                .whereEqualTo("agentId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    propertiesCountText.setText(String.valueOf(count));
                    Log.d(TAG, "Properties count: " + count);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading properties count", e));

            // Load reservations count
            db.collection("reservations")
                .whereEqualTo("agentId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    reservationsCountText.setText(String.valueOf(count));
                    Log.d(TAG, "Reservations count: " + count);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading reservations count", e));
        } catch (Exception e) {
            Log.e(TAG, "Error in loadAgentData", e);
            Toast.makeText(this, R.string.error_loading_data, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Refresh data when returning to the dashboard
            loadAgentData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }
}