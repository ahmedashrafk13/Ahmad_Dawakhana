package com.example.hospi.GUI;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

/**
 * Handles uploading of patient vitals to the database,
 * including emergency detection for abnormal vitals and email alert.
 */
public class UploadVitals {

    private static final String INSERT_VITALS_SQL =
            "INSERT INTO vitals (patient_id, heart_rate, blood_pressure, oxygen_level, temperature, recorded_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final Notifiable notifier = new EmailNotification();

    public static boolean uploadVitalsToDatabase(
            int patientId,
            String heartRate,
            String bloodPressure,
            String oxygenLevel,
            String temperature
    ) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_VITALS_SQL, Statement.RETURN_GENERATED_KEYS)) {

            fillPreparedStatement(pstmt, patientId, heartRate, bloodPressure, oxygenLevel, temperature);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0 && isAbnormal(heartRate, bloodPressure, oxygenLevel, temperature)) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int vitalsId = generatedKeys.getInt(1);
                        handleEmergency(conn, patientId, vitalsId);
                        showAbnormalPopup();
                    }
                }
            }
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error uploading vitals: " + e.getMessage());
            return false;
        }
    }

    public static boolean uploadVitalsFromCSV(File csvFile, int patientId) {
        if (csvFile == null || !csvFile.exists()) {
            System.err.println("Invalid CSV file provided.");
            return false;
        }

        boolean hasValidEntries = false;

        try (Connection conn = DatabaseConnection.getConnection();
             BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            // Skip the header line
            String line = br.readLine();
            if (line == null) {
                System.err.println("CSV file is empty.");
                return false;
            }

            // Process remaining lines
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 4) {
                    System.out.println("Skipping invalid row: " + line);
                    continue;
                }

                String heartRate = values[0].trim();
                String bloodPressure = values[1].trim();
                String oxygenLevel = values[2].trim();
                String temperature = values[3].trim();

                try (PreparedStatement pstmt = conn.prepareStatement(INSERT_VITALS_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    fillPreparedStatement(pstmt, patientId, heartRate, bloodPressure, oxygenLevel, temperature);
                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0 && isAbnormal(heartRate, bloodPressure, oxygenLevel, temperature)) {
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int vitalsId = generatedKeys.getInt(1);
                                handleEmergency(conn, patientId, vitalsId);
                                showAbnormalPopup();
                            }
                        }
                    }

                    hasValidEntries = true;
                }
            }

        } catch (IOException | SQLException e) {
            System.err.println("Error uploading vitals from CSV: " + e.getMessage());
        }

        return hasValidEntries;
    }

    private static boolean isAbnormal(String heartRate, String bloodPressure, String oxygenLevel, String temperature) {
        try {
            int hr = Integer.parseInt(heartRate);
            int o2 = Integer.parseInt(oxygenLevel);
            float temp = Float.parseFloat(temperature);

            String[] bpParts = bloodPressure.split("/");
            if (bpParts.length != 2) {
                System.err.println("Invalid blood pressure format. Expected systolic/diastolic.");
                return false;
            }

            int systolic = Integer.parseInt(bpParts[0].trim());
            int diastolic = Integer.parseInt(bpParts[1].trim());

            return (hr < 60 || hr > 100) ||
                    (systolic < 90 || systolic > 140) ||
                    (diastolic < 60 || diastolic > 90) ||
                    (o2 < 95) ||
                    (temp < 97.0 || temp > 99.5);

        } catch (Exception e) {
            System.err.println("Error parsing vitals: " + e.getMessage());
            return false;
        }
    }

    private static void handleEmergency(Connection conn, int patientId, int vitalsId) throws SQLException {
        String insertAlertSQL = "INSERT INTO emergency_alerts (patient_id, vitals_id, alert_time, alert_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertAlertSQL)) {
            ps.setInt(1, patientId);
            ps.setInt(2, vitalsId);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setString(4, "Abnormal Vitals");
            ps.executeUpdate();
        }

        String doctorQuery = "SELECT d.email, d.name FROM doctors d " +
                "JOIN doctorpatientassignment da ON d.id = da.DoctorID " +
                "WHERE da.PatientID = ?";
        try (PreparedStatement ps = conn.prepareStatement(doctorQuery)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String email = rs.getString("email");
                String name = rs.getString("name");
                String subject = "🚨 Emergency Alert for Patient ID " + patientId;
                String message = "Dear Dr. " + name + ",\n\nAbnormal vitals were detected for your patient (ID: " + patientId + ").\nPlease review the vitals immediately.\n\nRegards,\nHospital System";
                System.out.println("Sending emergency alert to: " + email);
                notifier.sendNotification(subject, message, email);
            }
        }
    }

    private static void fillPreparedStatement(PreparedStatement pstmt,
                                              int patientId,
                                              String heartRate,
                                              String bloodPressure,
                                              String oxygenLevel,
                                              String temperature) throws SQLException {
        pstmt.setInt(1, patientId);
        pstmt.setString(2, heartRate);
        pstmt.setString(3, bloodPressure);
        pstmt.setString(4, oxygenLevel);
        pstmt.setString(5, temperature);
        pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
    }

    private static void showAbnormalPopup() {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Abnormal Vitals Detected");
            alert.setHeaderText("Your vitals appear abnormal.");
            alert.setContentText("An emergency alert has been sent to your assigned doctor.");
            alert.showAndWait();
        });
    }
}
