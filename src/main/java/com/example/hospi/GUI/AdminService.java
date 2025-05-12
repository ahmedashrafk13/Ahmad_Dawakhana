package com.example.hospi.GUI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Service class for handling admin-related database operations.
 * Provides methods to retrieve admin information from the database.
 */
public class AdminService {

    /**
     * Retrieves an Admin object from the database based on the username.
     *
     * @param username The username of the admin to search for
     * @return Admin object if found, null if not found or if an error occurs
     */
    public static Admin getAdminByUsername(String username) {
        // SQL query to join admins and users tables to find admin by username
        String query = "SELECT a.* FROM admins a JOIN users u ON a.user_id = u.id WHERE u.username = ?";

        try (
                // Get database connection from connection pool
                Connection conn = DatabaseConnection.getConnection();
                // Prepare the SQL statement
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            // Set the username parameter in the prepared statement
            stmt.setString(1, username);

            // Execute the query and get the result set
            ResultSet rs = stmt.executeQuery();

            // If a record is found, create and return an Admin object
            if (rs.next()) {
                return new Admin(
                        rs.getInt("id"),          // admin id
                        rs.getInt("user_id"),     // associated user id
                        rs.getString("name"),     // admin name
                        rs.getString("email"),    // admin email
                        rs.getString("phone")      // admin phone number
                );
            }
        } catch (Exception e) {
            // Log any errors that occur during database access
            System.out.println("Failed to fetch admin: " + e.getMessage());
        }

        // Return null if no admin found or if an error occurred
        return null;
    }
}