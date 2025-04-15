package com.yourname.reservation.interfaces;

/**
 * Interface for handling reservation-related actions
 */
public interface OnReservationActionListener {
    /**
     * Called when a reservation item is clicked
     * @param reservationId The ID of the clicked reservation
     */
    void onReservationClick(String reservationId);

    /**
     * Called when a reservation's status changes
     * @param reservationId The ID of the reservation
     * @param newStatus The new status of the reservation
     */
    void onReservationStatusChange(String reservationId, String newStatus);
} 