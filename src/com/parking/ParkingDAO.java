package com.parking;

import java.util.List;

/**
 * <h2>ParkingDAO</h2>
 * Defines the contract interface for our Data Access Object pattern. 
 * Abstracts database interactions from higher layers of application logic.
 */
public interface ParkingDAO {
    
    /**
     * Allocates an available slot inside the persistent engine for a specific vehicle.
     *
     * @param vehicleNumber Structural identifier code mapped onto the space entity.
     * @param vehicleType   Classification parameter determining storage properties (e.g., CAR, BIKE).
     * @return true if persistence state successfully saved; false if errors occur.
     */
    boolean parkVehicle(String vehicleNumber, String vehicleType);

    /**
     * Frees an allocated slot from the registry, clears fields, and generates dynamic invoices.
     *
     * @param spotId Unique key identity pointing to spatial database records.
     * @return true if records successfully cleared and closed; false if operation failed.
     */
    boolean unparkVehicle(int spotId);

    /**
     * Pulls the complete array dataset of slots managed under active storage arrays.
     *
     * @return a {@link List} containing complete historical mapped {@link ParkingSpot} entities.
     */
    List<ParkingSpot> getAllSpots();

    /**
     * Filters available spaces matching classification descriptors.
     *
     * @param vehicleType Classification constraints matching targeting elements.
     * @return a filtered {@link List} containing match records whose current status is free.
     */
    List<ParkingSpot> getAvailableSpotsByVehicleType(String vehicleType);
}