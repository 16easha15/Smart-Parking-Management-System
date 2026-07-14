package com.parking;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ParkingDAO parkingDAO = new ParkingDAOImpl();

    // Regex Explanation: Requires 4 to 15 characters, allowing alphanumeric characters, spaces, or hyphens.
    private static final Pattern VEHICLE_PLATE_PATTERN = Pattern.compile("^[A-Z0-9\\s\\-]{4,15}$", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) {
        System.out.println("=== Welcome to Smart Parking Management System ===");
        
        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Park a Vehicle");
            System.out.println("2. Unpark a Vehicle (Free a Spot & Bill)");
            System.out.println("3. View Dashboard (All Spots)");
            System.out.println("4. Check Available Spots by Vehicle Type");
            System.out.println("5. Exit");
            System.out.print("Select an option (1-5): ");

            int choice = readIntegerInput();

            switch (choice) {
                case 1:
                    handlePark();
                    break;
                case 2:
                    handleUnpark();
                    break;
                case 3:
                    displayAllSpots();
                    break;
                case 4:
                    displayAvailableByType();
                    break;
                case 5:
                    System.out.println("Thank you for using Smart Parking Management System. Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("❌ Invalid choice! Please enter a number between 1 and 5.");
            }
        }
    }

    private static void handlePark() {
        System.out.print("Enter Vehicle Type (CAR / BIKE): ");
        String vehicleType = scanner.nextLine().trim().toUpperCase();
        
        // VALIDATION: Strict type check
        if (!vehicleType.equals("CAR") && !vehicleType.equals("BIKE")) {
            System.out.println("❌ Validation Error: System only supports 'CAR' or 'BIKE'.");
            return;
        }

        System.out.print("Enter Vehicle Registration Number (e.g., MH-12-AB-1234): ");
        String vehicleNumber = scanner.nextLine().trim();

        // VALIDATION: Regex format and empty string check
        if (vehicleNumber.isEmpty()) {
            System.out.println("❌ Validation Error: Registration number cannot be empty.");
            return;
        }
        if (!VEHICLE_PLATE_PATTERN.matcher(vehicleNumber).matches()) {
            System.out.println("❌ Validation Error: Invalid Registration format! Use 4 to 15 alphanumeric characters (hyphens/spaces allowed).");
            return;
        }

        parkingDAO.parkVehicle(vehicleNumber, vehicleType);
    }

    private static void handleUnpark() {
        System.out.print("Enter Spot ID to clear: ");
        int spotId = readIntegerInput();
        
        // VALIDATION: Stop negative or invalid integer IDs
        if (spotId <= 0) {
            System.out.println("❌ Validation Error: Spot ID must be a positive integer.");
            return;
        } 

        // This triggers the dynamic bill printing logic from the DAO
        parkingDAO.unparkVehicle(spotId);
    }

    private static void displayAllSpots() {
        List<ParkingSpot> spots = parkingDAO.getAllSpots();
        if (spots.isEmpty()) {
            System.out.println("⚠️ Database is currently empty. Please configure some spots in MySQL first.");
            return;
        }
        System.out.println("\n=================================== PARKING SYSTEM DASHBOARD ===================================");
        spots.forEach(System.out::println);
        System.out.println("=================================================================================================");
    }

    private static void displayAvailableByType() {
        System.out.print("Enter Vehicle Type to query (CAR / BIKE): ");
        String type = scanner.nextLine().trim().toUpperCase();
        
        if (!type.equals("CAR") && !type.equals("BIKE")) {
            System.out.println("❌ Validation Error: Vehicle Type must be CAR or BIKE.");
            return;
        }

        List<ParkingSpot> availableSpots = parkingDAO.getAvailableSpotsByVehicleType(type);
        if (availableSpots.isEmpty()) {
            System.out.println("❌ Sorry, no available spots left for " + type);
        } else {
            System.out.println("\n--- Available " + type + " Slots ---");
            availableSpots.forEach(spot -> System.out.println("📍 Spot ID: " + spot.getSpotId()));
        }
    }

    // This is the missing piece that handles inputs safely and fixes your compiler crash
    private static int readIntegerInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1; // Gracefully flag non-integer inputs without throwing system exceptions
        }
    }
}