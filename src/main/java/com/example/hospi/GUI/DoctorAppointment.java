package com.example.hospi.GUI;

import javafx.scene.Cursor;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.sql.*;
import java.util.Optional;

/**
 * DoctorAppointment is a JavaFX application for managing doctor appointments.
 * It provides functionality to view, accept/reject, reschedule, and cancel appointments,
 * as well as manage doctor availability and view patient information.
 */
public class DoctorAppointment extends Application {

    // UI Theme Constants
    private static final String BACKGROUND_COLOR = "#0d1b2a";       // Dark blue background
    private static final String SECTION_BACKGROUND = "#102841";     // Slightly lighter section backgrounds
    private static final Color TEXT_COLOR = Color.web("#E0F7FA");   // Light text color
    private static final Color ACCENT_COLOR = Color.web("#87CEEB"); // Sky blue accent color
    private static final String BUTTON_STYLE =                     // Standard button styling
            "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;";

    // UI Components
    private Button btnViewAppointments, btnManageAvailability, btnAcceptRejectAppointments,
            btnViewPatientInfo, btnRescheduleAppointment, btnCancelAppointment;
    private Label statusLabel;
    private ListView<String> appointmentsListView;
    private Label hospitalNameLabel;
    private int selectedAppointmentId = -1; // Tracks currently selected appointment
    private int doctorId;

    /**
     * Main entry point for the JavaFX application.
     *
     * @param primaryStage The primary stage/window for this application
     */

    DoctorAppointment(int doctorId){
        this.doctorId= doctorId;
    }
    @Override
    public void start(Stage primaryStage) {
        // ========== UI COMPONENT SETUP ========== //

        // Hospital Title (Top Section)
        hospitalNameLabel = new Label("🏥 Lifeline Remote Hospital - Doctor Portal");
        hospitalNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        hospitalNameLabel.setTextFill(ACCENT_COLOR);
        hospitalNameLabel.setStyle("-fx-background-color: " + SECTION_BACKGROUND + "; -fx-padding: 20px;");
        hospitalNameLabel.setMaxWidth(Double.MAX_VALUE);
        hospitalNameLabel.setAlignment(Pos.CENTER);

        // Appointments View (Center Section)
        Label appointmentLabel = new Label("📋 Appointments");
        appointmentLabel.setTextFill(TEXT_COLOR);
        appointmentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        appointmentsListView = new ListView<>();
        appointmentsListView.setStyle("-fx-control-inner-background: " + SECTION_BACKGROUND +
                "; -fx-text-fill: " + TEXT_COLOR.toString().replace("0x", "#") +
                "; -fx-font-size: 14px;");
        appointmentsListView.setPrefHeight(400);

        VBox appointmentBox = new VBox(10, appointmentLabel, appointmentsListView);
        appointmentBox.setStyle("-fx-background-color: " + SECTION_BACKGROUND +
                "; -fx-padding: 20px; -fx-background-radius: 10px;");
        appointmentBox.setPrefWidth(600);

        // Action Buttons (Left Section)
        btnViewAppointments = createStyledButton("📄 View Appointments");
        btnManageAvailability = createStyledButton("🗓️ Manage Availability");
        btnAcceptRejectAppointments = createStyledButton("✅ Accept/Reject");
        btnViewPatientInfo = createStyledButton("👤 View Patient Info");
        btnRescheduleAppointment = createStyledButton("🕒 Reschedule");
        btnCancelAppointment = createStyledButton("❌ Cancel Appointment");

        VBox buttonBox = new VBox(15, btnViewAppointments, btnManageAvailability, btnAcceptRejectAppointments,
                btnViewPatientInfo, btnRescheduleAppointment, btnCancelAppointment);
        buttonBox.setPadding(new Insets(20));
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setStyle("-fx-background-color: " + SECTION_BACKGROUND + "; -fx-background-radius: 10px;");
        buttonBox.setPrefWidth(300);

        // Status Bar (Bottom Section)
        statusLabel = new Label("Status: Ready");
        statusLabel.setTextFill(TEXT_COLOR);
        statusLabel.setPadding(new Insets(10));
        statusLabel.setFont(Font.font("Segoe UI", 14));

        // ========== MAIN LAYOUT ========== //
        BorderPane root = new BorderPane();
        root.setTop(hospitalNameLabel);
        root.setLeft(buttonBox);
        root.setCenter(appointmentBox);
        root.setBottom(statusLabel);
        root.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // ========== EVENT HANDLERS ========== //
        btnViewAppointments.setOnAction(e -> viewAppointments());
        btnManageAvailability.setOnAction(e -> manageAvailability());
        btnAcceptRejectAppointments.setOnAction(e -> acceptRejectAppointments());
        btnViewPatientInfo.setOnAction(e -> viewPatientInfo());
        btnRescheduleAppointment.setOnAction(e -> rescheduleAppointment());
        btnCancelAppointment.setOnAction(e -> cancelAppointment());

        // ========== SCENE SETUP ========== //
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Doctor Dashboard - Appointment Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates a styled button with hover effects.
     *
     * @param text The button text
     * @return The configured Button object
     */
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", 14));
        button.setStyle(BUTTON_STYLE + "-fx-pref-width: 260px;");
        button.setCursor(Cursor.HAND);

        // Hover effects
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 260px;"));
        button.setOnMouseExited(e -> button.setStyle(BUTTON_STYLE + "-fx-pref-width: 260px;"));
        return button;
    }

