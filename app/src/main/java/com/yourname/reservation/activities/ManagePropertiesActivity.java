package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yourname.reservation.R;
import com.yourname.reservation.adapters.PropertyAdapter;
import com.yourname.reservation.models.Property;

import java.util.ArrayList;
import java.util.List;

public class ManagePropertiesActivity extends AppCompatActivity implements PropertyAdapter.OnPropertyClickListener {
    private static final String TAG = "ManagePropertiesActivity";
    private RecyclerView propertiesRecyclerView;
    private PropertyAdapter propertyAdapter;
    private List<Property> properties;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_manage_properties);

            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Gérer mes biens");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            // Initialize RecyclerView
            propertiesRecyclerView = findViewById(R.id.propertiesRecyclerView);
            propertiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            // Initialize properties list and adapter
            properties = new ArrayList<>();
            propertyAdapter = new PropertyAdapter(properties, this);
            propertiesRecyclerView.setAdapter(propertyAdapter);

            // Load properties
            loadProperties();

            findViewById(R.id.addPropertyButton).setOnClickListener(v -> {
                Intent intent = new Intent(this, AddPropertyActivity.class);
                startActivity(intent);
            });

        } catch (Exception e) {
            Toast.makeText(this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProperties(); // Reload properties when returning to this activity
    }

    private void loadProperties() {
        String userId = auth.getCurrentUser().getUid();
        properties.clear();

        db.collection("properties")
                .whereEqualTo("agentId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Property property = document.toObject(Property.class);
                        property.setId(document.getId()); // Set the document ID
                        properties.add(property);
                    }
                    propertyAdapter.notifyDataSetChanged();

                    // Show empty state if no properties
                    View emptyState = findViewById(R.id.emptyState);
                    if (emptyState != null) {
                        emptyState.setVisibility(properties.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement des propriétés", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPropertyClick(Property property) {
        Intent intent = new Intent(this, EditPropertyActivity.class);
        intent.putExtra("propertyId", property.getId());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 