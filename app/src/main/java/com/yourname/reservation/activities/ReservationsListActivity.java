package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yourname.reservation.R;
import com.yourname.reservation.adapters.ReservationAdapter;
import com.yourname.reservation.models.Reservation;

import java.util.ArrayList;
import java.util.List;

public class ReservationsListActivity extends AppCompatActivity {
    private static final String TAG = "ReservationsListActivity";
    
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyView;
    private ReservationAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Reservation> reservations;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called - reloading reservations");
        loadReservations(); // Reload data when returning to this activity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_reservations_list);

            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            
            // Check connection
            checkFirestoreConnection();

            // Check if user is logged in
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                Log.d(TAG, "No user logged in");
                Toast.makeText(this, getString(R.string.login_required), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            
            Log.d(TAG, "Current user ID: " + currentUser.getUid());

            // Initialize views
            initializeViews();

            // Load reservations
            loadReservations();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(R.string.reservation_requests);
            }

            // Initialize views
            progressBar = findViewById(R.id.progressBar);
            emptyView = findViewById(R.id.emptyView);
            recyclerView = findViewById(R.id.reservationsRecyclerView);

            // Set up RecyclerView
            reservations = new ArrayList<>();
            adapter = new ReservationAdapter(this, reservations, true); // true for agent view
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadReservations() {
        try {
            showLoading();
            
            // Get current user
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                Log.d(TAG, "No user logged in during loadReservations");
                hideLoading();
                showEmptyView();
                return;
            }

            String userId = currentUser.getUid();
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "User ID is null or empty");
                Toast.makeText(this, "Erreur: ID agent non valide", Toast.LENGTH_SHORT).show();
                hideLoading();
                showEmptyView();
                return;
            }
            
            Log.d(TAG, "Loading reservations for agent ID: " + userId);
            
            // Clear existing data first
            reservations.clear();
            adapter.notifyDataSetChanged();
            
            // Forcing a direct query for debugging - get all reservations
            db.collection("reservations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Total reservations in database: " + queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d(TAG, "Reservation in DB: " + doc.getId() + ", agentId: " + doc.getString("agentId"));
                    }
                    
                    // If the database has no reservations at all, show empty view
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No reservations found in database");
                        hideLoading();
                        showEmptyView();
                    }
                });

            // Query reservations specifically for this agent
            Log.d(TAG, "Executing Firestore query for reservations...");
            db.collection("reservations")
                .whereEqualTo("agentId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Number of reservations found for agent " + userId + ": " + queryDocumentSnapshots.size());
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No reservations found for this agent");
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
                                          " AgentId: " + reservation.getAgentId() +
                                          " PropertyId: " + reservation.getPropertyId());
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
                    Toast.makeText(ReservationsListActivity.this, getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show();
                    hideLoading();
                    showEmptyView();
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadReservations", e);
            Toast.makeText(this, getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show();
            hideLoading();
            showEmptyView();
        }
    }

    private void showLoading() {
        runOnUiThread(() -> {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            if (emptyView != null) emptyView.setVisibility(View.GONE);
        });
    }

    private void hideLoading() {
        runOnUiThread(() -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            
            // Always ensure recyclerView is visible when loading is done - we'll hide it in showEmptyView if needed
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showEmptyView() {
        runOnUiThread(() -> {
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
                Log.d(TAG, "Empty view set to VISIBLE");
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
                Log.d(TAG, "RecyclerView set to GONE in showEmptyView");
            }
        });
    }

    private void hideEmptyView() {
        runOnUiThread(() -> {
            if (emptyView != null) emptyView.setVisibility(View.GONE);
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
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