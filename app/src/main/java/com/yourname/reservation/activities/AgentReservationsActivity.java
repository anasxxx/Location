package com.yourname.reservation.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class AgentReservationsActivity extends AppCompatActivity {
    private static final String TAG = "AgentReservationsActivity";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView reservationsRecyclerView;
    private ReservationAdapter reservationAdapter;
    private View progressBar;
    private View emptyView;
    private List<Reservation> reservations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_reservations);

        try {
            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            if (auth.getCurrentUser() == null) {
                Log.e(TAG, "No user logged in");
                Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize views
            initializeViews();

            // Load reservations
            loadReservations();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
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
                getSupportActionBar().setTitle("Demandes de réservation");
            }

            // Initialize RecyclerView
            reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);
            reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            reservations = new ArrayList<>();
            reservationAdapter = new ReservationAdapter(this, reservations, true); // true for agent view
            reservationsRecyclerView.setAdapter(reservationAdapter);

            // Initialize other views
            progressBar = findViewById(R.id.progressBar);
            emptyView = findViewById(R.id.emptyView);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    private void loadReservations() {
        try {
            String agentId = auth.getCurrentUser().getUid();
            Log.d(TAG, "Loading reservations for agent: " + agentId);
            progressBar.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            db.collection("reservations")
                .whereEqualTo("agentId", agentId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    try {
                        if (error != null) {
                            Log.e(TAG, "Error listening for reservations", error);
                            Toast.makeText(this, "Erreur lors du chargement des réservations", 
                                         Toast.LENGTH_SHORT).show();
                            return;
                        }

                        progressBar.setVisibility(View.GONE);
                        reservations.clear();

                        if (value != null && !value.isEmpty()) {
                            for (QueryDocumentSnapshot document : value) {
                                try {
                                    Reservation reservation = document.toObject(Reservation.class);
                                    if (reservation != null) {
                                        reservation.setId(document.getId());
                                        reservations.add(reservation);
                                        Log.d(TAG, "Added reservation: " + reservation.toString());
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing reservation document", e);
                                }
                            }
                            reservationAdapter.notifyDataSetChanged();
                            updateEmptyView();
                        } else {
                            Log.d(TAG, "No reservations found");
                            updateEmptyView();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing reservations", e);
                        Toast.makeText(this, "Erreur lors du traitement des réservations", 
                                     Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error loading reservations", e);
            Toast.makeText(this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            updateEmptyView();
        }
    }

    private void updateEmptyView() {
        if (reservations.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            reservationsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            reservationsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
