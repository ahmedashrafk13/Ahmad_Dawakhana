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
import java.sql.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.io.File;
import javafx.stage.FileChooser;

/**
 * PatientDashboard - Main dashboard interface for patients in the hospital system.
 * Displays vital health information, appointments, medications, and communication tools.
 */
public class PatientDashboard extends Application {

    private GridPane vitalsGrid;        // Grid layout for displaying vital signs
    private int patientId;             // ID of the current patient
    private String patientName;        // Name of the current patient
    private Patient patient;           // Patient object containing additional details

    /**
     * Constructor with patient ID and name
     *
     * @param patientId   Unique identifier for the patient
     * @param patientName Full name of the patient
     */
    public PatientDashboard(int patientId, String patientName) {
        this.patientId = patientId;
        this.patientName = patientName;
    }

    /**
     * Default constructor with demo values
     */
    public PatientDashboard() {
        this(1, "Patient");
    }

    // Getters and setters with input validation

    /**
     * @return The patient's unique identifier
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * Sets the patient ID with validation
     *
     * @param patientId Must be positive number
     */
    public void setPatientId(int patientId) {
        this.patientId = patientId > 0 ? patientId : 1;
    }

    /**
     * @return The patient's full name
     */
    public String getPatientName() {
        return patientName;
    }

    /**
     * Sets the patient name with validation
     *
     * @param patientName Cannot be null or blank
     */
    public void setPatientName(String patientName) {
        this.patientName = (patientName == null || patientName.isBlank()) ? "Patient" : patientName;
    }

    /**
     * @return The complete Patient object
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * Sets the Patient object
     *
     * @param patient Patient object containing medical details
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    /**
     * Main JavaFX entry point
     *
     * @param primaryStage The primary window for this application
     */
    @Override
    public void start(Stage primaryStage) {
        initializeFallbacks(); // Ensure valid default values

        // Main application layout
        BorderPane root = new BorderPane();
        root.setTop(createHeader(primaryStage)); // Header with hospital name and logout

        // Scrollable main content area
        ScrollPane scrollPane = new ScrollPane(createMainContent());
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0d1b2a; -fx-border-color: transparent;");
        root.setCenter(scrollPane);
        root.setStyle("-fx-background-color: #0d1b2a;"); // Dark blue background

        // Set up and show the window
        Scene scene = new Scene(root, 900, 750);
        primaryStage.setTitle("Patient Dashboard - Lifeline Remote Hospital");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Ensures valid fallback values if none provided
     */
    private void initializeFallbacks() {
        if (patientName == null || patientName.isBlank()) patientName = "Patient";
        if (patientId <= 0) patientId = 1;
    }

    /**
     * Creates the application header with hospital logo and logout button
     *
     * @param stage Reference to main window for logout functionality
     * @return Configured HBox containing header elements
     */
    private HBox createHeader(Stage stage) {
        // Hospital name/logo
        Label hospitalName = new Label("🏥 Lifeline Remote Hospital");
        hospitalName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        hospitalName.setTextFill(Color.web("#87CEEB")); // Light blue color

        // Spacer to push logout button to right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Logout button with red background
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        logoutBtn.setOnAction(e -> {
            stage.close(); // Close current window

            // Restart login screen
            try {
                new Loginpage().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Assemble header components
        HBox topBar = new HBox(15, hospitalName, spacer, logoutBtn);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #102841;"); // Darker blue background
        return topBar;
    }

    /**
     * Creates the main content area with all dashboard sections
     *
     * @return VBox containing all content sections
     */
    private VBox createMainContent() {
        // Welcome message personalized with patient name
        Label welcome = new Label("Welcome, " + patientName);
        welcome.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));
        welcome.setTextFill(Color.web("#D0E8F2")); // Light blue-gray
        HBox welcomeBox = new HBox(welcome);
        welcomeBox.setAlignment(Pos.CENTER_LEFT);
        welcomeBox.setPadding(new Insets(20, 30, 10, 30));

        // Assemble all content sections
        VBox content = new VBox(20,
                welcomeBox,
                createSection("❤️ Vitals Overview", createVitalsSection()),
                createSection("📅 Appointments", createAppointmentsSection(), createAppointmentActions()),
                createSection("💊 Medications", createMedicationsSection(), createMedicationActions()),
                createSection("📝 Feedback & Communication", createFeedbackSection(), createCommunicationActions()),
                createSection("📊 Health Data", createHealthDataActions())
        );
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);
        return content;
    }

    /**
     * Creates a standardized section container
     *
     * @param title Section header text
     * @param nodes Child elements to include in section
     * @return Styled VBox containing the section
     */
    private VBox createSection(String title, Node... nodes) {
        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        sectionTitle.setTextFill(Color.web("#87CEEB")); // Light blue

        VBox section = new VBox(15);
        section.getChildren().add(sectionTitle);
        section.getChildren().addAll(nodes);
        section.setPadding(new Insets(15));
        section.setSpacing(10);
        section.setStyle("-fx-background-color: #102841; -fx-background-radius: 10;");
        return section;
    }

    /**
     * Creates the vitals monitoring section
     *
     * @return VBox containing current vital signs
     */
    private VBox createVitalsSection() {
        Label title = createSectionTitle("Health Vitals");

        // Grid layout for vitals display
        vitalsGrid = new GridPane();
        vitalsGrid.setHgap(20);
        vitalsGrid.setVgap(10);

        // Load latest vitals from database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT heart_rate, blood_pressure, oxygen_level FROM vitals WHERE patient_id = ? ORDER BY recorded_at DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                updateVitals(rs.getString("heart_rate"), rs.getString("blood_pressure"), rs.getString("oxygen_level"));
            } else {
                updateVitals("N/A", "N/A", "N/A"); // Default values if no records
            }
        } catch (SQLException e) {
            updateVitals("Error", "Error", "Error"); // Error state
            e.printStackTrace();
        }

