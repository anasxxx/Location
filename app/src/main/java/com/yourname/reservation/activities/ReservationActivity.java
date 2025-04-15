package com.yourname.reservation.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourname.reservation.R;
import com.yourname.reservation.models.Property;
import com.yourname.reservation.models.Reservation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReservationActivity extends AppCompatActivity {
    private static final String TAG = "ReservationActivity";
    
    private EditText visitDateInput;
    private EditText messageInput;
    private Button submitButton;
    private Property property;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SimpleDateFormat dateFormat;
    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        try {
            // Initialize Firebase
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            selectedDateTime = Calendar.getInstance();

            // Get property from intent
            property = (Property) getIntent().getSerializableExtra("property");
            if (property == null || property.getAgentId() == null) {
                Log.e(TAG, "Property or agent ID is null");
                Toast.makeText(this, "Erreur: propriété non trouvée ou agent non spécifié", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize views
            initializeViews();

            // Set up click listeners
            setupClickListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        visitDateInput = findViewById(R.id.visitDateInput);
        messageInput = findViewById(R.id.messageInput);
        submitButton = findViewById(R.id.submitButton);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Demande de visite");
        }

        // Disable keyboard for date input
        visitDateInput.setFocusable(false);
        visitDateInput.setClickable(true);
    }

    private void setupClickListeners() {
        visitDateInput.setOnClickListener(v -> showDateTimePicker());
        submitButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Show time picker after date is selected
                new TimePickerDialog(
                    this,
                    (timeView, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        visitDateInput.setText(dateFormat.format(selectedDateTime.getTime()));
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    true
                ).show();
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void validateAndSubmit() {
        try {
            String dateStr = visitDateInput.getText().toString().trim();
            String message = messageInput.getText().toString().trim();

            if (dateStr.isEmpty()) {
                visitDateInput.setError("Veuillez choisir une date");
                return;
            }

            if (message.isEmpty()) {
                messageInput.setError("Veuillez ajouter un message");
                return;
            }

            // Disable submit button to prevent double submission
            submitButton.setEnabled(false);

            // Create reservation with selected date
            String userId = auth.getCurrentUser().getUid();
            Log.d(TAG, "Creating reservation for user: " + userId + ", agent: " + property.getAgentId() + ", property: " + property.getId());
            
            Timestamp visitTimestamp = new Timestamp(selectedDateTime.getTime());
            Reservation reservation = new Reservation(
                property.getId(),    // propertyId
                userId,             // userId
                property.getAgentId(), // agentId
                visitTimestamp,     // visitDate
                message            // message
            );

            // Log the reservation object to verify all fields are set correctly
            Log.d(TAG, "Reservation object created: propertyId=" + reservation.getPropertyId() 
                  + ", userId=" + reservation.getUserId() 
                  + ", agentId=" + reservation.getAgentId()
                  + ", status=" + reservation.getStatus()
                  + ", message=" + reservation.getMessage());

            // Save to Firestore
            Log.d(TAG, "Adding reservation to Firestore...");
            db.collection("reservations")
                .add(reservation)
                .addOnSuccessListener(documentReference -> {
                    // Set the ID and update the document
                    String docId = documentReference.getId();
                    Log.d(TAG, "Reservation added successfully with ID: " + docId);
                    reservation.setId(docId);
                    documentReference.set(reservation)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Reservation ID updated successfully");
                            Toast.makeText(this, "Demande envoyée avec succès", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating reservation with ID", e);
                            Toast.makeText(this, "Erreur lors de la mise à jour: " + e.getMessage(), 
                                         Toast.LENGTH_SHORT).show();
                            submitButton.setEnabled(true);
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving reservation", e);
                    Toast.makeText(this, "Erreur lors de l'envoi: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                    submitButton.setEnabled(true);
                });

        } catch (Exception e) {
            Log.e(TAG, "Error in validateAndSubmit", e);
            Toast.makeText(this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
            submitButton.setEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}