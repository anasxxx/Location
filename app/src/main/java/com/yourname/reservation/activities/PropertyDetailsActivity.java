package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourname.reservation.R;
import com.yourname.reservation.adapters.ImageSliderAdapter;
import com.yourname.reservation.models.Property;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PropertyDetailsActivity extends AppCompatActivity {
    private static final String TAG = "PropertyDetailsActivity";
    
    private ViewPager2 imageSlider;
    private MaterialTextView titleText;
    private MaterialTextView priceText;
    private MaterialTextView locationText;
    private MaterialTextView descriptionText;
    private MaterialTextView detailsText;
    private MaterialButton reserveButton;
    private View progressBar;
    private Toolbar toolbar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Property property;
    private String propertyId;
    private List<String> currentImages;
    private ImageSliderAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        try {
            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Get property ID from intent
            propertyId = getIntent().getStringExtra("propertyId");
            Log.d(TAG, "Received propertyId: " + propertyId);
            
            if (propertyId == null) {
                Toast.makeText(this, "Erreur: Propriété non trouvée", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize views
            initializeViews();
            
            // Setup toolbar
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Détails du bien");
            }

            // Load property data
            loadPropertyData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            imageSlider = findViewById(R.id.imageSlider);
            titleText = findViewById(R.id.titleText);
            priceText = findViewById(R.id.priceText);
            locationText = findViewById(R.id.locationText);
            descriptionText = findViewById(R.id.descriptionText);
            detailsText = findViewById(R.id.detailsText);
            reserveButton = findViewById(R.id.reserveButton);
            progressBar = findViewById(R.id.progressBar);

            // Initialize image slider
            currentImages = new ArrayList<>();
            imageAdapter = new ImageSliderAdapter(this, currentImages);
            imageSlider.setAdapter(imageAdapter);
            imageSlider.setOffscreenPageLimit(1);

            // Setup reserve button click listener
            reserveButton.setOnClickListener(v -> startReservation());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    private void loadPropertyData() {
        try {
            Log.d(TAG, "Loading property data for ID: " + propertyId);
            progressBar.setVisibility(View.VISIBLE);

            db.collection("properties")
                    .document(propertyId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            progressBar.setVisibility(View.GONE);
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "Document exists: " + documentSnapshot.getData());
                                property = documentSnapshot.toObject(Property.class);
                                if (property != null) {
                                    property.setId(documentSnapshot.getId());
                                    Log.d(TAG, "Property loaded: " + property.toString());
                                    displayPropertyData();
                                } else {
                                    Log.e(TAG, "Failed to convert document to Property object");
                                    Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } else {
                                Log.e(TAG, "Document does not exist for ID: " + propertyId);
                                Toast.makeText(this, "Propriété non trouvée", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing document", e);
                            Toast.makeText(this, "Erreur lors du traitement des données", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading property", e);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadPropertyData", e);
            Toast.makeText(this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayPropertyData() {
        try {
            Log.d(TAG, "Displaying property data: " + property.toString());
            
            // Set title
            titleText.setText(property.getTitle());

            // Format and set price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
            priceText.setText(formatter.format(property.getPrice()));

            // Set location
            locationText.setText(property.getLocation());

            // Set description
            descriptionText.setText(property.getDescription());

            // Set details (bedrooms, bathrooms, and type)
            String details = String.format("%d ch • %d sdb • %s", 
                property.getBedrooms(), 
                property.getBathrooms(),
                property.getType());
            detailsText.setText(details);

            // Setup image slider
            List<String> imageUrls = property.getImageUrls();
            Log.d(TAG, "Image URLs from property: " + (imageUrls != null ? imageUrls.toString() : "null"));

            if (imageUrls != null && !imageUrls.isEmpty()) {
                Log.d(TAG, "Setting up image slider with " + imageUrls.size() + " images");
                // Create a new list to avoid modification issues
                List<String> validUrls = new ArrayList<>();
                for (String url : imageUrls) {
                    if (url != null && !url.isEmpty()) {
                        validUrls.add(url);
                        Log.d(TAG, "Added valid URL: " + url);
                    }
                }
                
                if (!validUrls.isEmpty()) {
                    imageAdapter.updateImages(validUrls);
                } else {
                    Log.d(TAG, "No valid URLs found, using placeholder");
                    List<String> placeholder = new ArrayList<>();
                    placeholder.add("placeholder");
                    imageAdapter.updateImages(placeholder);
                }
            } else {
                Log.d(TAG, "No images available, using placeholder");
                List<String> placeholder = new ArrayList<>();
                placeholder.add("placeholder");
                imageAdapter.updateImages(placeholder);
            }

            // Show reserve button only for clients (not for the agent who owns the property)
            if (auth.getCurrentUser() != null && 
                !auth.getCurrentUser().getUid().equals(property.getAgentId())) {
                reserveButton.setVisibility(View.VISIBLE);
            } else {
                reserveButton.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying property data", e);
            Toast.makeText(this, "Erreur lors de l'affichage des données", Toast.LENGTH_SHORT).show();
        }
    }

    private void startReservation() {
        try {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Veuillez vous connecter pour réserver", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                return;
            }

            if (property == null) {
                Toast.makeText(this, "Erreur: données de la propriété manquantes", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, ReservationActivity.class);
            intent.putExtra("property", property);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting reservation", e);
            Toast.makeText(this, "Erreur lors du démarrage de la réservation", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
} 