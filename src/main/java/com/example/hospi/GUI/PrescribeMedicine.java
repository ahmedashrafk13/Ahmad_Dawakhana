package com.example.hospi.GUI;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.sql.*;

/**
 * PrescribeMedicine - A JavaFX application for doctors to prescribe medications to patients.
 * Provides a form interface to select patients, enter medication details, and submit prescriptions.
 */
public class PrescribeMedicine extends Application {
    // UI Components
    private ComboBox<String> patientComboBox;          // Dropdown for patient selection
    private TextField medicineNameTextField;           // Input for medicine name
    private ComboBox<String> dosageFrequencyComboBox;  // Dropdown for dosage frequency
    private TextArea instructionsTextArea;            // Text area for special instructions
    private TextField durationTextField;              // Input for treatment duration
    private TextField refillsTextField;               // Input for number of refills
    private int doctorId;

    PrescribeMedicine(int doctorId){
        this.doctorId = doctorId;
    }


    /**
     * Main entry point for the JavaFX application
     *
     * @param stage The primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        // Create header with application title
        Label header = new Label("Prescribe Medicine");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        header.setTextFill(Color.web("#e0f0ff"));  // Light blue text color
        StackPane headerPane = new StackPane(header);
        headerPane.setPadding(new Insets(20));
        headerPane.setStyle("-fx-background-color: #0d1b2a;");  // Dark blue background

        // Create form grid layout
        GridPane form = new GridPane();
        form.setHgap(20);  // Horizontal gap between columns
        form.setVgap(20);  // Vertical gap between rows
        form.setPadding(new Insets(30));
        form.setAlignment(Pos.CENTER_LEFT);

        double labelWidth = 140;  // Fixed width for form labels

        // PATIENT SELECTION
        Label patientLabel = createStyledLabel("Select Patient:");
        patientLabel.setMinWidth(labelWidth);
        patientComboBox = new ComboBox<>();
        patientComboBox.setPrefWidth(300);
        styleComboBox(patientComboBox);

        // MEDICINE NAME
        Label medicineLabel = createStyledLabel("Medicine Name:");
        medicineLabel.setMinWidth(labelWidth);
        medicineNameTextField = new TextField();
        medicineNameTextField.setPromptText("e.g. Paracetamol");
        medicineNameTextField.setPrefWidth(300);
        styleInput(medicineNameTextField);

        // DOSAGE FREQUENCY
        Label dosageLabel = createStyledLabel("Dosage Frequency:");
        dosageLabel.setMinWidth(labelWidth);
        dosageFrequencyComboBox = new ComboBox<>();
        dosageFrequencyComboBox.getItems().addAll(
                "1 time a day",
                "2 times a day",
                "3 times a day",
                "As needed"
        );
        dosageFrequencyComboBox.setPrefWidth(300);
        styleComboBox(dosageFrequencyComboBox);

        // INSTRUCTIONS
        Label instructionsLabel = createStyledLabel("Instructions:");
        instructionsLabel.setMinWidth(labelWidth);
        instructionsTextArea = new TextArea();
        instructionsTextArea.setPromptText("e.g. Take after meals...");
        instructionsTextArea.setPrefSize(300, 80);  // Width x Height
        styleTextArea(instructionsTextArea);

        // DURATION
        Label durationLabel = createStyledLabel("Duration (days):");
        durationLabel.setMinWidth(labelWidth);
        durationTextField = new TextField();
        durationTextField.setPromptText("e.g. 7");
        durationTextField.setPrefWidth(300);
        styleInput(durationTextField);

        // REFILLS
        Label refillsLabel = createStyledLabel("Refills:");
        refillsLabel.setMinWidth(labelWidth);
        refillsTextField = new TextField();
        refillsTextField.setPromptText("e.g. 2");
        refillsTextField.setPrefWidth(300);
        styleInput(refillsTextField);

        // Add all components to the form grid
        form.addRow(0, patientLabel, patientComboBox);
        form.addRow(1, medicineLabel, medicineNameTextField);
        form.addRow(2, dosageLabel, dosageFrequencyComboBox);
        form.addRow(3, instructionsLabel, instructionsTextArea);
        form.addRow(4, durationLabel, durationTextField);
        form.addRow(5, refillsLabel, refillsTextField);

        // SUBMIT BUTTON
        Button submitButton = new Button("Submit Prescription");
        submitButton.setStyle(
                "-fx-background-color: #3498db;" +  // Blue background
                        "-fx-text-fill: white;" +           // White text
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +     // Rounded corners
                        "-fx-padding: 10 20;"              // Vertical/Horizontal padding
        );
        submitButton.setPrefWidth(250);
        submitButton.setOnAction(e -> submitPrescription());

        // Create card container for form
        VBox card = new VBox(30, form, submitButton);
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #102841; -fx-background-radius: 12;");

        // Main layout containing header and form card
        VBox mainLayout = new VBox(20, headerPane, card);
        mainLayout.setStyle("-fx-background-color: #0d1b2a;");
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Set up scene and stage
        Scene scene = new Scene(mainLayout, 750, 650);
        applyComboBoxDropdownStyle(scene);  // Apply custom dropdown styling

        stage.setTitle("Prescribe Medicine");
        stage.setScene(scene);
        stage.show();

        // Load patient data from database
        loadPatientsFromDatabase(getDoctorID());
    }

    /**
     * Creates a styled label with consistent formatting
     *
     * @param text The label text
     * @return Configured Label object
     */
    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#e0f0ff"));  // Light blue text
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        return label;
    }

    /**
     * Applies consistent styling to input controls
     *
     * @param control The input control to style (TextField, etc.)
     */
    private void styleInput(Control control) {
        control.setStyle(
                "-fx-background-color: #1e2d3d;" +  // Dark background
                        "-fx-text-fill: white;" +           // White text
                        "-fx-background-radius: 10;" +      // Rounded corners
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #3498db;" +     // Blue border
                        "-fx-border-width: 1;" +
                        "-fx-prompt-text-fill: #9cbcd6;"    // Light blue placeholder
        );
        if (control instanceof TextInputControl) {
            ((TextInputControl) control).setFont(Font.font("Segoe UI", 13));
        }
    }

    /**
     * Applies special styling to TextArea controls
     *
     * @param textArea The TextArea to style
     */
    private void styleTextArea(TextArea textArea) {
        textArea.setStyle(
                "-fx-control-inner-background: #1e2d3d;" +  // Dark background
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #3498db;" +
                        "-fx-border-width: 1;" +
                        "-fx-prompt-text-fill: #9cbcd6;"
        );
        textArea.setFont(Font.font("Segoe UI", 13));
        textArea.setWrapText(true);  // Enable text wrapping
    }

    /**
     * Styles ComboBox controls consistently
     *
     * @param comboBox The ComboBox to style
     */
    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle(
                "-fx-background-color: #1e2d3d;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #3498db;" +
                        "-fx-border-width: 1;" +
                        "-fx-prompt-text-fill: #9cbcd6;"
        );
    }

    /**
     * Applies custom styling to ComboBox dropdown menus
     *
     * @param scene The scene to apply the styles to
     */
    private void applyComboBoxDropdownStyle(Scene scene) {
        String css = """
                .combo-box-popup .list-view {
                    -fx-background-color: #1e2d3d;
                    -fx-control-inner-background: #1e2d3d;
                    -fx-text-fill: white;
                }
                .combo-box-popup .list-cell {
                    -fx-background-color: #1e2d3d;
                    -fx-text-fill: white;
                }
                .combo-box-popup .list-cell:hover {
                    -fx-background-color: #3498db;
                }
                """;
        scene.getStylesheets().add("data:text/css," + css.replace("\n", "").replace(" ", "%20"));
    }

    /**
     * Placeholder method to get doctor ID
     *
     * @return Hardcoded doctor ID (to be replaced with actual implementation)
     */
    private int getDoctorID() {
        return doctorId; // TODO: Replace with actual doctor ID retrieval
    }

    /**
     * Loads patients assigned to the specified doctor from the database
     * and populates the patient dropdown menu
     *
     * @param doctorId The ID of the doctor whose patients to load
     */
    private void loadPatientsFromDatabase(int doctorId) {
        // SQL query to fetch patients assigned to this doctor
        String query ="""
                SELECT DISTINCT p.id, p.name 
                FROM hospital_db.doctorpatientassignment dpa
                JOIN hospital_db.patients p ON dpa.PatientID = p.id
                WHERE dpa.DoctorID = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, doctorId);
            try (ResultSet rs = pst.executeQuery()) {
                // Add each patient to the dropdown
                while (rs.next()) {
                    patientComboBox.getItems().add(rs.getString("patient_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading patient data.");
        }
    }

    /**
     * Handles prescription form submission - validates input, saves to database,
     * and sends email notification to patient
     */
    private void submitPrescription() {
        // Get form values
        String patientName = patientComboBox.getValue();
        String medicine = medicineNameTextField.getText().trim();
        String dosage = dosageFrequencyComboBox.getValue();
        String instructions = instructionsTextArea.getText().trim();
        String duration = durationTextField.getText().trim();
        String refills = refillsTextField.getText().trim();

        // Validate required fields
        if (patientName == null || medicine.isEmpty() || dosage == null || duration.isEmpty()) {
            showAlert("Please fill in all required fields.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // First query: Get patient ID and email (stored in address field)
            String patientQuery = "SELECT id, address FROM hospital_db.patients WHERE name = ?";
            int patientId = -1;
            String patientEmail = "";

            try (PreparedStatement pst = conn.prepareStatement(patientQuery)) {
                pst.setString(1, patientName);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        patientId = rs.getInt("id");
                        patientEmail = rs.getString("address"); // Email is stored in address
                    }
                }
            }

            if (patientId == -1 || patientEmail.isEmpty()) {
                showAlert("Patient information not found or missing email.");
                return;
            }

            // Second query: Insert prescription record
            String insertQuery = """
                    INSERT INTO hospital_db.prescriptions 
                    (PatientID, DoctorID, MedicineName, Dosage, Instructions, PrescriptionDate, Duration, Refills, Status)
                    VALUES (?, ?, ?, ?, ?, CURDATE(), ?, ?, 'Active')
                    """;

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                // Set parameters for the prepared statement
                insertStmt.setInt(1, patientId);
                insertStmt.setInt(2, getDoctorID());
                insertStmt.setString(3, medicine);
                insertStmt.setString(4, dosage);
                insertStmt.setString(5, instructions);
                insertStmt.setString(6, duration);
                insertStmt.setString(7, refills.isEmpty() ? "0" : refills);

                // Execute the insert
                int rowsInserted = insertStmt.executeUpdate();
                if (rowsInserted > 0) {
                    showAlert("✅ Prescription submitted successfully.");

                    // Prepare email notification
                    String subject = "🩺 Prescription Issued: " + medicine;
                    String emailBody = String.format("""
                                    Dear %s,
                                    
                                    Your doctor has issued a new prescription for you through the Hospital Management System.
                                    
                                    🧾 Prescription Details:
                                    • Medicine: %s
                                    • Dosage: %s
                                    • Duration: %s day(s)
                                    • Refills: %s
                                    • Instructions: %s
                                    
                                    📅 Issued on: %s
                                    
                                    Please follow the instructions carefully. For any concerns, feel free to reach out to your doctor.
                                    
                                    Wishing you a speedy recovery,
                                    Hospital Management System
                                    """,
                            patientName, medicine, dosage, duration,
                            refills.isEmpty() ? "None" : refills,
                            instructions.isEmpty() ? "N/A" : instructions,
                            java.time.LocalDate.now()
                    );

                    // Send email notification
                    Notifiable emailer = new EmailNotification();
                    emailer.sendNotification(subject, emailBody, patientEmail);
                } else {
                    showAlert("❌ Failed to submit prescription.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("A database error occurred. Please try again.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("An unexpected error occurred.");
        }
    }

    /**
     * Displays an information alert dialog
     *
     * @param message The message to display in the alert
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Main entry point for the JavaFX application
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
