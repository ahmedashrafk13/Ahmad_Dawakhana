package com.example.hospi.GUI;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The PanicButton class provides emergency alert functionality for patients.
 * When triggered, it sends notifications to the assigned doctor via both email and SMS.
 */
public class PanicButton {

    private final int patientId; // ID of the patient triggering the alert
    private final EmailNotification emailService = new EmailNotification(); // Email service instance
    private final TwilioSMS twilioService = new TwilioSMS(); // SMS service instance

    /**
     * Constructor for PanicButton
     * @param patientId The ID of the patient who can trigger this panic button
     */
    public PanicButton(int patientId) {
        this.patientId = patientId;
    }

    /**
     * Displays the emergency alert button window
     */
    public void displayPanicButtonWindow() {
        Stage stage = new Stage();
        stage.setTitle("Emergency Alert");

        // Create and style the panic button
        Button panicButton = new Button("Send Emergency Alert");
        panicButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px;");

        // Set action handler for the panic button
        panicButton.setOnAction(e -> {
            try {
                sendEmergencyAlert();
                showConfirmation("Emergency alert sent successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Failed to send emergency alert.");
            }
        });

        // Set up the layout
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20px; -fx-alignment: center;");
        root.getChildren().add(panicButton);

        Scene scene = new Scene(root, 300, 200);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Sends emergency alerts to the assigned doctor via email and SMS
     * @throws SQLException If there's an error accessing the database
     */
    private void sendEmergencyAlert() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // SQL queries to retrieve patient and doctor information
            String patientQuery = "SELECT name, phone, address FROM patients WHERE id = ?";
            String doctorQuery = "SELECT d.email, d.phone, d.name FROM doctorpatientassignment a " +
                    "JOIN doctors d ON a.DoctorID = d.id WHERE a.PatientID = ? LIMIT 1";

            // Variables to store retrieved information
            String patientName = "", patientPhone = "", patientAddress = "";
            String doctorEmail = "", doctorPhone = "", doctorName = "";

            // Retrieve patient details
            try (PreparedStatement patientStmt = conn.prepareStatement(patientQuery)) {
                patientStmt.setInt(1, patientId);
                try (ResultSet rs = patientStmt.executeQuery()) {
                    if (rs.next()) {
                        patientName = rs.getString("name");
                        patientPhone = rs.getString("phone");
                        patientAddress = rs.getString("address");
                    }
                }
            }

            // Retrieve assigned doctor details
            try (PreparedStatement doctorStmt = conn.prepareStatement(doctorQuery)) {
                doctorStmt.setInt(1, patientId);
                try (ResultSet rs = doctorStmt.executeQuery()) {
                    if (rs.next()) {
                        doctorEmail = rs.getString("email");
                        doctorPhone = rs.getString("phone");
                        doctorName = rs.getString("name");
                    }
                }
            }

            // If we found a doctor, send notifications
            if (!doctorEmail.isEmpty()) {
                sendEmailNotification(doctorName, doctorEmail, patientName, patientPhone, patientAddress);
                sendSMSNotification(doctorPhone, doctorName, patientName);
            }
        }
    }

    /**
     * Sends email notification to the assigned doctor
     * @param doctorName Name of the doctor to notify
     * @param doctorEmail Email address of the doctor
     * @param patientName Name of the patient in distress
     * @param patientPhone Phone number of the patient
     * @param patientAddress Address of the patient
     */
    private void sendEmailNotification(String doctorName, String doctorEmail,
                                       String patientName, String patientPhone,
                                       String patientAddress) {
        String subject = "Emergency Alert: Immediate Attention Needed";
        String message = "Dear Dr. " + doctorName + ",\n\n" +
                "Your patient, " + patientName + ", has triggered the emergency panic button.\n" +
                "Patient details:\n" +
                "Name: " + patientName + "\n" +
                "Phone: " + patientPhone + "\n" +
                "Address: " + patientAddress + "\n\n" +
                "Please check on them immediately.\n\nRegards,\nHospital Management System";
        emailService.sendNotification(subject, message, doctorEmail);
    }

    /**
     * Sends SMS notification to the assigned doctor
     * @param doctorPhone Phone number of the doctor
     * @param doctorName Name of the doctor
     * @param patientName Name of the patient in distress
     */
    private void sendSMSNotification(String doctorPhone, String doctorName, String patientName) {
        String smsMessage = "Urgent: Patient " + patientName + " has triggered the panic button. Immediate attention required.";
        twilioService.sendSMS(doctorPhone, smsMessage);
    }

    /**
     * Shows a confirmation dialog
     * @param message The message to display
     */
    private void showConfirmation(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error dialog
     * @param message The error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}