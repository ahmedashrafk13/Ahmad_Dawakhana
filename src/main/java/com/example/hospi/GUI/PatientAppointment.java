package com.example.hospi.GUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalTime;

public class PatientAppointment extends Application {

    // UI Components
    private ComboBox<String> doctorComboBox;       // Dropdown to select doctors
    private ListView<String> slotListView;         // List to show available time slots
    private ComboBox<LocalTime> timeComboBox;      // Dropdown to select specific time
    private Button btnBookAppointment;             // Button to book appointment
    private Label statusLabel;                     // Label to show status messages

    // Data fields
    private int selectedDoctorId = -1;             // Currently selected doctor's ID
    private int selectedSlotId = -1;               // Currently selected time slot ID
    private int patientId;

    // Default patient ID for demo
    PatientAppointment(int patientId){
        this.patientId = patientId;
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize UI components
        doctorComboBox = new ComboBox<>();
        slotListView = new ListView<>();
        timeComboBox = new ComboBox<>();
        btnBookAppointment = new Button("Book Appointment");
        statusLabel = new Label();

        // Apply styling to components
        styleComboBoxString(doctorComboBox);
        styleListView(slotListView);
        styleComboBoxTime(timeComboBox);
        styleButton(btnBookAppointment);
        styleLabel(statusLabel);

        // Set up event handlers
        btnBookAppointment.setOnAction(e -> bookAppointment());
        doctorComboBox.setOnAction(e -> loadAvailableSlots());
        slotListView.setOnMouseClicked(e -> loadAvailableTimes());

        // Create layout containers for each section
        VBox doctorBox = new VBox(10, createTitleLabel("Select Doctor:"), doctorComboBox);
        VBox slotBox = new VBox(10, createTitleLabel("Available Days:"), slotListView);
        VBox timeBox = new VBox(10, createTitleLabel("Select Time:"), timeComboBox);
        VBox bookingBox = new VBox(20, btnBookAppointment, statusLabel);

        // Center align all containers
        doctorBox.setAlignment(Pos.CENTER);
        slotBox.setAlignment(Pos.CENTER);
        timeBox.setAlignment(Pos.CENTER);
        bookingBox.setAlignment(Pos.CENTER);

        // Main layout container
        VBox mainLayout = new VBox(25, doctorBox, slotBox, timeBox, bookingBox);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: #0D1B2A;");  // Dark blue background

        // Set up and show the scene
        Scene scene = new Scene(mainLayout, 600, 600);
        primaryStage.setTitle("Patient Appointment Booking");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load initial doctor data
        loadDoctors();
    }

