package com.yourname.reservation;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

public class ReservationApp extends Application {
    private static final String TAG = "ReservationApp";

    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Initialize Firebase Realtime Database persistence - only called once here
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase Realtime Database persistence enabled");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase persistence", e);
        }
    }
} 