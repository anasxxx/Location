package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yourname.reservation.R;
import com.yourname.reservation.adapters.ChatSelectionAdapter;
import com.yourname.reservation.models.ChatContact;

import java.util.ArrayList;
import java.util.List;

public class ChatSelectionActivity extends AppCompatActivity implements ChatSelectionAdapter.OnChatContactClickListener {
    private static final String TAG = "ChatSelectionActivity";
    
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyView;
    private TextView emptyViewText;
    
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;
    private String userType; // "agent" or "client"
    
    private List<ChatContact> contacts = new ArrayList<>();
    private ChatSelectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selection);
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        db = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();
        userType = getIntent().getStringExtra("userType");
        
        if (userType == null) {
            Log.e(TAG, "userType not provided in intent");
            Toast.makeText(this, "Erreur: type d'utilisateur non spécifié", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Log.d(TAG, "User type: " + userType + ", User ID: " + userId);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            String title = userType.equals("agent") ? "Chats avec clients" : "Chats avec agents";
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Initialize views
        recyclerView = findViewById(R.id.chatContactsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        emptyViewText = findViewById(R.id.emptyViewText);
        
        // Setup RecyclerView
        adapter = new ChatSelectionAdapter(this, contacts, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Load contacts
        loadChatContacts();
    }
    
    private void loadChatContacts() {
        showLoading();
        contacts.clear();
        
        final String statusField = "status";
        final String userIdField = userType.equals("agent") ? "userId" : "agentId";
        final String otherUserIdField = userType.equals("agent") ? "agentId" : "userId";
        
        db.collection("reservations")
            .whereEqualTo(otherUserIdField, userId)
            .whereEqualTo(statusField, "accepted")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " accepted reservations");
                
                if (queryDocumentSnapshots.isEmpty()) {
                    showEmptyView("Aucune réservation acceptée trouvée");
                    return;
                }
                
                final int[] processed = {0};
                final int total = queryDocumentSnapshots.size();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    final String otherUserId = document.getString(userIdField);
                    final String reservationId = document.getId();
                    
                    if (otherUserId == null || reservationId == null) {
                        Log.e(TAG, "Null user ID or reservation ID");
                        processed[0]++;
                        if (processed[0] >= total && contacts.isEmpty()) {
                            showEmptyView("Aucun contact disponible");
                        }
                        continue;
                    }
                    
                    // Get property details for context
                    final String propertyId = document.getString("propertyId");
                    if (propertyId != null) {
                        db.collection("properties")
                            .document(propertyId)
                            .get()
                            .addOnSuccessListener(propertyDoc -> {
                                String propertyTitle = propertyDoc.getString("title");
                                if (propertyTitle == null) propertyTitle = "Propriété non disponible";
                                
                                final String finalPropertyTitle = propertyTitle;
                                
                                // Get other user details
                                db.collection("users")
                                    .document(otherUserId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        String userName = userDoc.getString("nom");
                                        if (userName == null || userName.isEmpty()) {
                                            userName = userType.equals("agent") ? "Client" : "Agent";
                                        }
                                        
                                        // Create contact
                                        ChatContact contact = new ChatContact(
                                            otherUserId,
                                            userName,
                                            finalPropertyTitle,
                                            reservationId
                                        );
                                        
                                        contacts.add(contact);
                                        Log.d(TAG, "Added contact: " + contact.getName() + " for property: " + contact.getPropertyTitle());
                                        
                                        processed[0]++;
                                        if (processed[0] >= total) {
                                            updateUI();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error getting user details", e);
                                        processed[0]++;
                                        if (processed[0] >= total) {
                                            updateUI();
                                        }
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting property details", e);
                                processed[0]++;
                                if (processed[0] >= total) {
                                    updateUI();
                                }
                            });
                    } else {
                        // No property ID, just get user details
                        db.collection("users")
                            .document(otherUserId)
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                String userName = userDoc.getString("nom");
                                if (userName == null || userName.isEmpty()) {
                                    userName = userType.equals("agent") ? "Client" : "Agent";
                                }
                                
                                // Create contact
                                ChatContact contact = new ChatContact(
                                    otherUserId,
                                    userName,
                                    "Réservation sans propriété",
                                    reservationId
                                );
                                
                                contacts.add(contact);
                                Log.d(TAG, "Added contact: " + contact.getName() + " (no property)");
                                
                                processed[0]++;
                                if (processed[0] >= total) {
                                    updateUI();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting user details", e);
                                processed[0]++;
                                if (processed[0] >= total) {
                                    updateUI();
                                }
                            });
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading chat contacts", e);
                showEmptyView("Erreur lors du chargement des contacts");
            });
    }
    
    private void updateUI() {
        runOnUiThread(() -> {
            if (contacts.isEmpty()) {
                showEmptyView("Aucun contact disponible");
            } else {
                hideEmptyView();
                adapter.notifyDataSetChanged();
            }
            hideLoading();
        });
    }
    
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }
    
    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }
    
    private void showEmptyView(String message) {
        emptyViewText.setText(message);
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        hideLoading();
    }
    
    private void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onChatContactClick(ChatContact contact) {
        Log.d(TAG, "Starting chat with: " + contact.getName() + ", ID: " + contact.getUserId() + ", Reservation: " + contact.getReservationId());
        
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("currentUserId", userId);
        intent.putExtra("otherUserId", contact.getUserId());
        intent.putExtra("otherUserName", contact.getName());
        intent.putExtra("reservationId", contact.getReservationId());
        startActivity(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 