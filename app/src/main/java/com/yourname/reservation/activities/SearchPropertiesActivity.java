package com.yourname.reservation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yourname.reservation.R;
import com.yourname.reservation.adapters.PropertyAdapter;
import com.yourname.reservation.models.Property;

import java.util.ArrayList;
import java.util.List;

public class SearchPropertiesActivity extends AppCompatActivity implements PropertyAdapter.OnPropertyClickListener {
    private EditText searchInput;
    private SeekBar priceRangeBar;
    private TextView priceRangeText;
    private RecyclerView propertiesRecyclerView;
    private PropertyAdapter propertyAdapter;
    private List<Property> properties;
    private List<Property> filteredProperties;
    private FirebaseFirestore db;
    private double maxPrice = 10000; // Default max price in euros

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_properties);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        searchInput = findViewById(R.id.searchInput);
        priceRangeBar = findViewById(R.id.priceRangeBar);
        priceRangeText = findViewById(R.id.priceRangeText);
        propertiesRecyclerView = findViewById(R.id.propertiesRecyclerView);

        // Setup RecyclerView
        properties = new ArrayList<>();
        filteredProperties = new ArrayList<>();
        propertyAdapter = new PropertyAdapter(filteredProperties, this);
        propertiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        propertiesRecyclerView.setAdapter(propertyAdapter);

        // Setup search input listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterProperties();
            }
        });

        // Setup price range bar
        priceRangeBar.setMax(100);
        priceRangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatePriceRangeText(progress);
                filterProperties();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Load properties
        loadProperties();
    }

    private void loadProperties() {
        db.collection("properties")
            .orderBy("price", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                properties.clear();
                maxPrice = 10000; // Reset max price

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Property property = document.toObject(Property.class);
                    property.setId(document.getId());
                    properties.add(property);

                    // Update max price if needed
                    if (property.getPrice() > maxPrice) {
                        maxPrice = property.getPrice();
                    }
                }

                // Initial filtering
                filterProperties();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erreur lors du chargement des biens", Toast.LENGTH_SHORT).show();
            });
    }

    private void filterProperties() {
        String searchText = searchInput.getText().toString().toLowerCase();
        double maxPriceFilter = (priceRangeBar.getProgress() / 100.0) * maxPrice;

        filteredProperties.clear();

        for (Property property : properties) {
            boolean matchesSearch = searchText.isEmpty() ||
                    property.getTitle().toLowerCase().contains(searchText) ||
                    property.getLocation().toLowerCase().contains(searchText) ||
                    property.getDescription().toLowerCase().contains(searchText);

            boolean matchesPrice = property.getPrice() <= maxPriceFilter;

            if (matchesSearch && matchesPrice) {
                filteredProperties.add(property);
            }
        }

        propertyAdapter.notifyDataSetChanged();

        // Show/hide empty state
        View emptyState = findViewById(R.id.emptyState);
        if (emptyState != null) {
            emptyState.setVisibility(filteredProperties.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updatePriceRangeText(int progress) {
        double currentMaxPrice = (progress / 100.0) * maxPrice;
        priceRangeText.setText(String.format("Prix maximum: %.0f€", currentMaxPrice));
    }

    @Override
    public void onPropertyClick(Property property) {
        Intent intent = new Intent(this, PropertyDetailsActivity.class);
        intent.putExtra("propertyId", property.getId());
        startActivity(intent);
    }
} 