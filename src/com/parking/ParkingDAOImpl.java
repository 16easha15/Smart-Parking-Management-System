package com.parking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingDAOImpl implements ParkingDAO {

    private boolean isVehicleAlreadyParked(Connection conn, String vehicleNumber) throws SQLException {
        String query = "SELECT COUNT(*) FROM parking_spots WHERE vehicle_number = ? AND is_available = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, vehicleNumber.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private boolean doesSpotExist(Connection conn, int spotId) throws SQLException {
        String query = "SELECT COUNT(*) FROM parking_spots WHERE spot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, spotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public boolean parkVehicle(String vehicleNumber, String vehicleType) {
        String findSpotQuery = "SELECT spot_id FROM parking_spots WHERE vehicle_type = ? AND is_available = TRUE LIMIT 1";
        String allocateSpotQuery = "UPDATE parking_spots SET vehicle_number = ?, is_available = FALSE, allocated_time = NOW() WHERE spot_id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            if (isVehicleAlreadyParked(conn, vehicleNumber)) {
                System.out.println("❌ Validation Error: Vehicle " + vehicleNumber + " is already parked inside!");
                return false;
            }

            int availableSpotId = -1;
            try (PreparedStatement psFind = conn.prepareStatement(findSpotQuery)) {
                psFind.setString(1, vehicleType.toUpperCase());
                try (ResultSet rs = psFind.executeQuery()) {
                    if (rs.next()) availableSpotId = rs.getInt("spot_id");
                }
            }

            if (availableSpotId == -1) {
                System.out.println("❌ Error: No available slots for vehicle type: " + vehicleType);
                return false;
            }

            try (PreparedStatement psAllocate = conn.prepareStatement(allocateSpotQuery)) {
                psAllocate.setString(1, vehicleNumber.toUpperCase());
                psAllocate.setInt(2, availableSpotId);
                
                int rowsUpdated = psAllocate.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("✅ Success: Vehicle parked successfully at Spot ID: " + availableSpotId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error during parking: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean unparkVehicle(int spotId) {
        // Query reads the specific hourly_rate saved in that row
        String detailsQuery = "SELECT vehicle_number, vehicle_type, hourly_rate, TIMESTAMPDIFF(HOUR, allocated_time, NOW()) AS hours_spent FROM parking_spots WHERE spot_id = ?";
        String unparkQuery = "UPDATE parking_spots SET vehicle_number = NULL, is_available = TRUE, allocated_time = NULL WHERE spot_id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            
            if (!doesSpotExist(conn, spotId)) {
                System.out.println("❌ Validation Error: Spot ID " + spotId + " does not exist.");
                return false;
            }

            String vehicleNumber = "";
            String vehicleType = "";
            double hourlyRate = 0.0;
            long hoursSpent = 0;

            try (PreparedStatement psDetails = conn.prepareStatement(detailsQuery)) {
                psDetails.setInt(1, spotId);
                try (ResultSet rs = psDetails.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getString("vehicle_number") == null) {
                            System.out.println("❌ Validation Error: Spot ID " + spotId + " is already vacant.");
                            return false;
                        }
                        vehicleNumber = rs.getString("vehicle_number");
                        vehicleType = rs.getString("vehicle_type");
                        hourlyRate = rs.getDouble("hourly_rate");
                        hoursSpent = rs.getLong("hours_spent");
                    }
                }
            }

            // Minimum 1 hour fee guard
            if (hoursSpent < 1) {
                hoursSpent = 1; 
            }

            double totalCost = hoursSpent * hourlyRate;

            try (PreparedStatement psUnpark = conn.prepareStatement(unparkQuery)) {
                psUnpark.setInt(1, spotId);
                int rowsUpdated = psUnpark.executeUpdate();
                
                if (rowsUpdated > 0) {
                    System.out.println("\n=====================================");
                    System.out.println("       PARKING RECEIPT & BILL        ");
                    System.out.println("=====================================");
                    System.out.println("📍 Spot Released   : " + spotId);
                    System.out.println("🚗 Vehicle Number : " + vehicleNumber);
                    System.out.println("📦 Vehicle Type   : " + vehicleType);
                    System.out.println("⏱️ Duration       : " + hoursSpent + " Hour(s)");
                    System.out.println("💰 Dynamic Rate   : Rs. " + hourlyRate + " /hr");
                    System.out.println("-------------------------------------");
                    System.out.println("💵 TOTAL PAYABLE  : Rs. " + totalCost);
                    System.out.println("=====================================\n");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error during unparking: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<ParkingSpot> getAllSpots() {
        List<ParkingSpot> spots = new ArrayList<>();
        String query = "SELECT * FROM parking_spots";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                spots.add(new ParkingSpot(
                        rs.getInt("spot_id"),
                        rs.getString("vehicle_number"),
                        rs.getString("vehicle_type"),
                        rs.getBoolean("is_available"),
                        rs.getTimestamp("allocated_time"),
                        rs.getDouble("hourly_rate")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error fetching spots: " + e.getMessage());
        }
        return spots;
    }

    @Override
    public List<ParkingSpot> getAvailableSpotsByVehicleType(String vehicleType) {
        List<ParkingSpot> spots = new ArrayList<>();
        String query = "SELECT * FROM parking_spots WHERE vehicle_type = ? AND is_available = TRUE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, vehicleType.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    spots.add(new ParkingSpot(
                            rs.getInt("spot_id"),
                            rs.getString("vehicle_number"),
                            rs.getString("vehicle_type"),
                            rs.getBoolean("is_available"),
                            rs.getTimestamp("allocated_time"),
                            rs.getDouble("hourly_rate")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error fetching availability: " + e.getMessage());
        }
        return spots;
    }
}