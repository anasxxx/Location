package com.yourname.reservation.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yourname.reservation.R;
import com.yourname.reservation.models.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditPropertyActivity extends AppCompatActivity {
    private static final String TAG = "EditPropertyActivity";

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText locationInput;
    private EditText priceInput;
    private EditText typeInput;
    private EditText bedroomsInput;
    private EditText bathroomsInput;
    private Button addImagesButton;
    private MaterialButton submitButton;
    private MaterialButton deleteButton;
    private RecyclerView imagesRecyclerView;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private Property property;
    private String propertyId;
    private List<Uri> newImages;
    private List<String> existingImageUrls;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_edit_property);

            // Initialize Firebase
            db = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
            auth = FirebaseAuth.getInstance();

            // Get property ID from intent
            propertyId = getIntent().getStringExtra("propertyId");
            if (propertyId == null || propertyId.isEmpty()) {
                Log.e(TAG, "Property ID is null or empty in intent");
                Toast.makeText(this, "Erreur: ID de propriété invalide", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d(TAG, "Loading property with ID: " + propertyId);

            // Initialize views and setup
            initializeViews();
            setupToolbar();
            setupImagePicker();

            // Initialize lists
            newImages = new ArrayList<>();
            existingImageUrls = new ArrayList<>();

            // Load property data from Firestore
            loadPropertyFromFirestore();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Erreur lors de l'initialisation", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadPropertyFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("properties")
            .document(propertyId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    property = documentSnapshot.toObject(Property.class);
                    if (property != null) {
                        property.setId(documentSnapshot.getId());
                        existingImageUrls = new ArrayList<>(property.getImageUrls());
                        loadPropertyData();
                    } else {
                        Log.e(TAG, "Failed to convert document to Property object");
                        Toast.makeText(this, "Erreur: données de propriété invalides", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Log.e(TAG, "Property document does not exist");
                    Toast.makeText(this, "Erreur: propriété non trouvée", Toast.LENGTH_SHORT).show();
                    finish();
                }
                progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading property data", e);
                Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                finish();
            });
    }

    private void initializeViews() {
        try {
            titleInput = findViewById(R.id.titleInput);
            descriptionInput = findViewById(R.id.descriptionInput);
            locationInput = findViewById(R.id.locationInput);
            priceInput = findViewById(R.id.priceInput);
            typeInput = findViewById(R.id.typeInput);
            bedroomsInput = findViewById(R.id.bedroomsInput);
            bathroomsInput = findViewById(R.id.bathroomsInput);
            addImagesButton = findViewById(R.id.addImagesButton);
            submitButton = findViewById(R.id.submitButton);
            deleteButton = findViewById(R.id.deleteButton);
            imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
            progressBar = findViewById(R.id.progressBar);

            // Setup delete button
            if (deleteButton != null) {
                deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    private void setupToolbar() {
        try {
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Modifier le bien");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar", e);
            throw e;
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    newImages.add(uri);
                    updateImagesDisplay();
                }
            }
        );
    }

    private void loadPropertyData() {
        try {
            titleInput.setText(property.getTitle());
            descriptionInput.setText(property.getDescription());
            locationInput.setText(property.getLocation());
            priceInput.setText(String.valueOf(property.getPrice()));
            typeInput.setText(property.getType());
            bedroomsInput.setText(String.valueOf(property.getBedrooms()));
            bathroomsInput.setText(String.valueOf(property.getBathrooms()));
            updateImagesDisplay();

            // Set click listeners after data is loaded
            addImagesButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
            submitButton.setOnClickListener(v -> validateAndUpdate());
        } catch (Exception e) {
            Log.e(TAG, "Error loading property data", e);
            Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImagesDisplay() {
        // TODO: Implement image display logic
    }

    private void validateAndUpdate() {
        try {
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

            int price = Integer.parseInt(priceStr);
            int bedrooms = Integer.parseInt(bedroomsStr);
            int bathrooms = Integer.parseInt(bathroomsStr);

            // Verify property ID again
            if (property.getId() == null || property.getId().isEmpty()) {
                Log.e(TAG, "Property ID is null or empty before update");
                Toast.makeText(this, "Erreur: ID de propriété invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update property object
            property.setTitle(title);
            property.setDescription(description);
            property.setLocation(location);
            property.setPrice(price);
            property.setType(type);
            property.setBedrooms(bedrooms);
            property.setBathrooms(bathrooms);

            // Show progress
            progressBar.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);

            Log.d(TAG, "Updating property with ID: " + property.getId());

            // Upload new images if any
            if (!newImages.isEmpty()) {
                uploadNewImagesAndUpdate();
            } else {
                updateProperty();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Veuillez entrer des nombres valides", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error in validateAndUpdate", e);
            Toast.makeText(this, "Erreur lors de la validation", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadNewImagesAndUpdate() {
        try {
            List<String> newImageUrls = new ArrayList<>();
            StorageReference storageRef = storage.getReference();
            int[] uploadCount = {0};

            for (Uri imageUri : newImages) {
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
                        newImageUrls.add(uri.toString());
                        uploadCount[0]++;

                        if (uploadCount[0] == newImages.size()) {
                            // Combine existing and new image URLs
                            List<String> allImageUrls = new ArrayList<>(existingImageUrls);
                            allImageUrls.addAll(newImageUrls);
                            property.setImageUrls(allImageUrls);
                            updateProperty();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error uploading image", e);
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        Toast.makeText(this, "Erreur lors du téléchargement des images", Toast.LENGTH_SHORT).show();
                    });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in uploadNewImagesAndUpdate", e);
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Erreur lors du téléchargement des images", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProperty() {
        try {
            Log.d(TAG, "Updating property in Firestore with ID: " + property.getId());
            db.collection("properties")
                .document(property.getId())
                .set(property)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Property updated successfully");
                    Toast.makeText(this, "Bien mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating property", e);
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Erreur lors de la mise à jour: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in updateProperty", e);
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Supprimer le bien")
            .setMessage("Êtes-vous sûr de vouloir supprimer ce bien ? Cette action est irréversible.")
            .setPositiveButton("Supprimer", (dialog, which) -> deleteProperty())
            .setNegativeButton("Annuler", null)
            .show();
    }

    private void deleteProperty() {
        try {
            Log.d(TAG, "Starting delete process for property: " + propertyId);
            progressBar.setVisibility(View.VISIBLE);
            deleteButton.setEnabled(false);
            submitButton.setEnabled(false);

            // First, delete all images from storage
            List<String> imageUrls = property.getImageUrls();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                Log.d(TAG, "Found " + imageUrls.size() + " images to delete");
                int[] deleteCount = {0};
                for (String imageUrl : imageUrls) {
                    try {
                        Log.d(TAG, "Attempting to delete image: " + imageUrl);
                        StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
                        imageRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Successfully deleted image: " + imageUrl);
                                deleteCount[0]++;
                                Log.d(TAG, "Deleted " + deleteCount[0] + " of " + imageUrls.size() + " images");
                                if (deleteCount[0] == imageUrls.size()) {
                                    Log.d(TAG, "All images deleted, proceeding to delete property document");
                                    deletePropertyDocument();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting image: " + imageUrl, e);
                                deleteCount[0]++;
                                if (deleteCount[0] == imageUrls.size()) {
                                    Log.d(TAG, "Finished attempting to delete all images, proceeding with property deletion");
                                    deletePropertyDocument();
                                }
                            });
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Invalid image URL format: " + imageUrl, e);
                        deleteCount[0]++;
                        if (deleteCount[0] == imageUrls.size()) {
                            deletePropertyDocument();
                        }
                    }
                }
            } else {
                Log.d(TAG, "No images to delete, proceeding with property deletion");
                deletePropertyDocument();
            }
        } catch (Exception e) {
            Log.e(TAG, "Critical error in deleteProperty", e);
            progressBar.setVisibility(View.GONE);
            deleteButton.setEnabled(true);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePropertyDocument() {
        try {
            Log.d(TAG, "Attempting to delete property document with ID: " + propertyId);
            db.collection("properties")
                .document(propertyId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Property document deleted successfully");
                    Toast.makeText(this, "Bien supprimé avec succès", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting property document", e);
                    progressBar.setVisibility(View.GONE);
                    deleteButton.setEnabled(true);
                    submitButton.setEnabled(true);
                    Toast.makeText(this, "Erreur lors de la suppression: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Log.e(TAG, "Critical error in deletePropertyDocument", e);
            progressBar.setVisibility(View.GONE);
            deleteButton.setEnabled(true);
            submitButton.setEnabled(true);
            Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
} 