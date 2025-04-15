package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yourname.reservation.R;
import com.yourname.reservation.adapters.ReservationAdapter;
import com.yourname.reservation.models.Reservation;

import java.util.ArrayList;
import java.util.List;

public class ClientReservationsActivity extends AppCompatActivity {
    private static final String TAG = "ClientReservationsAct";
    
    private RecyclerView reservationsRecyclerView;
    private ReservationAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private List<Reservation> reservations;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_reservations);

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
        
        // Check Firestore connection
        Log.d(TAG, "Checking Firestore connection...");
        checkFirestoreConnection();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mes réservations");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        initializeViews();

        // Load reservations
        loadReservations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called - reloading reservations");
        loadReservations(); // Reload data when returning to this activity
    }

    private void initializeViews() {
        try {
            reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);
            progressBar = findViewById(R.id.progressBar);
            emptyView = findViewById(R.id.emptyView);

            // Setup RecyclerView
            reservations = new ArrayList<>();
            adapter = new ReservationAdapter(this, reservations, false); // false for client view
            reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            reservationsRecyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Erreur lors de l'initialisation", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadReservations() {
        try {
            showLoading();
            
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "User ID is null or empty");
                Toast.makeText(this, "Erreur: ID utilisateur non valide", Toast.LENGTH_SHORT).show();
                hideLoading();
                showEmptyView();
                return;
            }
            
            Log.d(TAG, "Loading reservations for client ID: " + userId);
            
            // Clear existing data
            reservations.clear();
            adapter.notifyDataSetChanged();
            
            // Get all reservations for debugging
            db.collection("reservations")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d(TAG, "Total reservations in database: " + snapshot.size());
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Log.d(TAG, "Reservation in DB: " + doc.getId() + 
                              " userId: " + doc.getString("userId") + 
                              " agentId: " + doc.getString("agentId"));
                    }
                });
            
            // Get reservations for this user - use get() for more reliable results
            db.collection("reservations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Number of reservations found for user " + userId + ": " + queryDocumentSnapshots.size());
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No reservations found for this user");
                        hideLoading();
                        showEmptyView();
                        return;
                    }
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Log.d(TAG, "Processing reservation document: " + document.getId());
                            Log.d(TAG, "Document data: " + document.getData());
                            
                            Reservation reservation = document.toObject(Reservation.class);
                            if (reservation != null) {
                                reservation.setId(document.getId());
                                reservations.add(reservation);
                                Log.d(TAG, "Added reservation: " + document.getId() + 
                                          " Status: " + reservation.getStatus() +
                                          " PropertyId: " + reservation.getPropertyId() +
                                          " AgentId: " + reservation.getAgentId());
                            } else {
                                Log.e(TAG, "Failed to convert document to Reservation: " + document.getId());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing reservation document: " + document.getId(), e);
                        }
                    }

                    Log.d(TAG, "Final reservations list size: " + reservations.size());
                    
                    runOnUiThread(() -> {
                        if (reservations.isEmpty()) {
                            Log.d(TAG, "No reservations to display, showing empty view");
                            showEmptyView();
                        } else {
                            Log.d(TAG, "Showing " + reservations.size() + " reservations");
                            adapter.notifyDataSetChanged();
                            hideEmptyView();
                        }
                        hideLoading();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reservations", e);
                    Toast.makeText(ClientReservationsActivity.this, "Erreur lors du chargement des réservations", Toast.LENGTH_SHORT).show();
                    hideLoading();
                    showEmptyView();
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadReservations", e);
            Toast.makeText(this, "Erreur lors du chargement des réservations", Toast.LENGTH_SHORT).show();
            hideLoading();
            showEmptyView();
        }
    }

    private void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        if (reservationsRecyclerView != null) reservationsRecyclerView.setVisibility(View.GONE);
        Log.d(TAG, "showLoading: hiding recyclerView, showing progressBar");
    }

    private void hideLoading() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        // Don't set recyclerview visibility here - let showEmptyView or hideEmptyView handle it
        Log.d(TAG, "hideLoading: hiding progressBar");
    }

    private void showEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
            Log.d(TAG, "showEmptyView: showing emptyView");
        }
        if (reservationsRecyclerView != null) {
            reservationsRecyclerView.setVisibility(View.GONE);
            Log.d(TAG, "showEmptyView: hiding reservationsRecyclerView");
        }
    }

    private void hideEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
            Log.d(TAG, "hideEmptyView: hiding emptyView");
        }
        if (reservationsRecyclerView != null) {
            reservationsRecyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "hideEmptyView: showing reservationsRecyclerView");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Add a diagnostic method
    private void checkFirestoreConnection() {
        db.collection("reservations").limit(1).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Firestore connection successful");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Firestore connection failed", e);
            });
    }
}