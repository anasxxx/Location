package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourname.reservation.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int SPLASH_DELAY = 1500; // 1.5 seconds

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        progressBar = findViewById(R.id.progressBar);

        // Launch with delay to show splash screen
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserAndRedirect, SPLASH_DELAY);
    }

    private void checkUserAndRedirect() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, go to login screen
            goToLoginScreen();
            return;
        }

        // User is logged in, check user type in Firestore
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("type");
                        if ("agent".equals(userType)) {
                            goToAgentDashboard();
                        } else if ("client".equals(userType)) {
                            goToClientDashboard();
                        } else {
                            Log.w(TAG, "Unknown user type: " + userType);
                            goToLoginScreen();
                        }
                    } else {
                        Log.w(TAG, "User document doesn't exist");
                        mAuth.signOut();
                        goToLoginScreen();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user type", e);
                    goToLoginScreen();
                });
    }

    private void goToLoginScreen() {
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToAgentDashboard() {
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(MainActivity.this, AgentDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToClientDashboard() {
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(MainActivity.this, ClientDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 