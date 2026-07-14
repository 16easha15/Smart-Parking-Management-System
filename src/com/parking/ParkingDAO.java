package com.parking;

import java.util.List;

public interface ParkingDAO {
	boolean parkVehicle(String vehicleNumber, String vehicleType);
    boolean unparkVehicle(int spotId);
    List<ParkingSpot> getAllSpots();
    List<ParkingSpot> getAvailableSpotsByVehicleType(String vehicleType);

}
