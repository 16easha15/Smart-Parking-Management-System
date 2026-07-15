package com.parking;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * <h2>Main</h2>
 * Acts as the Presentation Layer / Command Line Interface (CLI) controller. Handles
 * stream inputs, regular expression validation, error handling, and orchestrating
 * user operations down to the structural Data Access Layer.
 * 
 * @author Easha Kadganve
 * @author Vaishnavi Jadhav
 * @version 1.0.0
 * @since 2026-07-14
 */
public class Main {
    /** Dedicated Scanner instance listening onto standard user input console stream. */
    private static final Scanner scanner = new Scanner(System.in);
    
    /** Abstract loose decoupling binding to data interface handling persistent requests. */
    private static final ParkingDAO parkingDAO = new ParkingDAOImpl();

    /** 
     * Regex constraint: Requires string length bounds between 4 and 15 items.
     * Accepts alphanumeric literals, spacing gaps, or traditional dash hyphens.
     */
    private static final Pattern VEHICLE_PLATE_PATTERN = Pattern.compile("^[A-Z0-9\\s\\-]{4,15}$", Pattern.CASE_INSENSITIVE);

    /**
     * Runtime system orchestrator loop presenting interactive selection states.
     *
     * @param args Array of execution arguments passed into runtime environment.
     */
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
                    break;
                default:
                    System.out.println("❌ Invalid choice! Please enter a number between 1 and 5.");
            }
        }
    }

    /**
     * Intercepts user streams to handle allocation procedures.
     * Sanitizes inputs and runs edge validations prior to handing execution to the DAO layer.
     */
    private static void handlePark() {
        System.out.print("Enter Vehicle Type (CAR / BIKE): ");
        String vehicleType = scanner.nextLine().trim().toUpperCase();
        
        if (!vehicleType.equals("CAR") && !vehicleType.equals("BIKE")) {
            System.out.println("❌ Validation Error: System only supports 'CAR' or 'BIKE'.");
            return;
        }

        System.out.print("Enter Vehicle Registration Number (e.g., MH-12-AB-1234): ");
        String vehicleNumber = scanner.nextLine().trim();

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

    /**
     * Intercepts user inputs to clean up slot allocations and calculate fee structures.
     */
    private static void handleUnpark() {
        System.out.print("Enter Spot ID to clear: ");
        int spotId = readIntegerInput();
        
        if (spotId <= 0) {
            System.out.println("❌ Validation Error: Spot ID must be a positive integer.");
            return;
        } 

        parkingDAO.unparkVehicle(spotId);
    }

    /**
     * Fetches complete grid records from database engine and displays formatted records.
     */
    private static void displayAllSpots() {
        List<ParkingSpot> spots = parkingDAO.getAllSpots();
        if (spots.isEmpty()) {
            System.out.println("⚠️ Database is currently empty. Please configure some spots in MySQL first.");
            return;
        }
        
        String border = "=========================================================================================================================";
        
        System.out.println("\n" + border);
        System.out.println("                                            PARKING SYSTEM DASHBOARD                                                     ");
        System.out.println(border);
        spots.forEach(System.out::println);
        System.out.println(border);
    }

    /**
     * Queries specific subset arrays based on defined classification strings.
     */
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

    /**
     * Parses user inputs while catching exceptions to prevent system crashes from bad inputs.
     *
     * @return the parsed choice selection integer, or -1 fallback indicating bad format error.
     */
    private static int readIntegerInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1; 
        }
    }
}