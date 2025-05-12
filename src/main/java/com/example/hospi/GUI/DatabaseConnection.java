package com.example.hospi.GUI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides centralized database connection management for the application.
 * This utility class handles establishing and managing connections to the MySQL database.
 */
class DatabaseConnection {

    /**
     * Establishes and returns a connection to the hospital database.
     *
     * @return A live database Connection object
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        // Database connection parameters
        String url = "jdbc:mysql://localhost:3306/hospital_db"; // JDBC URL for MySQL database
        String user = "root"; // Database username with access privileges
        String password = "seecs@123"; // Database password (Note: Hardcoded credentials should be secured in production)

        try {
            // Attempt to establish connection using DriverManager
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connection successful!"); // Log successful connection
            return connection;
        } catch (SQLException e) {
            // Log connection failure details
            System.out.println("Connection failed: " + e.getMessage());
            // Re-throw exception with application-specific message
            throw new SQLException("Connection to database failed.");
        }
    }

    /**
     * Test method to verify database connectivity.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // Test the connection by getting and immediately closing it
            Connection connection = DatabaseConnection.getConnection();

            // Ensure connection is properly closed after test
            if (connection != null) {
                connection.close();
                System.out.println("Test connection closed successfully.");
            }
        } catch (SQLException e) {
            // Display any connection errors
            System.out.println("Error: " + e.getMessage());
        }
    }
}