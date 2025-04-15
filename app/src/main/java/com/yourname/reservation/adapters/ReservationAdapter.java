package com.yourname.reservation.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yourname.reservation.R;
import com.yourname.reservation.models.Property;
import com.yourname.reservation.models.Reservation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
    private static final String TAG = "ReservationAdapter";
    
    private final List<Reservation> reservations;
    private final Context context;
    private final boolean isAgentView;
    private final FirebaseFirestore db;

    public ReservationAdapter(Context context, List<Reservation> reservations, boolean isAgentView) {
        this.context = context;
        this.reservations = reservations;
        this.isAgentView = isAgentView;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if (reservations == null || position >= reservations.size()) {
                Log.e(TAG, "Invalid position or null reservations list");
                return;
            }
            
            Reservation reservation = reservations.get(position);
            Log.d(TAG, "Binding reservation at position " + position + ": " + reservation.getId() + " " + reservation);
            
            // Format the date
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String formattedDate = "Date non disponible";
                if (reservation.getVisitDate() != null) {
                    formattedDate = dateFormat.format(reservation.getVisitDate().toDate());
                    Log.d(TAG, "Formatted date: " + formattedDate);
                } else {
                    Log.e(TAG, "Visit date is null for reservation: " + reservation.getId());
                }
                holder.dateText.setText(formattedDate);
            } catch (Exception e) {
                Log.e(TAG, "Error formatting date", e);
                holder.dateText.setText("Date non disponible");
            }

            // Set common views
            holder.messageText.setText(reservation.getMessage());
            holder.statusText.setText(getStatusText(reservation.getStatus()));
            holder.statusText.setTextColor(getStatusColor(reservation.getStatus()));
            Log.d(TAG, "Set text views - Message: " + reservation.getMessage() + ", Status: " + getStatusText(reservation.getStatus()));

            // Load property details
            loadPropertyDetails(holder, reservation);

            // Show/hide action buttons based on view type and status
            boolean showButtons = isAgentView && "pending".equalsIgnoreCase(reservation.getStatus());
            Log.d(TAG, "Show action buttons: " + showButtons + " (isAgentView: " + isAgentView + ", status: " + reservation.getStatus() + ")");
            
            if (showButtons) {
                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.rejectButton.setVisibility(View.VISIBLE);

                holder.acceptButton.setOnClickListener(v -> updateReservationStatus(reservation, "accepted"));
                holder.rejectButton.setOnClickListener(v -> updateReservationStatus(reservation, "rejected"));
            } else {
                holder.acceptButton.setVisibility(View.GONE);
                holder.rejectButton.setVisibility(View.GONE);
            }
            
            Log.d(TAG, "Successfully bound view holder for reservation: " + reservation.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder at position " + position, e);
            e.printStackTrace();
        }
    }

    private void loadPropertyDetails(ViewHolder holder, Reservation reservation) {
        try {
            // Set default value in case of error
            holder.propertyText.setText("Propriété non disponible");
            holder.propertyText.setVisibility(View.VISIBLE);
            
            String propertyId = reservation.getPropertyId();
            Log.d(TAG, "Loading property details for propertyId: " + propertyId);
            
            if (propertyId != null && !propertyId.isEmpty()) {
                db.collection("properties")
                    .document(propertyId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            if (documentSnapshot.exists()) {
                                Property property = documentSnapshot.toObject(Property.class);
                                if (property != null) {
                                    String propertyInfo = String.format("%s - %s", 
                                        property.getTitle() != null ? property.getTitle() : "Sans titre", 
                                        property.getLocation() != null ? property.getLocation() : "Emplacement inconnu");
                                    holder.propertyText.setText(propertyInfo);
                                    holder.propertyText.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "Property loaded successfully: " + propertyInfo);
                                } else {
                                    Log.e(TAG, "Property document could not be converted to object");
                                }
                            } else {
                                Log.e(TAG, "Property document does not exist for ID: " + propertyId);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing property document", e);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading property", e);
                        holder.propertyText.setText("Erreur lors du chargement de la propriété");
                    });
            } else {
                Log.e(TAG, "Property ID is null or empty");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadPropertyDetails", e);
        }
    }

    private void updateReservationStatus(Reservation reservation, String newStatus) {
        try {
            String reservationId = reservation.getId();
            if (reservationId == null || reservationId.isEmpty()) {
                Log.e(TAG, "Reservation ID is null or empty");
                return;
            }

            DocumentReference reservationRef = db.collection("reservations").document(reservationId);
            
            reservationRef
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    String message = newStatus.equals("accepted") ? 
                        "Réservation acceptée" : "Réservation refusée";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating reservation status", e);
                    Toast.makeText(context, "Erreur lors de la mise à jour", 
                                 Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in updateReservationStatus", e);
            Toast.makeText(context, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
        }
    }

    private String getStatusText(String status) {
        if (status == null) return "En attente";
        switch (status.toLowerCase()) {
            case "pending":
                return "En attente";
            case "accepted":
                return "Acceptée";
            case "rejected":
                return "Refusée";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return context.getColor(R.color.colorPending);
        switch (status.toLowerCase()) {
            case "pending":
                return context.getColor(R.color.colorPending);
            case "accepted":
                return context.getColor(R.color.colorAccepted);
            case "rejected":
                return context.getColor(R.color.colorRejected);
            default:
                return context.getColor(android.R.color.darker_gray);
        }
    }

    @Override
    public int getItemCount() {
        return reservations != null ? reservations.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView messageText;
        TextView statusText;
        TextView propertyText;
        Button acceptButton;
        Button rejectButton;

        ViewHolder(View view) {
            super(view);
            dateText = view.findViewById(R.id.dateText);
            messageText = view.findViewById(R.id.messageText);
            statusText = view.findViewById(R.id.statusText);
            propertyText = view.findViewById(R.id.propertyText);
            acceptButton = view.findViewById(R.id.acceptButton);
            rejectButton = view.findViewById(R.id.rejectButton);
        }
    }
}