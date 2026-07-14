package com.parking;

import java.sql.Timestamp;

public class ParkingSpot {
    private int spotId;
    private String vehicleNumber;
    private String vehicleType;
    private boolean isAvailable;
    private Timestamp allocatedTime;
    private double hourlyRate; // Added field

    public ParkingSpot() {}

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

    @Override
    public String toString() {
        return String.format("| Spot ID: %-5d | Type: %-6s | Available: %-5b | Rate/Hr: Rs.%-6.2f | Vehicle: %-10s | Time: %-19s |", 
                spotId, vehicleType, isAvailable, hourlyRate, (vehicleNumber == null ? "N/A" : vehicleNumber), 
                (allocatedTime == null ? "N/A" : allocatedTime.toString()));
    }
}