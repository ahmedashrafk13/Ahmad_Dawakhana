package com.example.hospi.GUI;

import javafx.beans.value.ObservableValue;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * DoctorVideoCall - Interface for doctors to manage video call appointments.
 * Allows doctors to view pending video call requests and accept/reject them.
 */
public class DoctorVideoCall extends Application {
    private int doctorId; // ID of the logged-in doctor
    private Connection conn; // Database connection

    // Theme colors for consistent UI styling
    private static final String BACKGROUND_COLOR = "#0d1b2a"; // Dark blue background
    private static final String SECTION_BACKGROUND = "#102841"; // Slightly lighter blue for sections
    private static final Color TEXT_COLOR = Color.web("#E0F7FA"); // Light cyan text
    private static final Color ACCENT_COLOR = Color.web("#87CEEB"); // Sky blue accents
    private static final String BUTTON_STYLE = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;";

    /**
     * Constructor for DoctorVideoCall
     * @param doctorId The ID of the doctor using this interface
     */
    public DoctorVideoCall(int doctorId) {
        this.doctorId = doctorId;
    }

    /**
     * Main JavaFX start method
     * @param stage The primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        try {
            this.conn = DatabaseConnection.getConnection();

            // ========== MAIN LAYOUT ========== //
            VBox mainLayout = new VBox(15); // 15px vertical spacing
            mainLayout.setPadding(new Insets(20)); // 20px padding around edges
            mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

            // ========== HEADER SECTION ========== //
            Label headerLabel = new Label("Video Call Appointments");
            headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
            headerLabel.setTextFill(ACCENT_COLOR);

            // ========== APPOINTMENTS TABLE ========== //
            TableView<AppointmentData> table = new TableView<>();
            table.setMinWidth(600); // Minimum width for better visibility
            table.setStyle("-fx-background-color: " + SECTION_BACKGROUND + ";");

            // Create table columns
            TableColumn<AppointmentData, Integer> idColumn = createTableColumn("Appointment ID", "appointmentId");
            TableColumn<AppointmentData, String> patientColumn = createTableColumn("Patient ID", "patientId");
            TableColumn<AppointmentData, LocalDateTime> timeColumn = createTableColumn("Appointment Time", "appointmentTime");
            TableColumn<AppointmentData, String> statusColumn = createTableColumn("Status", "status");

            table.getColumns().addAll(idColumn, patientColumn, timeColumn, statusColumn);

            // ========== ACTION BUTTONS ========== //
            HBox buttonBox = new HBox(15); // 15px horizontal spacing
            buttonBox.setAlignment(Pos.CENTER);

            Button acceptButton = createStyledButton("Accept Appointment");
            Button rejectButton = createStyledButton("Reject Appointment");

            // Set button actions
            acceptButton.setOnAction(e -> handleAppointmentResponse(table, true));
            rejectButton.setOnAction(e -> handleAppointmentResponse(table, false));

            buttonBox.getChildren().addAll(acceptButton, rejectButton);

            // ========== FINAL ASSEMBLY ========== //
            mainLayout.getChildren().addAll(headerLabel, table, buttonBox);

            // Scene setup
            Scene scene = new Scene(mainLayout, 800, 500);
            stage.setTitle("Doctor Video Call Appointments");
            stage.setScene(scene);
            stage.show();

            // Load initial appointment data
            getAppointments(table);

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Error connecting to database");
            e.printStackTrace();
        }
    }

    // ========== GETTERS/SETTERS ========== //

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public Connection getConnection() {
        return conn;
    }

    // ========== UI HELPER METHODS ========== //

    /**
     * Creates a styled table column with appropriate data binding
     * @param <T> The column data type
     * @param title Column header text
     * @param property The property name to bind to
     * @return Configured TableColumn instance
     */
    private <T> TableColumn<AppointmentData, T> createTableColumn(String title, String property) {
        TableColumn<AppointmentData, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            // Dynamic property binding based on requested property
            if (property.equals("appointmentId")) {
                return (ObservableValue<T>) cellData.getValue().appointmentIdProperty().asObject();
            } else if (property.equals("patientId")) {
                return (ObservableValue<T>) cellData.getValue().patientIdProperty().asString();
            } else if (property.equals("appointmentTime")) {
                return (ObservableValue<T>) cellData.getValue().appointmentTimeProperty();
            } else {
                return (ObservableValue<T>) cellData.getValue().statusProperty();
            }
        });
        column.setStyle("-fx-text-fill: " + TEXT_COLOR.toString().replace("0x", "#") + ";");
        return column;
    }

    /**
     * Creates a consistently styled button
     * @param text The button label
     * @return Styled Button instance
     */
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle(BUTTON_STYLE);
        button.setPrefWidth(180); // Consistent width
        button.setPrefHeight(35); // Consistent height
        return button;
    }

    // ========== BUSINESS LOGIC METHODS ========== //

    /**
     * Handles doctor's response to an appointment (accept/reject)
     * @param table The appointments table
     * @param accept True for accept, false for reject
     */
    private void handleAppointmentResponse(TableView<AppointmentData> table, boolean accept) {
        AppointmentData selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                respondToAppointment(selected.getAppointmentId(), accept);
                table.getItems().remove(selected);
                showAlert("Success", "Appointment " + (accept ? "accepted" : "rejected") + " successfully");
            } catch (SQLException ex) {
                showErrorAlert("Database Error", "Failed to update appointment status");
                ex.printStackTrace();
            }
        } else {
            showAlert("No Selection", "Please select an appointment first");
        }
    }

    /**
     * Loads pending video call appointments from database
     * @param table The table to populate with appointments
     * @throws SQLException If database access fails
     */
    public void getAppointments(TableView<AppointmentData> table) throws SQLException {
        String query = """
            SELECT id, patient_id, appointment_time, status
            FROM video_call_appointments
            WHERE doctor_id = ? AND status = 'PENDING'
            ORDER BY appointment_time ASC
        """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    table.getItems().add(new AppointmentData(
                            rs.getInt("id"),
                            rs.getInt("patient_id"),
                            rs.getTimestamp("appointment_time").toLocalDateTime(),
                            rs.getString("status")
                    ));
                }
            }
        }
    }

    /**
     * Updates appointment status in database
     * @param appointmentId The ID of the appointment to update
     * @param accept Whether to accept (true) or reject (false)
     * @throws SQLException If database update fails
     */
    public void respondToAppointment(int appointmentId, boolean accept) throws SQLException {
        String status = accept ? "ACCEPTED" : "REJECTED";
        String meetingLink = accept ? generateMeetingLink() : null;

        String update = """
            UPDATE video_call_appointments
            SET status = ?, meeting_link = ?
            WHERE id = ? AND doctor_id = ?
        """;
        try (PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setString(1, status);
            stmt.setString(2, meetingLink);
            stmt.setInt(3, appointmentId);
            stmt.setInt(4, doctorId);
            stmt.executeUpdate();

            if (accept) {
                sendEmailToBothUsers(appointmentId, meetingLink);
            }
        }
    }

    /**
     * Sends confirmation email to both doctor and patient
     * @param appointmentId The appointment ID
     * @param link The meeting link (for accepted appointments)
     * @throws SQLException If database access fails
     */
    private void sendEmailToBothUsers(int appointmentId, String link) throws SQLException {
        String query = """
            SELECT p.address AS patient_email, d.email AS doctor_email, a.appointment_time
            FROM video_call_appointments a
            JOIN patients p ON a.patient_id = p.id
            JOIN doctors d ON a.doctor_id = d.id
            WHERE a.id = ?
        """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String patientEmail = rs.getString("patient_email");
                    String doctorEmail = rs.getString("doctor_email");
                    LocalDateTime time = rs.getTimestamp("appointment_time").toLocalDateTime();

                    String subject = "Video Call Appointment Confirmed";
                    String body = "Your appointment is scheduled for " + time +
                            "\nJoin via: " + link;

                    EmailNotification email = new EmailNotification();
                    email.sendNotification(subject, body, patientEmail);
                    email.sendNotification(subject, body, doctorEmail);
                }
            }
        }
    }

    /**
     * Generates a unique meeting link
     * @return Generated meeting URL
     */
    private String generateMeetingLink() {
        return "https://meet.jit.si/Meet-" + System.currentTimeMillis();
    }

    // ========== ALERT/DIALOG METHODS ========== //

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        styleAlert(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        styleAlert(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Applies consistent styling to alert dialogs
     * @param alert The alert to style
     */
    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + SECTION_BACKGROUND + ";");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: " + TEXT_COLOR.toString().replace("0x", "#") + ";");
    }

    // ========== DATA MODEL CLASS ========== //

    /**
     * AppointmentData - Model class for video call appointment information
     */
    public static class AppointmentData {
        private final IntegerProperty appointmentId;
        private final IntegerProperty patientId;
        private final ObjectProperty<LocalDateTime> appointmentTime;
        private final StringProperty status;

        public AppointmentData(int appointmentId, int patientId, LocalDateTime appointmentTime, String status) {
            this.appointmentId = new SimpleIntegerProperty(appointmentId);
            this.patientId = new SimpleIntegerProperty(patientId);
            this.appointmentTime = new SimpleObjectProperty<>(appointmentTime);
            this.status = new SimpleStringProperty(status);
        }

        // Standard getters
        public int getAppointmentId() { return appointmentId.get(); }
        public int getPatientId() { return patientId.get(); }
        public LocalDateTime getAppointmentTime() { return appointmentTime.get(); }
        public String getStatus() { return status.get(); }

        // Property getters for JavaFX binding
        public IntegerProperty appointmentIdProperty() { return appointmentId; }
        public IntegerProperty patientIdProperty() { return patientId; }
        public ObjectProperty<LocalDateTime> appointmentTimeProperty() { return appointmentTime; }
        public StringProperty statusProperty() { return status; }
    }
}