    /**
     * Loads and displays appointments from the database.
     */
    private void viewAppointments() {
        String query = """
                SELECT a.id, p.name, a.appointment_date, a.start_time, a.end_time, a.status
                FROM appointments a
                JOIN patients p ON a.patient_id = p.id
                WHERE a.doctor_id = ?
                ORDER BY a.appointment_date, a.start_time
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, doctorId); // TODO: Replace with logged-in doctor ID
            ResultSet rs = stmt.executeQuery();
            appointmentsListView.getItems().clear();

            while (rs.next()) {
                String appointment = String.format("ID: %d | Patient: %s | Date: %s | Time: %s-%s | Status: %s",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDate("appointment_date"),
                        rs.getTime("start_time"),
                        rs.getTime("end_time"),
                        rs.getString("status"));
                appointmentsListView.getItems().add(appointment);
            }
            statusLabel.setText("Status: Appointments loaded successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Status: Error loading appointments.");
        }
    }

    /**
     * Shows a dialog for managing weekly availability.
     */
    private void manageAvailability() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🗓️ Manage Weekly Availability");
        dialog.getDialogPane().setStyle("-fx-background-color: " + SECTION_BACKGROUND + "; -fx-padding: 30px;");

        ButtonType saveButton = new ButtonType("💾 Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 10, 0));

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        Label[] dayLabels = new Label[days.length];
        TextField[] startFields = new TextField[days.length];
        TextField[] endFields = new TextField[days.length];
        LocalDate[] availableDates = new LocalDate[days.length];

        LocalDate today = LocalDate.now();

        for (int i = 0; i < days.length; i++) {
            DayOfWeek targetDay = DayOfWeek.of((i + 1) % 7 == 0 ? 7 : (i + 1) % 7);
            LocalDate date = today.with(TemporalAdjusters.nextOrSame(targetDay));
            availableDates[i] = date;

            String display = days[i] + " (" + date + ")";
            dayLabels[i] = new Label(display);
            dayLabels[i].setStyle("-fx-text-fill: " + TEXT_COLOR.toString().replace("0x", "#") +
                    "; -fx-font-size: 15px; -fx-font-weight: bold;");

            startFields[i] = new TextField();
            endFields[i] = new TextField();
            startFields[i].setPromptText("Start (e.g., 09:00)");
            endFields[i].setPromptText("End (e.g., 17:00)");

            styleTextField(startFields[i]);
            styleTextField(endFields[i]);

            grid.add(dayLabels[i], 0, i);
            grid.add(startFields[i], 1, i);
            grid.add(endFields[i], 2, i);
        }

        dialog.getDialogPane().setContent(grid);

        // Style dialog buttons
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButton);
        saveBtn.setStyle(BUTTON_STYLE);
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButton) {
            saveAvailabilityToDatabase(days, startFields, endFields, availableDates);
        }
    }

    /**
     * Saves availability data to the database.
     */
    private void saveAvailabilityToDatabase(String[] days, TextField[] startFields, TextField[] endFields, LocalDate[] availableDates) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String selectQuery = "SELECT id FROM doctor_availability WHERE doctor_id = ? AND day_of_week = ? AND available_date = ?";
            String updateQuery = "UPDATE doctor_availability SET start_time = ?, end_time = ? WHERE id = ?";
            String insertQuery = """
            INSERT INTO doctor_availability (doctor_id, day_of_week, available_date, start_time, end_time)
            VALUES (?, ?, ?, ?, ?)
        """;

            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

            for (int i = 0; i < days.length; i++) {
                String day = days[i];
                LocalDate date = availableDates[i];
                String start = startFields[i].getText().trim();
                String end = endFields[i].getText().trim();

                if (start.isEmpty() || end.isEmpty()) continue; // Skip empty inputs

                // Check if record already exists
                selectStmt.setInt(1, doctorId);
                selectStmt.setString(2, day);
                selectStmt.setDate(3, java.sql.Date.valueOf(date));

                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    int existingId = rs.getInt("id");
                    // Update existing record
                    updateStmt.setString(1, start);
                    updateStmt.setString(2, end);
                    updateStmt.setInt(3, existingId);
                    updateStmt.executeUpdate();
                } else {
                    // Insert new record
                    insertStmt.setInt(1, doctorId);
                    insertStmt.setString(2, day);
                    insertStmt.setDate(3, java.sql.Date.valueOf(date));
                    insertStmt.setString(4, start);
                    insertStmt.setString(5, end);
                    insertStmt.executeUpdate();
                }
            }

            statusLabel.setText("Status: Availability saved/updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Status: Error saving availability.");
        }
    }


    /**
     * Styles text fields consistently.
     */
    private void styleTextField(TextField field) {
        field.setStyle("""
            -fx-background-color: #2a2a3d;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #AAAAAA;
            -fx-border-color: #3498db;
            -fx-border-radius: 5px;
            -fx-background-radius: 5px;
            -fx-font-size: 13px;
            -fx-padding: 6px;
            """);
    }


    // Method to handle accepting or rejecting appointments
    private void acceptRejectAppointments() {
        // Get selected appointment from list view
        String selected = appointmentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select an appointment.");
            return;
        }

        // Extract appointment ID from the selected string
        int appointmentId = extractAppointmentId(selected);

        // Create dialog for doctor to choose status (Accepted/Rejected)
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Accepted", "Accepted", "Rejected");
        dialog.setTitle("Appointment Decision");
        dialog.setHeaderText("Choose the appointment status:");
        dialog.getDialogPane().setStyle("-fx-background-color: #34495e; -fx-padding: 20px;");

        // Process the doctor's choice
        dialog.showAndWait().ifPresent(status -> {
            // SQL queries for updating status and fetching patient details
            String updateQuery = "UPDATE appointments SET status = ? WHERE id = ?";
            String fetchEmailQuery = """
                    SELECT p.name, p.address, a.appointment_date, a.start_time
                    FROM appointments a
                    JOIN patients p ON a.patient_id = p.id
                    WHERE a.id = ?
                    """;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                 PreparedStatement fetchStmt = conn.prepareStatement(fetchEmailQuery)) {

                // Update appointment status in database
                updateStmt.setString(1, status);
                updateStmt.setInt(2, appointmentId);
                updateStmt.executeUpdate();

                // Fetch patient details for email notification
                fetchStmt.setInt(1, appointmentId);
                ResultSet rs = fetchStmt.executeQuery();

                if (rs.next()) {
                    // Extract patient details from result set
                    String patientName = rs.getString("name");
                    String patientEmail = rs.getString("address");
                    String date = rs.getString("appointment_date");
                    String time = rs.getString("start_time");

                    // Create email subject and body
                    String subject = "Your Appointment Has Been " + status;
                    String body = String.format("""
                            Dear %s,
                            
                            Your appointment scheduled for %s at %s has been %s by the doctor.
                            
                            Thank you for using City Hospital RPMS.
                            
                            Best regards,
                            City Hospital
                            """, patientName, date, time, status.toLowerCase());

                    // Send email notification to patient
                    Notifiable notifier = new EmailNotification();
                    notifier.sendNotification(subject, body, patientEmail);

                    statusLabel.setText("Status updated and email sent to patient.");
                }

                viewAppointments(); // Refresh the appointments list

            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Database error occurred.");
            }
        });
    }

    // Method to view patient information for selected appointment
    private void viewPatientInfo() {
        String selected = appointmentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Status: Please select an appointment first.");
            return;
        }

        // Extract appointment ID
        int appointmentId = extractAppointmentId(selected);

        // SQL query to fetch patient details
        String query = """
                SELECT p.id, p.name, p.gender, p.dob, p.phone, p.address
                FROM appointments a
                JOIN patients p ON a.patient_id = p.id
                WHERE a.id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Create dialog to display patient information
                Dialog<Void> dialog = new Dialog<>();
                dialog.setTitle("Patient Information");
                dialog.getDialogPane().setStyle("-fx-background-color: " + SECTION_BACKGROUND + ";");

                // Create grid layout for patient data
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20));

                // Add patient details to grid
                addPatientInfoRow(grid, 0, "Patient ID:", rs.getString("id"));
                addPatientInfoRow(grid, 1, "Name:", rs.getString("name"));
                addPatientInfoRow(grid, 2, "Gender:", rs.getString("gender"));
                addPatientInfoRow(grid, 3, "Date of Birth:", rs.getString("dob"));
                addPatientInfoRow(grid, 4, "Phone:", rs.getString("phone"));
                addPatientInfoRow(grid, 5, "Address:", rs.getString("address"));

                dialog.getDialogPane().setContent(grid);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

                // Style the OK button
                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.setStyle(BUTTON_STYLE);

                dialog.showAndWait();
            } else {
                statusLabel.setText("Status: No patient information found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Status: Error retrieving patient information.");
        }
    }

    // Helper method to add a row of patient information to the grid
    private void addPatientInfoRow(GridPane grid, int row, String label, String value) {
        Label infoLabel = new Label(label);
        infoLabel.setTextFill(TEXT_COLOR);
        infoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Label infoValue = new Label(value != null ? value : "N/A");
        infoValue.setTextFill(TEXT_COLOR);
        infoValue.setFont(Font.font("Segoe UI", 14));

        grid.add(infoLabel, 0, row);
        grid.add(infoValue, 1, row);
    }

    // Method to reschedule an appointment
    private void rescheduleAppointment() {
        String selected = appointmentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an appointment first.");
            return;
        }

        int id = extractAppointmentId(selected);
        // Create dialog to get new date from user
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reschedule");
        dialog.setHeaderText("Enter new date (YYYY-MM-DD):");
        dialog.getDialogPane().setStyle("-fx-background-color: #34495e; -fx-padding: 20px;");

        // Style dialog buttons
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        saveBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 6px;");
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 6px;");

        // Process new date input
        dialog.showAndWait().ifPresent(newDate -> {
            String query = "UPDATE appointments SET appointment_date = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                // Update appointment date in database
                stmt.setString(1, newDate);
                stmt.setInt(2, id);
                stmt.executeUpdate();
                viewAppointments(); // Refresh list
                statusLabel.setText("Rescheduled to " + newDate);
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Reschedule failed.");
            }
        });
    }

    // Method to cancel an appointment
    private void cancelAppointment() {
        String selected = appointmentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an appointment first.");
            return;
        }

        int id = extractAppointmentId(selected);

        // Create confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Appointment");
        confirmAlert.setHeaderText("Are you sure you want to cancel this appointment?");
        confirmAlert.setContentText("This action cannot be undone.");
        confirmAlert.getDialogPane().setStyle("-fx-background-color: #34495e; -fx-padding: 20px;");

        // Style confirmation buttons
        Button okButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px;");
        Button cancelButton = (Button) confirmAlert.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px;");

        // Process user confirmation
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String query = "UPDATE appointments SET status = 'Cancelled' WHERE id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    // Update appointment status to cancelled
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    viewAppointments(); // Refresh list
                    statusLabel.setText("Appointment cancelled successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    statusLabel.setText("Error cancelling appointment.");
                }
            }
        });
    }

    // Helper method to extract appointment ID from string
    private int extractAppointmentId(String appointmentText) {
        try {
            // Split the appointment text and find the ID portion
            String[] parts = appointmentText.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("ID:")) {
                    return Integer.parseInt(parts[i + 1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if ID not found
    }

    // Main method to launch the application
    public static void main(String[] args) {
        launch(args);
    }
}