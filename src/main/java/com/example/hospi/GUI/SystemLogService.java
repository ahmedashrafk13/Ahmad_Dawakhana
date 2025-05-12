package com.example.hospi.GUI;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

/**
 * SystemLogService - Provides centralized logging and retrieval functionality
 * for tracking administrative activities in the hospital management system.
 */
public class SystemLogService {

    /**
     * Logs an administrative action to the system audit trail.
     *
     * @param adminUsername The username of the administrator performing the action
     * @param action        The type/category of action (e.g., "USER_CREATED", "CONFIG_UPDATED")
     * @param description   Detailed description of the action performed
     */
    public static void log(String adminUsername, String action, String description) {
        String query = "INSERT INTO system_logs (admin_username, action, description) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, adminUsername);
            stmt.setString(2, action);
            stmt.setString(3, description);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging in production
        }
    }

    /**
     * Loads all logs into the given table model.
     *
     * @param model The DefaultTableModel to populate.
     */
    public static void loadLogs(DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT admin_username, action, description, timestamp FROM system_logs ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("admin_username"),
                        rs.getString("action"),
                        rs.getString("description"),
                        rs.getTimestamp("timestamp")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads logs filtered by a specific admin.
     *
     * @param adminUsername Username to filter by
     * @param model         Table model to populate
     */
    public static void loadLogsByAdmin(String adminUsername, DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT admin_username, action, description, timestamp FROM system_logs WHERE admin_username = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, adminUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("admin_username"),
                            rs.getString("action"),
                            rs.getString("description"),
                            rs.getTimestamp("timestamp")
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Alias for `loadLogsByAdmin` to fix calls expecting a method named getLogsByAdmin.
     *
     * @param adminUsername Admin to filter logs by
     * @param model         Table model to populate
     */
    public static void getLogsByAdmin(String adminUsername, DefaultTableModel model) {
        loadLogsByAdmin(adminUsername, model);
    }
}
