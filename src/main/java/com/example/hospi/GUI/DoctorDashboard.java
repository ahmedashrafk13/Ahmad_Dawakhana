package com.example.hospi.GUI;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DoctorDashboard - The main interface for doctors in the Lifeline Remote Hospital system.
 * Provides access to patient management, appointment scheduling, and medical tools.
 */
public class DoctorDashboard extends Application {
    private int doctorId; // Default doctor ID (would normally be set during login)

    DoctorDashboard(int doctorId){
        this.doctorId = doctorId;
    }

    /**
     * Main entry point for the JavaFX application.
     *
     * @param stage The primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        // Fetch doctor information from database
        String doctorName = getDoctorName(doctorId);
        String doctorUsername = getDoctorUsername(doctorId);

        // Get data for dashboard sections
        List<String> patients = getAssignedPatients(doctorId);
        List<String> appointments = getAppointmentsForToday(doctorId);

        // ========== UI COMPONENT CREATION ========== //

        // 1. Header Section (contains hospital name, welcome message, logout)
        VBox header = createHeader(stage, doctorName);

        // 2. Patients Section (lists patients and related actions)
        VBox patientsSection = createSection("\uD83D\uDC68\u200D⚕️ Your Patients",
                createStyledListView(patients),
                createHorizontalButtonGroup(
                        createButton("View Vitals", e -> openVitalsPage()),
                        createButton("Provide Feedback", e -> openProvideFeedbackWindow()),
                        createButton("Prescribe Medicine", e -> openPrescribeMedicineWindow())
                )
        );

        // 3. Appointments Section (today's schedule and actions)
        VBox appointmentsSection = createSection("\uD83D\uDCC5 Today's Appointments",
                createAppointmentsSection(appointments),
                createHorizontalButtonGroup(
                        createButton("Schedule Appointment", e -> openAppointmentSchedulingWindow()),
                        createButton("Start Video Call", e -> openVideoCallPage())
                )
        );

        // 4. Tools Section (doctor utilities)
        VBox toolsSection = createSection("\uD83D\uDEE0️ Doctor Tools",
                createHorizontalButtonGroup(
                        createButton("View Health Trends", e -> openViewTrendsDoctorPage()),
                        createButton("Generate Report", e -> GenerateReport.generatePatientReportUI(doctorId)),
                        createButton("Start Consultation", e -> openChatClient(doctorUsername))
                )
        );

        // ========== MAIN LAYOUT CONSTRUCTION ========== //

        // Combine all sections vertically
        VBox content = new VBox(20,
                header,
                patientsSection,
                appointmentsSection,
                toolsSection
        );
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #0d1b2a;"); // Dark blue background
        content.setAlignment(Pos.TOP_CENTER);

        // Make scrollable for smaller screens
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0d1b2a;");

        // Set up the main scene
        Scene scene = new Scene(scrollPane, 900, 750);
        stage.setScene(scene);
        stage.setTitle("Doctor Dashboard - Lifeline Remote Hospital");
        stage.show();
    }

    /**
     * Creates the header section with hospital name, welcome message, and logout button.
     *
     * @param stage      The main application stage (for logout functionality)
     * @param doctorName The name of the logged-in doctor
     * @return Configured VBox containing header components
     */
    private VBox createHeader(Stage stage, String doctorName) {
        // Hospital branding
        Label hospitalNameLabel = new Label("Lifeline Remote Hospital");
        hospitalNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 38));
        hospitalNameLabel.setTextFill(Color.web("#87CEEB")); // Sky blue color

        // Personalized welcome message
        Label welcomeLabel = new Label("Welcome, Dr. " + doctorName);
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));
        welcomeLabel.setTextFill(Color.web("#D0E8F2")); // Light blue color

        // Logout button that returns to login screen
        Button logoutButton = createButton("Logout", e -> {
            stage.close();
            try {
                new Loginpage().start(new Stage()); // Return to login screen
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        logoutButton.setPrefWidth(100);

        // Header layout with flexible spacing
        HBox headerRow = new HBox(15, hospitalNameLabel, new Region(), logoutButton);
        HBox.setHgrow(headerRow.getChildren().get(1), Priority.ALWAYS); // Pushes elements to sides
        headerRow.setPadding(new Insets(20, 30, 20, 30));

        return new VBox(10, headerRow, welcomeLabel);
    }

    /**
     * Creates a standardized content section with title and components.
     *
     * @param title The section title (with optional emoji)
     * @param nodes The UI components to include in the section
     * @return Configured VBox representing the content section
     */
    private VBox createSection(String title, Node... nodes) {
        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        sectionTitle.setTextFill(Color.web("#87CEEB")); // Consistent with header

        VBox section = new VBox(15); // 15px vertical spacing
        section.getChildren().add(sectionTitle);
        section.getChildren().addAll(nodes);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #102841; -fx-background-radius: 10;");
        return section;
    }

    /**
     * Creates a styled button with consistent appearance.
     *
     * @param text    The button label text
     * @param handler The action to perform when clicked
     * @return Configured Button instance
     */
    private Button createButton(String text, EventHandler<ActionEvent> handler) {
        Button button = new Button(text);
        button.setPrefWidth(220); // Consistent button width
        button.setStyle(
                "-fx-background-color: #3498db; " + // Blue background
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8;"); // Rounded corners
        button.setOnAction(handler);
        return button;
    }

    /**
     * Groups buttons horizontally with consistent spacing.
     *
     * @param buttons The buttons to include in the group
     * @return Configured HBox containing the buttons
     */
    private HBox createHorizontalButtonGroup(Button... buttons) {
        HBox box = new HBox(15); // 15px horizontal spacing
        box.getChildren().addAll(buttons);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /**
     * Creates a styled ListView for displaying patient lists.
     *
     * @param items The list of strings to display
     * @return Configured ListView instance
     */
    private ListView<String> createStyledListView(List<String> items) {
        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(items);
        listView.setPrefHeight(150); // Reasonable default height
        listView.setStyle("-fx-control-inner-background: #2e2e2e; -fx-text-fill: white;");
        return listView;
    }

    /**
     * Creates a vertical list of appointments with simple labels.
     *
     * @param appointments The list of appointment strings
     * @return Configured VBox containing appointment labels
     */
    private VBox createAppointmentsSection(List<String> appointments) {
        VBox box = new VBox(7); // Tight vertical spacing for appointments
        appointments.forEach(a -> {
            Label lbl = new Label(a);
            lbl.setTextFill(Color.LIGHTGRAY); // Light text on dark background
            box.getChildren().add(lbl);
        });
        return box;
    }


    // ========== NAVIGATION METHODS ========== //

    /**
     * Opens the patient vitals viewing page in a new window.
     * Launches the ViewVitals screen to display patient health metrics.
     */
    private void openVitalsPage() {
        new ViewVitals(doctorId).start(new Stage());
    }

    /**
     * Opens the feedback provision window in a new window.
     * Allows doctors to provide feedback about patients.
     */
    private void openProvideFeedbackWindow() {
        new ProvideFeedback(doctorId).start(new Stage());
    }

    /**
     * Opens the medicine prescription window in a new window.
     * Enables doctors to create and send prescriptions to patients.
     */
    private void openPrescribeMedicineWindow() {
        new PrescribeMedicine(doctorId).start(new Stage());
    }

    /**
     * Opens the appointment scheduling interface in a new window.
     * Provides doctors with tools to manage patient appointments.
     */
    private void openAppointmentSchedulingWindow() {
        new DoctorAppointment(doctorId).start(new Stage());
    }

    /**
     * Opens the health trends analysis page in a new window.
     * Displays graphical representations of patient health data over time.
     */
    private void openViewTrendsDoctorPage() {
        new ViewTrendsDoctor(doctorId).start(new Stage());
    }

    /**
     * Initiates a video call session in a new window.
     * Uses the doctor's ID to establish the video consultation.
     */
    private void openVideoCallPage() {
        new DoctorVideoCall(doctorId).start(new Stage());
    }

    /**
     * Launches the chat client for doctor-patient communication.
     *
     * @param doctorUsername The username of the logged-in doctor
     */
    private void openChatClient(String doctorUsername) {
        // Validate username input
        if (doctorUsername == null || doctorUsername.isBlank()) {
            showAlert("Doctor username not found.");
            return;
        }

        // Get additional doctor information needed for chat
        int doctorUserId = getDoctorUserIdFromUsername(doctorUsername);
        String doctorRole = getDoctorRole(doctorUserId);

        // Verify information was retrieved successfully
        if (doctorUserId == -1 || doctorRole == null) {
            showAlert("Failed to retrieve doctor information.");
            return;
        }

        // Launch chat interface with proper identification
        new ChatClient(doctorUserId, doctorRole).start(new Stage());
    }

    /**
     * Displays an informational alert dialog.
     *
     * @param msg The message to display in the alert
     */
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ========== DATABASE METHODS ========== //

    /**
     * Retrieves the user ID associated with a doctor's username.
     *
     * @param username The doctor's login username
     * @return The user ID if found, -1 otherwise
     */
    private int getDoctorUserIdFromUsername(String username) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String query = "SELECT id FROM hospital_db.users WHERE username = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Gets the role of a user from the database.
     *
     * @param userId The ID of the user to look up
     * @return The user's role if found, null otherwise
     */
    private String getDoctorRole(int userId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String query = "SELECT role FROM hospital_db.users WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a doctor's full name from the database.
     *
     * @param doctorId The ID of the doctor
     * @return The doctor's name if found, "Unknown" otherwise
     */
    private String getDoctorName(int doctorId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String sql = "SELECT name FROM hospital_db.doctors WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, doctorId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    /**
     * Gets the username associated with a doctor's ID.
     *
     * @param doctorId The ID of the doctor
     * @return The username if found, null otherwise
     */
    private String getDoctorUsername(int doctorId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String query = "SELECT username FROM hospital_db.users WHERE id = (SELECT user_id FROM hospital_db.doctors WHERE id = ?)";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, doctorId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the list of patients assigned to a specific doctor.
     *
     * @param doctorId The ID of the doctor
     * @return List of patient names with person emoji prefix
     */
    private List<String> getAssignedPatients(int doctorId) {
        List<String> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.name FROM hospital_db.patients p JOIN hospital_db.doctorpatientassignment d ON p.id = d.PatientID WHERE d.DoctorID = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, doctorId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) list.add("👤 " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Gets today's appointments for a specific doctor.
     *
     * @param doctorId The ID of the doctor
     * @return List of formatted appointment strings including:
     * - Clock emoji
     * - Appointment time
     * - Patient name
     * - Appointment status in brackets
     */
    private List<String> getAppointmentsForToday(int doctorId) {
        List<String> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection()) {
            String sql = "SELECT a.start_time, p.name, a.status FROM hospital_db.appointments a JOIN hospital_db.patients p ON a.patient_id = p.id WHERE a.doctor_id = ? AND a.appointment_date = CURDATE()";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, doctorId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    list.add("⏰ " + rs.getString("start_time") + " - " + rs.getString("name") + " [" + rs.getString("status") + "]");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Main entry point for the Doctor Dashboard application.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}