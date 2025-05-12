package com.example.hospi.GUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProvideFeedback - A JavaFX application for doctors to provide feedback to patients.
 * Allows doctors to select patients, enter feedback, view feedback history,
 * and optionally include prescribed medications.
 */
public class ProvideFeedback extends Application {

    // UI Components
    private ComboBox<String> patientComboBox;  // Dropdown for patient selection
    private TextArea feedbackTextArea;        // Input area for feedback text
    private TextField medicationTextField;    // Input for prescribed medications
    private ListView<String> feedbackListView; // Display for feedback history
    private Button submitButton, clearButton; // Action buttons

    private int doctorId; // Hardcoded doctor ID (to be replaced with authentication)

    ProvideFeedback(int doctorId){
        this.doctorId = doctorId;
    }

    /**
     * Main entry point for the JavaFX application
     *
     * @param primaryStage The primary stage for this application
     */



    @Override
    public void start(Stage primaryStage) {
        // ==================== HEADER SECTION ====================
        Label headerLabel = new Label("🩺 Provide Patient Feedback");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 34));
        headerLabel.setTextFill(Color.web("#00c8ff")); // Light blue color
        headerLabel.setAlignment(Pos.CENTER);
        headerLabel.setPadding(new Insets(20, 0, 10, 0));

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #444;"); // Dark separator
        separator.setPrefWidth(600);

        VBox headerBox = new VBox(headerLabel, separator);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        // ==================== FORM SECTION ====================
        // Patient Selection
        Label patientLabel = createLabel("👤 Select Patient:");
        patientComboBox = new ComboBox<>();
        styleControl(patientComboBox);

        // Feedback Input
        Label feedbackLabel = createLabel("📝 Feedback:");
        feedbackTextArea = new TextArea();
        feedbackTextArea.setPromptText("Enter feedback...");
        feedbackTextArea.setWrapText(true); // Enable text wrapping
        feedbackTextArea.setFont(Font.font("Segoe UI", 13));
        feedbackTextArea.setPrefRowCount(5); // Default height
        feedbackTextArea.setStyle(
                "-fx-control-inner-background: #1e2d3d;" + // Dark background
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #9cbcd6;" + // Light blue placeholder
                        "-fx-background-radius: 10;" + // Rounded corners
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #3498db;" + // Blue border
                        "-fx-border-width: 1;"
        );

        // Medication Input (optional)
        Label medicationLabel = createLabel("💊 Prescribed Medication (optional):");
        medicationTextField = new TextField();
        medicationTextField.setPromptText("e.g., Paracetamol 500mg");
        styleControl(medicationTextField);

        // Action Buttons
        submitButton = new Button("✔ Submit Feedback");
        clearButton = new Button("✖ Clear");
        styleButton(submitButton, "#00bcd4"); // Teal submit button
        styleButton(clearButton, "#ff4444"); // Red clear button

        HBox buttonBox = new HBox(15, submitButton, clearButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Form Container
        VBox formBox = new VBox(15,
                patientLabel, patientComboBox,
                feedbackLabel, feedbackTextArea,
                medicationLabel, medicationTextField,
                buttonBox
        );
        formBox.setPadding(new Insets(30));
        formBox.setMaxWidth(450);
        formBox.setStyle("-fx-background-color: #252538; -fx-background-radius: 12;");

        // ==================== HISTORY SECTION ====================
        Label historyLabel = createLabel("📜 Feedback History:");
        feedbackListView = new ListView<>();
        feedbackListView.setPrefHeight(500);
        feedbackListView.setStyle(
                "-fx-control-inner-background: #1e2d3d;" + // Dark background
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #3498db;" +
                        "-fx-border-width: 1;" +
                        "-fx-font-size: 13px;"
        );

        VBox historyBox = new VBox(15, historyLabel, feedbackListView);
        historyBox.setPadding(new Insets(30));
        historyBox.setMaxWidth(500);
        historyBox.setStyle("-fx-background-color: #252538; -fx-background-radius: 12;");

        // ==================== MAIN LAYOUT ====================
        HBox contentBox = new HBox(40, formBox, historyBox);
        contentBox.setAlignment(Pos.TOP_CENTER);

        VBox root = new VBox(20, headerBox, contentBox);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #1e1e2f;"); // Dark background

        // ==================== SCENE SETUP ====================
        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Doctor Feedback - Lifeline Remote Hospital");
        primaryStage.show();

        // ==================== EVENT HANDLERS ====================
        loadPatientList(); // Initial patient data load
        patientComboBox.setOnAction(e -> loadFeedbackHistory()); // Load history when patient selected
        submitButton.setOnAction(e -> submitFeedback()); // Handle feedback submission
        clearButton.setOnAction(e -> clearFeedback()); // Handle form clearing
    }

    /**
     * Creates a styled label with consistent formatting
     *
     * @param text The label text
     * @return Configured Label object
     */
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#e0f0ff")); // Light blue text
        return label;
    }

    /**
     * Applies consistent styling to input controls
     *
     * @param control The control to style (TextField, ComboBox, etc.)
     */
    private void styleControl(Control control) {
        control.setStyle(
                "-fx-background-color: #1e2d3d;" + // Dark background
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" + // Rounded corners
                        "-fx-border-color: #3498db;" + // Blue border
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;" +
                        "-fx-prompt-text-fill: #9cbcd6;" // Light blue placeholder
        );
        if (control instanceof TextInputControl) {
            ((TextInputControl) control).setFont(Font.font("Segoe UI", 13));
        }
    }

    /**
     * Styles action buttons with consistent appearance
     *
     * @param button The button to style
     * @param color  The background color in hex format
     */
    private void styleButton(Button button, String color) {
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13;" +
                        "-fx-background-radius: 10;" + // Rounded corners
                        "-fx-padding: 8 16;" // Vertical/Horizontal padding
        );
    }

    // [Remaining methods would be commented similarly...]

    /**
     * Loads the list of patients assigned to the current doctor from the database
     * and populates the patient selection dropdown.
     * The patient names are displayed in the format "ID | Name".
     */
    private void loadPatientList() {
        // SQL query to get all patients assigned to this doctor
        String query = "SELECT p.id, p.name FROM patients p " +
                "JOIN doctorpatientassignment dpa ON p.id = dpa.PatientID WHERE dpa.DoctorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId); // Set the doctor ID parameter
            ResultSet rs = stmt.executeQuery();

            List<String> patientNames = new ArrayList<>();
            // Process each patient record
            while (rs.next()) {
                // Format as "ID | Name" for display
                patientNames.add(rs.getInt("id") + " | " + rs.getString("name"));
            }

            // Update the combo box with the patient list
            patientComboBox.getItems().setAll(patientNames);
        } catch (SQLException e) {
            showAlert("Error loading patients. Please try again later.");
        }
    }

    /**
     * Loads and displays the feedback history for the currently selected patient.
     * Each entry shows the timestamp, feedback text, and any prescribed medication.
     */
    private void loadFeedbackHistory() {
        String selected = patientComboBox.getValue();
        if (selected == null) return; // No patient selected

        // Extract the patient ID from the selected item ("ID | Name")
        int patientId = extractPatientId(selected);

        // Query to get all feedback records for this patient
        String query = "SELECT feedback_text, medication, created_at FROM feedback WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            // Clear previous feedback entries
            feedbackListView.getItems().clear();

            // Process each feedback record
            while (rs.next()) {
                // Format the feedback entry with emojis for better readability
                String entry = "🗓 " + rs.getTimestamp("created_at") +  // Timestamp
                        "\n📝 " + rs.getString("feedback_text") +      // Feedback text
                        "\n💊 " + rs.getString("medication");         // Medication
                feedbackListView.getItems().add(entry);
            }
        } catch (SQLException e) {
            showAlert("Error loading feedback history. Please try again.");
        }
    }

    /**
     * Submits new feedback for the selected patient, including:
     * 1. Saving to the database
     * 2. Sending email notification to patient
     * 3. Clearing the form
     * 4. Refreshing the history view
     */
    private void submitFeedback() {
        String selected = patientComboBox.getValue();

        // Validate required fields
        if (selected == null || feedbackTextArea.getText().trim().isEmpty()) {
            showAlert("Please select a patient and enter your feedback.");
            return;
        }

        // Get form data
        int patientId = extractPatientId(selected);
        String feedback = feedbackTextArea.getText().trim();
        String medication = medicationTextField.getText().trim();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // SQL queries
        String insertQuery = "INSERT INTO feedback (doctor_id, patient_id, feedback_text, medication, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        String emailQuery = "SELECT address FROM patients WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
             PreparedStatement emailStmt = conn.prepareStatement(emailQuery)) {

            // 1. Insert the new feedback record
            insertStmt.setInt(1, doctorId);
            insertStmt.setInt(2, patientId);
            insertStmt.setString(3, feedback);
            insertStmt.setString(4, medication.isEmpty() ? null : medication);
            insertStmt.setTimestamp(5, timestamp);
            insertStmt.executeUpdate();

            // 2. Get patient email for notification
            emailStmt.setInt(1, patientId);
            ResultSet rs = emailStmt.executeQuery();

            if (rs.next()) {
                String patientEmail = rs.getString("address");

                // Prepare email content with formatted feedback
                String subject = "🩺 New Feedback from Your Doctor";
                String message = "Hello,\n\nYou have received new feedback from your doctor:\n\n" +
                        "📅 Date: " + timestamp + "\n" +
                        "📝 Feedback: " + feedback + "\n" +
                        (medication.isEmpty() ? "" : "💊 Medication: " + medication + "\n") +
                        "\nBest regards,\nLifeline Remote Hospital";

                // Send email notification
                new EmailNotification().sendNotification(subject, message, patientEmail);
            }

            // Update UI
            showAlert("Feedback successfully submitted and email sent.");
            clearFeedback();
            loadFeedbackHistory();

        } catch (SQLException e) {
            showAlert("Failed to submit feedback. Please try again.");
            e.printStackTrace();
        }
    }

    /**
     * Clears the feedback input fields.
     */
    private void clearFeedback() {
        feedbackTextArea.clear();
        medicationTextField.clear();
    }

    /**
     * Extracts the patient ID from the formatted string ("ID | Name").
     *
     * @param fullText The combined ID and name string
     * @return The extracted patient ID
     */
    private int extractPatientId(String fullText) {
        return Integer.parseInt(fullText.split(" \\| ")[0]);
    }

    /**
     * Displays an informational alert dialog.
     *
     * @param message The message to display in the alert
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Main entry point for the JavaFX application.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args); // Start the JavaFX application
    }
}