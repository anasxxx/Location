package com.yourname.reservation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yourname.reservation.R;
import com.yourname.reservation.models.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {
    private final List<Property> properties;
    private final OnPropertyClickListener listener;

    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
    }

    public PropertyAdapter(List<Property> properties, OnPropertyClickListener listener) {
        this.properties = properties;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = properties.get(position);
        
        holder.titleText.setText(property.getTitle());
        holder.locationText.setText(property.getLocation());
        holder.priceText.setText(String.format(Locale.getDefault(), "%d €/mois", property.getPrice()));
        holder.detailsText.setText(String.format(Locale.getDefault(), 
            "%d ch • %d sdb • %s", 
            property.getBedrooms(), 
            property.getBathrooms(),
            property.getType()));

        // Load the first image if available
        List<String> imageUrls = property.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrls.get(0))
                    .placeholder(R.drawable.placeholder_property)
                    .error(R.drawable.error_property)
                    .centerCrop()
                    .into(holder.propertyImage);
        } else {
            holder.propertyImage.setImageResource(R.drawable.placeholder_property);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPropertyClick(property);
            }
        });
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    public void setProperties(List<Property> newProperties) {
        this.properties.clear();
        if (newProperties != null) {
            this.properties.addAll(newProperties);
        }
        notifyDataSetChanged();
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {
        final ImageView propertyImage;
        final TextView titleText;
        final TextView locationText;
        final TextView priceText;
        final TextView detailsText;

        PropertyViewHolder(View view) {
            super(view);
            propertyImage = view.findViewById(R.id.propertyImage);
            titleText = view.findViewById(R.id.titleText);
            locationText = view.findViewById(R.id.locationText);
            priceText = view.findViewById(R.id.priceText);
            detailsText = view.findViewById(R.id.detailsText);
        }
    }
} 