package com.example.hospi.GUI;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import javax.swing.*;

public class UserService {

    public boolean validateAdminLogin(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username=? AND password=? AND role='admin'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void loadUsers(DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT id, username, role FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addUser(String username, String password, String role) {
        if ("admin".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(null, "Adding admin users is not allowed.");
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String insertUser = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement stmtUser = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, username);
            stmtUser.setString(2, password);
            stmtUser.setString(3, role);
            stmtUser.executeUpdate();

            ResultSet generatedKeys = stmtUser.getGeneratedKeys();
            int userId = -1;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            if ("doctor".equalsIgnoreCase(role)) {
                String insertDoctor = "INSERT INTO doctors (user_id, name, specialization, phone, email) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmtDoctor = conn.prepareStatement(insertDoctor);
                stmtDoctor.setInt(1, userId);
                stmtDoctor.setString(2, username);
                stmtDoctor.setString(3, "General Practitioner");
                stmtDoctor.setString(4, "000-000-0000");
                stmtDoctor.setString(5, username + "@hospital.com");
                stmtDoctor.executeUpdate();

            } else if ("patient".equalsIgnoreCase(role)) {
                String insertPatient = "INSERT INTO patients (user_id, name, gender, dob, phone, address) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmtPatient = conn.prepareStatement(insertPatient);
                stmtPatient.setInt(1, userId);
                stmtPatient.setString(2, username);
                stmtPatient.setString(3, "Other");
                stmtPatient.setDate(4, java.sql.Date.valueOf("2000-01-01"));
                stmtPatient.setString(5, "000-000-0000");
                stmtPatient.setString(6, "123 Placeholder St");
                stmtPatient.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String getRoleQuery = "SELECT role FROM users WHERE id = ?";
            PreparedStatement roleStmt = conn.prepareStatement(getRoleQuery);
            roleStmt.setInt(1, userId);
            ResultSet rs = roleStmt.executeQuery();

            String role = null;
            if (rs.next()) {
                role = rs.getString("role");
            } else {
                conn.rollback();
                return false;
            }

            if ("admin".equalsIgnoreCase(role)) {
                JOptionPane.showMessageDialog(null, "Deleting admin users is not allowed.");
                return false;
            }

            if ("doctor".equalsIgnoreCase(role)) {
                // Get doctor.id from doctors where user_id = ?
                int doctorId = -1;
                PreparedStatement getDoctorIdStmt = conn.prepareStatement("SELECT id FROM doctors WHERE user_id = ?");
                getDoctorIdStmt.setInt(1, userId);
                ResultSet doctorRs = getDoctorIdStmt.executeQuery();
                if (doctorRs.next()) {
                    doctorId = doctorRs.getInt("id");
                }

                // First, delete doctor availability records
                String deleteAvailability = "DELETE FROM doctor_availability WHERE doctor_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteAvailability)) {
                    ps.setInt(1, doctorId);
                    ps.executeUpdate();
                }

                // Now delete from other tables
                String[][] doctorTables = {
                        {"appointments", "doctor_id"},
                        {"doctorpatientassignment", "DoctorID"},
                        {"chat_messages", "sender_id"},
                        {"chat_messages", "receiver_id"},
                        {"video_call_requests", "doctor_id"},
                        {"video_calls", "doctor_id"},
                        {"prescriptions", "DoctorID"},
                        {"feedback", "doctor_id"}
                };

                for (String[] pair : doctorTables) {
                    String table = pair[0];
                    String column = pair[1];
                    String sql = "DELETE FROM hospital_db." + table + " WHERE " + column + " = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, doctorId);
                        ps.executeUpdate();
                    }
                }

                // Finally, delete the doctor record itself
                PreparedStatement deleteDoctor = conn.prepareStatement("DELETE FROM doctors WHERE id = ?");
                deleteDoctor.setInt(1, doctorId);
                deleteDoctor.executeUpdate();



        } else if ("patient".equalsIgnoreCase(role)) {
                // Get patient.id from patients where user_id = ?
                int patientId = -1;
                PreparedStatement getPatientIdStmt = conn.prepareStatement("SELECT id FROM patients WHERE user_id = ?");
                getPatientIdStmt.setInt(1, userId);
                ResultSet patientRs = getPatientIdStmt.executeQuery();
                if (patientRs.next()) {
                    patientId = patientRs.getInt("id");
                }

                String[][] patientTables = {
                        {"appointments", "patient_id"},
                        {"feedback", "patient_id"},
                        {"chat_messages", "sender_id"},
                        {"chat_messages", "receiver_id"},
                        {"prescriptions", "PatientID"},
                        {"vitals", "patient_id"},
                        {"video_call_requests", "patient_id"},
                        {"video_calls", "patient_id"},
                        {"emergency_alerts", "patient_id"},
                        {"doctorpatientassignment", "PatientID"}
                };

                for (String[] pair : patientTables) {
                    String table = pair[0];
                    String column = pair[1];
                    String sql = "DELETE FROM hospital_db." + table + " WHERE " + column + " = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, column.equalsIgnoreCase("PatientID") ? patientId : userId);
                        ps.executeUpdate();
                    }
                }

                PreparedStatement deletePatient = conn.prepareStatement("DELETE FROM patients WHERE user_id = ?");
                deletePatient.setInt(1, userId);
                deletePatient.executeUpdate();
            }

            PreparedStatement deleteUserStmt = conn.prepareStatement("DELETE FROM users WHERE id = ?");
            deleteUserStmt.setInt(1, userId);
            int affectedRows = deleteUserStmt.executeUpdate();

            conn.commit();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