        VBox box = new VBox(10, title, vitalsGrid);
        styleSectionBox(box);
        return box;
    }

    /**
     * Creates the appointments listing section
     *
     * @return VBox containing upcoming appointments
     */
    private VBox createAppointmentsSection() {
        Label title = createSectionTitle("Upcoming Appointments");
        VBox appointmentsBox = new VBox(5);

        // Load appointments from database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT appointment_date, start_time, end_time, doctor_id FROM appointments WHERE patient_id = ? ORDER BY appointment_date ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            // Add each appointment to the list
            while (rs.next()) {
                String appointmentDate = rs.getString("appointment_date");
                String startTime = rs.getString("start_time");
                String endTime = rs.getString("end_time");
                int doctorId = rs.getInt("doctor_id");
                appointmentsBox.getChildren().add(createColoredLabel("\u2714 " + appointmentDate + ": " + startTime + "–" + endTime + " with Dr. " + doctorId));
            }

            if (appointmentsBox.getChildren().isEmpty()) {
                appointmentsBox.getChildren().add(createColoredLabel("No upcoming appointments."));
            }
        } catch (SQLException e) {
            appointmentsBox.getChildren().add(createColoredLabel("Error loading appointments."));
            e.printStackTrace();
        }

        VBox box = new VBox(10, title, appointmentsBox);
        styleSectionBox(box);
        return box;
    }

    /**
     * Creates action buttons for appointments section
     *
     * @return HBox containing appointment-related action buttons
     */
    private HBox createAppointmentActions() {
        // Button to book new appointment
        Button bookAppointmentBtn = createActionButton("📅 Book New Appointment", e -> new PatientAppointment(patientId).start(new Stage()));

        // Button to initiate video call
        Button videoCallButton = createActionButton("📞 Book Video Call", e -> {
            try {
                new PatientVideoCallPage(patientId).show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox actionsBox = new HBox(10, bookAppointmentBtn, videoCallButton);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));
        return actionsBox;
    }


    /**
     * Creates the feedback section showing doctor's feedback for the patient
     *
     * @return VBox containing the feedback display area
     */
    private VBox createFeedbackSection() {
        Label title = createSectionTitle("Doctor Feedback");
        TextArea feedbackArea = new TextArea();
        feedbackArea.setWrapText(true);  // Enable text wrapping
        feedbackArea.setEditable(false); // Read-only
        feedbackArea.setPrefHeight(100); // Fixed height
        feedbackArea.setStyle("-fx-control-inner-background: #2a2a3d; -fx-text-fill: #e0f7fa;"); // Dark background with light text

        // Load latest feedback from database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT feedback_text FROM feedback WHERE patient_id = ? ORDER BY created_at DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                feedbackArea.setText(rs.getString("feedback_text"));
            } else {
                feedbackArea.setText("No feedback available.");
            }
        } catch (SQLException e) {
            feedbackArea.setText("Error loading feedback.");
            e.printStackTrace();
        }

        VBox box = new VBox(10, title, feedbackArea);
        styleSectionBox(box);
        return box;
    }

    /**
     * Creates the medications section listing current prescriptions
     *
     * @return VBox containing the medications list
     */
    private VBox createMedicationsSection() {
        Label title = createSectionTitle("Current Medications");
        VBox medsBox = new VBox(5); // Container for medication list

        // Load medications from database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT MedicineName, Dosage, Instructions FROM prescriptions WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            // Add each medication to the list
            while (rs.next()) {
                medsBox.getChildren().add(createColoredLabel("\uD83D\uDC8A " +
                        rs.getString("MedicineName") + " - " +
                        rs.getString("Dosage") + " (" +
                        rs.getString("Instructions") + ")"));
            }

            if (medsBox.getChildren().isEmpty()) {
                medsBox.getChildren().add(createColoredLabel("No current medications."));
            }
        } catch (SQLException e) {
            medsBox.getChildren().add(createColoredLabel("Error loading medications."));
            e.printStackTrace();
        }

        VBox box = new VBox(10, title, medsBox);
        styleSectionBox(box);
        return box;
    }

    /**
     * Creates action buttons for medications section
     *
     * @return HBox containing medication-related action buttons
     */
    private HBox createMedicationActions() {
        Button uploadVitalsBtn = createActionButton("📤 Upload Vitals Data", e -> handleUploadVitals());
        HBox actionsBox = new HBox(10, uploadVitalsBtn);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));
        return actionsBox;
    }

    /**
     * Creates communication action buttons (consultation and emergency)
     *
     * @return HBox containing communication buttons
     */
    private HBox createCommunicationActions() {
        // Button to start chat consultation with doctor
        Button startConsultationBtn = createActionButton("💬 Start Consultation", e -> {
            int patientUserId = getPatientUserId(patientId);
            int doctorUserId = getAssignedDoctorUserId(patientId);
            if (patientUserId > 0 && doctorUserId > 0) {
                String role = getPatientRole(patientId);
                new ChatClient(patientUserId, role).start(new Stage());
            } else {
                showAlert("Unable to start consultation. User IDs not found.");
            }
        });

        // Emergency panic button
        Button panicBtn = createActionButton("🚨 Emergency Panic Button", e -> {
            try {
                PanicButton panicButton = new PanicButton(patientId);
                panicButton.displayPanicButtonWindow();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Failed to launch Panic Button window.");
            }
        });

        HBox actionsBox = new HBox(10, startConsultationBtn, panicBtn);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));
        return actionsBox;
    }

    /**
     * Creates health data action buttons
     *
     * @return VBox containing health history and trends buttons
     */
    private VBox createHealthDataActions() {
        Button viewHistoryBtn = createActionButton("📋 View Health History",
                e -> new ViewHealthHistory(patientId).start(new Stage()));
        Button viewTrendsBtn = createActionButton("📈 View Health Trends",
                e -> new HealthTrends(patientId).start(new Stage()));

        VBox actionsBox = new VBox(10, viewHistoryBtn, viewTrendsBtn);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));
        return actionsBox;
    }

    /**
     * Handles uploading vitals data from CSV file
     */
    private void handleUploadVitals() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Vitals CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            boolean success = UploadVitals.uploadVitalsFromCSV(selectedFile, patientId);
            if (success) {
                showAlert("Vitals uploaded successfully.");
            }
        } else {
            showAlert("No file selected.");
        }
    }

    /**
     * Creates a styled action button
     *
     * @param text    Button label text
     * @param handler Event handler for button click
     * @return Configured Button object
     */
    private Button createActionButton(String text, EventHandler<ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setPrefWidth(250); // Fixed width for consistency
        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: #f0faff; -fx-font-weight: bold;");
        btn.setOnAction(handler);
        return btn;
    }

    /**
     * Updates the vitals display with new values
     *
     * @param heartRate Current heart rate
     * @param bp        Current blood pressure
     * @param oxygen    Current oxygen level
     */
    private void updateVitals(String heartRate, String bp, String oxygen) {
        vitalsGrid.getChildren().clear();
        // Add labels and values in a 2-column grid
        vitalsGrid.add(createColoredLabel("Heart Rate:"), 0, 0);
        vitalsGrid.add(createColoredLabel(heartRate + " bpm"), 1, 0);
        vitalsGrid.add(createColoredLabel("Blood Pressure:"), 0, 1);
        vitalsGrid.add(createColoredLabel(bp + " mmHg"), 1, 1);
        vitalsGrid.add(createColoredLabel("Oxygen Level:"), 0, 2);
        vitalsGrid.add(createColoredLabel(oxygen + "%"), 1, 2);
    }

    /**
     * Creates a styled section title label
     *
     * @param text Title text
     * @return Configured Label object
     */
    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        label.setTextFill(Color.web("#80dfff")); // Light blue color
        return label;
    }

    /**
     * Creates a styled colored label
     *
     * @param text Label text
     * @return Configured Label object
     */
    private Label createColoredLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#e0f0ff")); // Light blue-gray color
        return label;
    }

    /**
     * Applies standard styling to section boxes
     *
     * @param box VBox to style
     */
    private void styleSectionBox(VBox box) {
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #2a2a3d; -fx-border-color: #444; -fx-border-radius: 8; -fx-background-radius: 8;");
    }

    /**
     * Shows an information alert dialog
     *
     * @param message Message to display
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gets the user role for a patient
     *
     * @param patientId Patient ID to look up
     * @return Role string ("patient" by default)
     */
    private String getPatientRole(int patientId) {
        String sql = "SELECT role FROM users WHERE id = (SELECT user_id FROM patients WHERE id = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "patient"; // Default role
    }

    /**
     * Gets the user ID associated with a patient
     *
     * @param patientId Patient ID to look up
     * @return User ID or -1 if not found
     */
    private int getPatientUserId(int patientId) {
        String sql = "SELECT user_id FROM patients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Error value
    }

    /**
     * Gets the user ID of the doctor assigned to a patient
     *
     * @param patientId Patient ID to look up
     * @return Doctor's user ID or -1 if not found
     */
    private int getAssignedDoctorUserId(int patientId) {
        String sql = "SELECT d.user_id FROM doctorpatientassignment da " +
                "JOIN doctors d ON da.DoctorID = d.id " +
                "WHERE da.PatientID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Error value
    }

    /**
     * Main application entry point
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args); // Start JavaFX application
    }
}