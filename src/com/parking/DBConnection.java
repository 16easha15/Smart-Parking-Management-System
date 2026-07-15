package com.parking;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;
/**
 * <h2>DBConnection</h2>
 * Factory utility class responsible for managing and establishing lifecycle 
 * connections to the underlying MySQL relational database.
 * 
 * @author Easha Kadganve
 * @author Vaishnavi Jadhav
 * @version 1.0.0
 * @since 2026-07-14
 */

public class DBConnection {
    
    /** Database URL endpoint specifying the host, port, and logical schema target. */
    private static final String URL = "jdbc:mysql://localhost:3306/smart_parking";
    
    /** Administrative privilege username credential for administrative access. */
    private static final String USER = "root"; 
    
    /** Security authentication password corresponding to the root user profile. */
    private static final String PASSWORD = "root@1";

    /**
     * Default private constructor to explicitly prevent instantiation of utility methods.
     */
    private DBConnection() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Initializes the relational database driver and constructs a active connection pipe.
     *
     * @return an active {@link Connection} session instance mapped to the target database.
     * @throws SQLException if a database structural network or credential access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly force registration of the MySQL Comms Driver to prevent class loading issues
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: MySQL JDBC Driver missing from runtime classpath: " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
