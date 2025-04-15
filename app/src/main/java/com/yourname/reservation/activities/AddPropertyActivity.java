package com.yourname.reservation.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yourname.reservation.R;
import com.yourname.reservation.models.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddPropertyActivity extends AppCompatActivity {
    private EditText titleInput;
    private EditText descriptionInput;
    private EditText locationInput;
    private EditText priceInput;
    private EditText typeInput;
    private EditText bedroomsInput;
    private EditText bathroomsInput;
    private Button addImagesButton;
    private Button submitButton;
    private RecyclerView imagesRecyclerView;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private List<Uri> selectedImages;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        setupToolbar();
        setupImagePicker();

        // Initialize image list
        selectedImages = new ArrayList<>();

        // Set click listeners
        addImagesButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        submitButton.setOnClickListener(v -> validateAndSave());
    }

    private void initializeViews() {
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        locationInput = findViewById(R.id.locationInput);
        priceInput = findViewById(R.id.priceInput);
        typeInput = findViewById(R.id.typeInput);
        bedroomsInput = findViewById(R.id.bedroomsInput);
        bathroomsInput = findViewById(R.id.bathroomsInput);
        addImagesButton = findViewById(R.id.addImagesButton);
        submitButton = findViewById(R.id.submitButton);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ajouter un bien");
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImages.add(uri);
                    updateImagesDisplay();
                }
            }
        );
    }

    private void updateImagesDisplay() {
        // Update your RecyclerView or image display logic here
    }

    private void validateAndSave() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String type = typeInput.getText().toString().trim();
        String bedroomsStr = bedroomsInput.getText().toString().trim();
        String bathroomsStr = bathroomsInput.getText().toString().trim();

        // Validation
        if (title.isEmpty() || description.isEmpty() || location.isEmpty() || 
            priceStr.isEmpty() || type.isEmpty() || bedroomsStr.isEmpty() || bathroomsStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int price = Integer.parseInt(priceStr);
            int bedrooms = Integer.parseInt(bedroomsStr);
            int bathrooms = Integer.parseInt(bathroomsStr);
            String agentId = auth.getCurrentUser().getUid();

            // Create property object
            Property property = new Property(title, description, location, price, 
                                          type, bedrooms, bathrooms, agentId);

            // Show progress
            progressBar.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);

            // Upload images first if any
            if (!selectedImages.isEmpty()) {
                uploadImagesAndSaveProperty(property);
            } else {
                saveProperty(property);
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Veuillez entrer des nombres valides", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImagesAndSaveProperty(Property property) {
        List<String> imageUrls = new ArrayList<>();
        StorageReference storageRef = storage.getReference();
        int[] uploadCount = {0};

        for (Uri imageUri : selectedImages) {
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = storageRef.child("properties/" + imageName);

            imageRef.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        imageUrls.add(uri.toString());
                        uploadCount[0]++;

                        if (uploadCount[0] == selectedImages.size()) {
                            property.setImageUrls(imageUrls);
                            saveProperty(property);
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(this, "Erreur lors de l'upload des images", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveProperty(Property property) {
        db.collection("properties")
                .add(property)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Bien ajouté avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Erreur lors de l'ajout du bien", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 