package com.yourname.reservation.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yourname.reservation.R;
import com.yourname.reservation.adapters.ChatAdapter;
import com.yourname.reservation.models.Chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<Chat> chatMessages;
    
    private String currentUserId;
    private String otherUserId;
    private String reservationId;
    
    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference chatRef;
    private ChildEventListener chatListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Debugging information
        Log.d(TAG, "===== CHAT ACTIVITY STARTED =====");
        
        // Get data from intent
        currentUserId = getIntent().getStringExtra("currentUserId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        reservationId = getIntent().getStringExtra("reservationId");
        String otherUserName = getIntent().getStringExtra("otherUserName");

        Log.d(TAG, "Chat parameters - currentUserId: " + currentUserId + 
                ", otherUserId: " + otherUserId + 
                ", reservationId: " + reservationId);

        if (currentUserId == null || otherUserId == null || reservationId == null) {
            Toast.makeText(this, "Erreur: informations manquantes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Use a default name if otherUserName is null
        if (otherUserName == null || otherUserName.isEmpty()) {
            otherUserName = "l'utilisateur";
        }

        // Initialize Firebase Database with public access
        try {
            database = FirebaseDatabase.getInstance();
            // Create a secure chat reference path with reservation ID
            String chatPath = "public_messages/" + reservationId;
            Log.d(TAG, "Using chat path: " + chatPath);
            chatRef = database.getReference(chatPath);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
            Toast.makeText(this, "Erreur d'initialisation Firebase", Toast.LENGTH_SHORT).show();
        }
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chat avec " + otherUserName);
        }

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Setup RecyclerView
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Setup send button
        sendButton.setOnClickListener(view -> {
            String text = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                // Visual feedback
                Toast.makeText(ChatActivity.this, "Envoi en cours...", Toast.LENGTH_SHORT).show();
                
                // Disable controls
                sendButton.setEnabled(false);
                messageInput.setEnabled(false);
                
                // Send message
                sendMessage(text);
            }
        });
        
        // Setup test connection
        testConnection();
        
        // Load messages
        loadMessages();
    }
    
    private void testConnection() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                Log.d(TAG, "Firebase connection: " + (connected ? "CONNECTED" : "DISCONNECTED"));
                
                if (!connected) {
                    Toast.makeText(ChatActivity.this, "Problème de connexion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Connection listener cancelled", error.toException());
            }
        });
    }
    
    private void sendMessage(String messageText) {
        try {
            // Get a new push ID
            String messageId = chatRef.push().getKey();
            
            if (messageId == null) {
                Log.e(TAG, "Failed to generate push key");
                resetInputControls();
                return;
            }
            
            // Create a simple message map with minimal data
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("from", currentUserId);
            messageMap.put("text", messageText);
            messageMap.put("time", System.currentTimeMillis());
            
            // Write directly to the database
            chatRef.child(messageId).setValue(messageMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Message sent successfully");
                    messageInput.setText("");
                    resetInputControls();
                    Toast.makeText(ChatActivity.this, "Message envoyé", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send message", e);
                    Toast.makeText(ChatActivity.this, "Échec de l'envoi", Toast.LENGTH_SHORT).show();
                    resetInputControls();
                });
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            resetInputControls();
        }
    }
    
    private void resetInputControls() {
        runOnUiThread(() -> {
            messageInput.setEnabled(true);
            sendButton.setEnabled(true);
            messageInput.requestFocus();
        });
    }
    
    private void loadMessages() {
        chatListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                try {
                    Log.d(TAG, "Message received: " + snapshot.getKey());
                    
                    // Extract data
                    String sender = snapshot.child("from").getValue(String.class);
                    String text = snapshot.child("text").getValue(String.class);
                    Object timeObj = snapshot.child("time").getValue();
                    
                    long timestamp = 0;
                    if (timeObj instanceof Long) {
                        timestamp = (Long) timeObj;
                    }
                    
                    // Create a message if we have the minimum required data
                    if (sender != null && text != null) {
                        String receiver = sender.equals(currentUserId) ? otherUserId : currentUserId;
                        
                        Chat message = new Chat(sender, receiver, text, timestamp, reservationId);
                        message.setMessageId(snapshot.getKey());
                        
                        // Add to the list and update UI
                        chatMessages.add(message);
                        chatAdapter.notifyDataSetChanged();
                        
                        // Scroll to bottom
                        chatRecyclerView.post(() -> {
                            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing message", e);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Log.d(TAG, "Message changed: " + snapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Message removed: " + snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                Log.d(TAG, "Message moved: " + snapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error", error.toException());
            }
        };
        
        // Add the listener
        chatRef.addChildEventListener(chatListener);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Remove listener
        if (chatListener != null && chatRef != null) {
            chatRef.removeEventListener(chatListener);
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
}
