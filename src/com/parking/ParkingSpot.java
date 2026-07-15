package com.parking;

import java.sql.Timestamp;

/**
 * <h2>ParkingSpot</h2>
 * Represents the core domain model (POJO / Entity class) encapsulating
 * the structural data state of an individual parking space row configuration.
 */
public class ParkingSpot {
    
    /** System unique reference ID identifier key for the spot. */
    private int spotId;
    
    /** Active license record value parked inside slot; null if vacant. */
    private String vehicleNumber;
    
    /** Explicit space categorization restriction parameter configuration (e.g., CAR/BIKE). */
    private String vehicleType;
    
    /** Availability state tracker flag. */
    private boolean isAvailable;
    
    /** Timestamp tracker indicating exact record check-in time. */
    private Timestamp allocatedTime;
    
    /** Financial variable specifying cost parameters tracked per hour metrics. */
    private double hourlyRate;

    /**
     * Default no-arguments constructor initializing basic structures.
     */
    public ParkingSpot() {}

    /**
     * Full arguments constructor configuring complete spatial elements fields.
     *
     * @param spotId        Unique system lookup key index.
     * @param vehicleNumber License text identifying vehicle state.
     * @param vehicleType   Classification parameter mapping constraints.
     * @param isAvailable   Boolean availability flag status tracker.
     * @param allocatedTime Timestamp record setting exact transaction check-in point.
     * @param hourlyRate    Financial rate value cost metric.
     */
    public ParkingSpot(int spotId, String vehicleNumber, String vehicleType, boolean isAvailable, Timestamp allocatedTime, double hourlyRate) {
        this.spotId = spotId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.isAvailable = isAvailable;
        this.allocatedTime = allocatedTime;
        this.hourlyRate = hourlyRate;
    }

    public int getSpotId() { return spotId; }
    public void setSpotId(int spotId) { this.spotId = spotId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public Timestamp getAllocatedTime() { return allocatedTime; }
    public void setAllocatedTime(Timestamp allocatedTime) { this.allocatedTime = allocatedTime; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    /**
     * Generates a tabular string presentation configuration row mapping elements perfectly.
     * Uses uniform sizing buffers to keep text boundaries aligned within console dashboard limits.
     *
     * @return dynamic grid visual presentation layout representation block string.
     */
    @Override
    public String toString() {
        return String.format("| Spot ID: %-4d | Type: %-5s | Available: %-5b | Rate/Hr: Rs.%-6.2f | Vehicle: %-15s | Time: %-21s |", 
                spotId, 
                vehicleType, 
                isAvailable, 
                hourlyRate, 
                (vehicleNumber == null ? "N/A" : vehicleNumber), 
                (allocatedTime == null ? "N/A" : allocatedTime.toString()));
    }
}