package com.yourname.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourname.reservation.activities.AgentDashboardActivity;
import com.yourname.reservation.activities.ClientDashboardActivity;
import com.yourname.reservation.activities.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Check user type and redirect accordingly
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String userType = documentSnapshot.getString("type");
                        Intent intent;
                        
                        if ("agent".equals(userType)) {
                            intent = new Intent(MainActivity.this, AgentDashboardActivity.class);
                        } else {
                            intent = new Intent(MainActivity.this, ClientDashboardActivity.class);
                        }
                        
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Error checking user type: " + e.getMessage());
                        // On error, redirect to login
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    });
        } else {
            // No user is signed in, redirect to login
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }
}