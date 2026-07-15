package com.parking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>ParkingDAOImpl</h2>
 * Concrete implementation engine fulfilling database CRUD behaviors. Uses transactional
 * SQL pipelines, handles boundary validations, and uses safe resource-management policies.
 */
public class ParkingDAOImpl implements ParkingDAO {

    /**
     * Database-level checkpoint validating if identification keys are already active inside the system.
     * 
     * @param conn Open transactional JDBC database pipe session instance.
     * @param vehicleNumber Plate sequence targeting verification lookup.
     * @return true if vehicle is actively tracked; false if vacant or completely clean.
     * @throws SQLException if connection pipeline faults occur.
     */
    private boolean isVehicleAlreadyParked(Connection conn, String vehicleNumber) throws SQLException {
        if (vehicleNumber == null) return false;
        // Using UPPER() ensures database-level case-insensitive plate lookups
        String query = "SELECT COUNT(*) FROM parking_spots WHERE UPPER(vehicle_number) = ? AND is_available = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, vehicleNumber.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Verifies if a given Spot ID exists in the system table configurations.
     * 
     * @param conn Open transactional JDBC database pipe session instance.
     * @param spotId Numeric key parameter identifying spatial records.
     * @return true if row matching ID exists; false if reference targets invalid out-of-bounds keys.
     * @throws SQLException if connection pipeline faults occur.
     */
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
        // LAYERED DEFENSE: Validate against null parameters immediately
        if (vehicleNumber == null || vehicleType == null || vehicleNumber.trim().isEmpty()) {
            System.out.println("❌ DAO Validation Error: Vehicle parameters cannot be null or empty.");
            return false;
        }

        String normalizedType = vehicleType.trim().toUpperCase();

        // LAYERED DEFENSE: Enforce strict type safety checks at the DAO layer
        if (!normalizedType.equals("CAR") && !normalizedType.equals("BIKE")) {
            System.out.println("❌ DAO Validation Error: System only supports 'CAR' or 'BIKE'. Cannot park: " + normalizedType);
            return false;
        }

        String findSpotQuery = "SELECT spot_id FROM parking_spots WHERE UPPER(vehicle_type) = ? AND is_available = TRUE LIMIT 1";
        String allocateSpotQuery = "UPDATE parking_spots SET vehicle_number = ?, is_available = FALSE, allocated_time = NOW() WHERE spot_id = ?";
        
        // Try-with-resources handles automatic closing of connection resources
        try (Connection conn = DBConnection.getConnection()) {
            if (isVehicleAlreadyParked(conn, vehicleNumber)) {
                System.out.println("❌ Validation Error: Vehicle " + vehicleNumber.toUpperCase() + " is already parked inside!");
                return false;
            }

            int availableSpotId = -1;
            try (PreparedStatement psFind = conn.prepareStatement(findSpotQuery)) {
                psFind.setString(1, normalizedType);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (rs.next()) availableSpotId = rs.getInt("spot_id");
                }
            }

            if (availableSpotId == -1) {
                System.out.println("❌ Error: No available slots for vehicle type: " + normalizedType);
                return false;
            }

            try (PreparedStatement psAllocate = conn.prepareStatement(allocateSpotQuery)) {
                psAllocate.setString(1, vehicleNumber.trim().toUpperCase());
                psAllocate.setInt(2, availableSpotId);
                
                int rowsUpdated = psAllocate.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("✅ Success: Vehicle parked successfully at Spot ID: " + availableSpotId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error during parking sequence execution: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean unparkVehicle(int spotId) {
        if (spotId <= 0) {
            System.out.println("❌ Validation Error: Invalid Spot ID.");
            return false;
        }

        String detailsQuery = "SELECT vehicle_number, vehicle_type, hourly_rate, allocated_time, " +
                              "TIMESTAMPDIFF(HOUR, allocated_time, NOW()) AS hours_spent " +
                              "FROM parking_spots WHERE spot_id = ?";
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
                        
                        // LAYERED DEFENSE: Guard against missing timestamp configurations
                        Timestamp allocatedTime = rs.getTimestamp("allocated_time");
                        if (allocatedTime == null) {
                            System.out.println("⚠️ Database Warning: allocated_time was NULL. Falling back to base 1-hour charge.");
                            hoursSpent = 1;
                        } else {
                            hoursSpent = rs.getLong("hours_spent");
                            // Fallback if data calculations generate less than one hour increments
                            if (rs.wasNull() || hoursSpent < 1) {
                                hoursSpent = 1; 
                            }
                        }
                    }
                }
            }

            double totalCost = hoursSpent * hourlyRate;

            try (PreparedStatement psUnpark = conn.prepareStatement(unparkQuery)) {
                psUnpark.setInt(1, spotId);
                int rowsUpdated = psUnpark.executeUpdate();
                
                if (rowsUpdated > 0) {
                    System.out.println("\n=====================================");
                    System.out.println("       PARKING RECEIPT & BILL        ");
                    System.out.println("=====================================");
                    System.out.println(" Spot Released   : " + spotId);
                    System.out.println(" Vehicle Number : " + vehicleNumber);
                    System.out.println(" Vehicle Type   : " + vehicleType);
                    System.out.println(" Duration       : " + hoursSpent + " Hour(s)");
                    System.out.println(" Dynamic Rate   : Rs. " + hourlyRate + " /hr");
                    System.out.println("-------------------------------------");
                    System.out.println(" TOTAL PAYABLE  : Rs. " + totalCost);
                    System.out.println("=====================================\n");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error during unparking sequence execution: " + e.getMessage());
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
            System.err.println("❌ DB Error fetching system layout records: " + e.getMessage());
        }
        return spots;
    }

    @Override
    public List<ParkingSpot> getAvailableSpotsByVehicleType(String vehicleType) {
        List<ParkingSpot> spots = new ArrayList<>();
        if (vehicleType == null || vehicleType.trim().isEmpty()) return spots;

        String normalizedType = vehicleType.trim().toUpperCase();
        
        // LAYERED DEFENSE: Keep safety clean across alternative lookup patterns
        if (!normalizedType.equals("CAR") && !normalizedType.equals("BIKE")) {
            System.out.println("❌ DAO Validation Error: Invalid type search query.");
            return spots;
        }

        String query = "SELECT * FROM parking_spots WHERE UPPER(vehicle_type) = ? AND is_available = TRUE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, normalizedType);
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
            System.err.println("❌ DB Error fetching vehicle status layout: " + e.getMessage());
        }
        return spots;
    }
}