    /**
     * Loads all doctors from the database into the doctor combo box
     */
    private void loadDoctors() {
        String query = "SELECT id, name, specialization FROM doctors";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            doctorComboBox.getItems().clear();
            while (rs.next()) {
                // Format doctor information for display
                String doctorInfo = "ID:" + rs.getInt("id") +
                        " | " + rs.getString("name") +
                        " (" + rs.getString("specialization") + ")";
                doctorComboBox.getItems().add(doctorInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads available time slots for the selected doctor
     */
    private void loadAvailableSlots() {
        String selectedDoctor = doctorComboBox.getSelectionModel().getSelectedItem();
        if (selectedDoctor == null) return;

        // Extract doctor ID from the selected string
        selectedDoctorId = extractDoctorId(selectedDoctor);

        String query = "SELECT id, available_date, start_time, end_time FROM doctor_availability WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selectedDoctorId);
            ResultSet rs = stmt.executeQuery();

            slotListView.getItems().clear();
            while (rs.next()) {
                // Format slot information for display
                String slotInfo = "SlotID:" + rs.getInt("id") +
                        " | Date: " + rs.getDate("available_date") +
                        " | " + rs.getString("start_time") + " to " + rs.getString("end_time");
                slotListView.getItems().add(slotInfo);
            }
            timeComboBox.getItems().clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads available times for the selected time slot
     */
    private void loadAvailableTimes() {
        String selectedSlot = slotListView.getSelectionModel().getSelectedItem();
        if (selectedSlot == null) return;

        selectedSlotId = extractSlotId(selectedSlot);
        timeComboBox.getItems().clear();

        String query = "SELECT start_time, end_time FROM doctor_availability WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selectedSlotId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Parse start and end times
                LocalTime start = LocalTime.parse(rs.getString("start_time"));
                LocalTime end = LocalTime.parse(rs.getString("end_time"));

                // Generate 30-minute time slots between start and end times
                while (start.isBefore(end)) {
                    timeComboBox.getItems().add(start);
                    start = start.plusMinutes(30);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Books an appointment with the selected doctor, time slot, and time
     */
    private void bookAppointment() {
        String selectedSlot = slotListView.getSelectionModel().getSelectedItem();
        LocalTime selectedTime = timeComboBox.getSelectionModel().getSelectedItem();

        if (selectedSlot == null || selectedTime == null) {
            statusLabel.setText("Please select a slot and time.");
            return;
        }

        selectedSlotId = extractSlotId(selectedSlot);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);  // Start transaction

            // Get details of the selected slot
            String fetchSlotQuery = "SELECT available_date FROM doctor_availability WHERE id = ?";
            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchSlotQuery)) {
                fetchStmt.setInt(1, selectedSlotId);
                ResultSet slotRs = fetchStmt.executeQuery();

                if (!slotRs.next()) {
                    statusLabel.setText("Selected slot no longer available.");
                    conn.rollback();
                    return;
                }

                Date appointmentDate = slotRs.getDate("available_date");
                LocalTime startTime = selectedTime;
                LocalTime endTime = startTime.plusHours(1);  // Assume 1-hour appointments

                // Check for scheduling conflicts
                String conflictQuery = "SELECT * FROM appointments WHERE doctor_id = ? AND appointment_date = ? " +
                        "AND ((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?))";
                try (PreparedStatement conflictStmt = conn.prepareStatement(conflictQuery)) {
                    conflictStmt.setInt(1, selectedDoctorId);
                    conflictStmt.setDate(2, appointmentDate);
                    conflictStmt.setTime(3, Time.valueOf(endTime));
                    conflictStmt.setTime(4, Time.valueOf(endTime));
                    conflictStmt.setTime(5, Time.valueOf(startTime));
                    conflictStmt.setTime(6, Time.valueOf(startTime));

                    ResultSet conflictRs = conflictStmt.executeQuery();
                    if (conflictRs.next()) {
                        statusLabel.setText("Doctor unavailable at selected time.");
                        conn.rollback();
                        return;
                    }
                }

                // Create the appointment record
                int generatedAppointmentId;
                String insertAppointmentQuery = "INSERT INTO appointments (doctor_id, patient_id, appointment_date, start_time, end_time, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertAppointmentQuery, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setInt(1, selectedDoctorId);
                    insertStmt.setInt(2, patientId);
                    insertStmt.setDate(3, appointmentDate);
                    insertStmt.setTime(4, Time.valueOf(startTime));
                    insertStmt.setTime(5, Time.valueOf(endTime));
                    insertStmt.setString(6, "Pending");
                    insertStmt.executeUpdate();
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        generatedAppointmentId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Failed to get generated appointment ID.");
                    }
                }

                // Create doctor-patient assignment record
                String insertAssignmentQuery = "INSERT INTO doctorpatientassignment (DoctorID, PatientID, AppointmentID, AssignedDate) " +
                        "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement assignStmt = conn.prepareStatement(insertAssignmentQuery)) {
                    assignStmt.setInt(1, selectedDoctorId);
                    assignStmt.setInt(2, patientId);
                    assignStmt.setInt(3, generatedAppointmentId);
                    assignStmt.executeUpdate();
                }

                conn.commit();  // Commit transaction
                statusLabel.setText("Appointment and Assignment created successfully!");
                loadAvailableTimes();

                // Get doctor details for email notification
                String doctorEmail = null;
                String doctorName = null;
                String doctorQuery = "SELECT name, email FROM doctors WHERE id = ?";
                try (PreparedStatement doctorStmt = conn.prepareStatement(doctorQuery)) {
                    doctorStmt.setInt(1, selectedDoctorId);
                    ResultSet rs = doctorStmt.executeQuery();
                    if (rs.next()) {
                        doctorEmail = rs.getString("email");
                        doctorName = rs.getString("name");
                    }
                }

                // Get patient details for email notification
                String patientName = null;
                String patientQuery = "SELECT name FROM patients WHERE id = ?";
                try (PreparedStatement patientStmt = conn.prepareStatement(patientQuery)) {
                    patientStmt.setInt(1, patientId);
                    ResultSet rs = patientStmt.executeQuery();
                    if (rs.next()) {
                        patientName = rs.getString("name");
                    }
                }

                // Send email notification if we have all required information
                if (doctorEmail != null && patientName != null) {
                    String subject = "🩺 New Appointment Request from " + patientName;
                    String emailMessage = "Dear Dr. " + doctorName + ",\n\n" +
                            "You have a new appointment request:\n\n" +
                            "Patient: " + patientName + "\n" +
                            "Date: " + appointmentDate + "\n" +
                            "Time: " + startTime + " - " + endTime + "\n\n" +
                            "Please review it in your dashboard.\n\n" +
                            "Regards,\nHospital Management System";

                    EmailNotification emailSender = new EmailNotification();
                    emailSender.sendNotification(subject, emailMessage, doctorEmail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error booking appointment.");
        }
    }

    // Helper methods would follow here (styleComboBoxString, styleListView, etc.)
    // These would handle the visual styling of UI components


    /**
     * Extracts the doctor ID from a formatted string (e.g., "ID:123 | Dr. Smith")
     *
     * @param selected The formatted string containing the doctor ID
     * @return The extracted doctor ID, or -1 if extraction fails
     */
    private int extractDoctorId(String selected) {
        try {
            // Split the string by pipe character and get first part (e.g., "ID:123")
            String idPart = selected.split("\\|")[0].trim();
            // Split by colon and get the numeric part after "ID:"
            return Integer.parseInt(idPart.split(":")[1].trim());
        } catch (Exception e) {
            return -1; // Return -1 if any parsing error occurs
        }
    }

    /**
     * Extracts the slot ID from a formatted string (e.g., "SlotID:456 | Date: 2023-05-20")
     *
     * @param selected The formatted string containing the slot ID
     * @return The extracted slot ID, or -1 if extraction fails
     */
    private int extractSlotId(String selected) {
        try {
            // Same logic as extractDoctorId but for slot IDs
            String idPart = selected.split("\\|")[0].trim();
            return Integer.parseInt(idPart.split(":")[1].trim());
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Applies consistent styling to ListView components
     *
     * @param listView The ListView component to style
     */
    private void styleListView(ListView<String> listView) {
        listView.setStyle("-fx-control-inner-background: #1B263B; " +  // Dark blue background
                "-fx-text-fill: white; " +                     // White text
                "-fx-font-size: 14px; " +                     // Font size
                "-fx-border-color: #3A86FF; " +               // Light blue border
                "-fx-border-radius: 5px;");                   // Rounded corners
        listView.setPrefWidth(230);    // Set preferred width
        listView.setPrefHeight(200);   // Set preferred height
    }

    /**
     * Applies consistent styling to ComboBox components for time selection
     *
     * @param comboBox The ComboBox component to style
     */
    private void styleComboBoxTime(ComboBox<LocalTime> comboBox) {
        comboBox.setStyle("-fx-background-color: #1B263B; " +  // Dark blue background
                "-fx-text-fill: white; " +            // White text
                "-fx-font-size: 14px; " +            // Font size
                "-fx-border-color: #3A86FF; " +      // Light blue border
                "-fx-border-radius: 5px;");          // Rounded corners
    }

    /**
     * Applies consistent styling to ComboBox components for string selection
     *
     * @param comboBox The ComboBox component to style
     */
    private void styleComboBoxString(ComboBox<String> comboBox) {
        comboBox.setStyle("-fx-background-color: #1B263B; " +  // Dark blue background
                "-fx-text-fill: white; " +           // White text
                "-fx-font-size: 14px; " +           // Font size
                "-fx-border-color: #3A86FF; " +     // Light blue border
                "-fx-border-radius: 5px;");         // Rounded corners
        comboBox.setPrefWidth(350);  // Wider than time combo boxes
    }

    /**
     * Applies consistent styling to Button components
     *
     * @param button The Button component to style
     */
    private void styleButton(Button button) {
        button.setStyle("-fx-background-color: #3A86FF; " +  // Light blue background
                "-fx-text-fill: white; " +           // White text
                "-fx-font-size: 16px; " +           // Slightly larger font
                "-fx-font-weight: bold; " +          // Bold text
                "-fx-background-radius: 8px;");      // More rounded corners
        button.setPrefWidth(180);  // Fixed width for consistency
    }

    /**
     * Applies consistent styling to Label components
     *
     * @param label The Label component to style
     */
    private void styleLabel(Label label) {
        label.setStyle("-fx-text-fill: #E0E1DD; " +  // Light gray text
                "-fx-font-size: 14px; " +     // Font size
                "-fx-font-weight: bold;");    // Bold text
    }

    /**
     * Creates a consistently styled title label with specific text
     *
     * @param text The text to display in the label
     * @return A styled Label component
     */
    private Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #3A86FF; " +  // Light blue text
                "-fx-font-size: 16px; " +    // Larger font size
                "-fx-font-weight: bold;");   // Bold text
        return label;
    }

    /**
     * Main entry point for the JavaFX application
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);  // Standard JavaFX application launch
    }
}