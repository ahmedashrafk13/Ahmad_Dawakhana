package com.example.hospi.GUI;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.sql.*;
import java.util.*;

/**
 * JavaFX application that allows doctors to view health trends of their assigned patients.
 * Features include:
 * - Dropdown selection of assigned patients
 * - Visualization of patient health trends
 * - Clean, professional UI with consistent styling
 */
public class ViewTrendsDoctor extends Application {
    // Default doctor ID (would normally come from login)
    private int doctorId;

    // UI components
    private ComboBox<String> patientComboBox;
    private BorderPane mainLayout;

    // Maps patient IDs to their names for easy lookup
    private Map<Integer, String> patientsMap = new HashMap<>();

    ViewTrendsDoctor(int doctorId){
        this.doctorId = doctorId;
    }

    /**
     * Main JavaFX start method that sets up the application window
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        // Set up main layout container with dark blue background
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #0d1b2a;");

        // Create and style application header
        Label headerLabel = new Label("Lifeline Remote Hospital - Patient Trends");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.web("#87CEEB")); // Sky blue text
        HBox headerBox = new HBox(headerLabel);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        // Get list of patients assigned to this doctor
        List<String> patientNames = getAssignedPatients(doctorId);

        // Set up patient selection combo box
        patientComboBox = new ComboBox<>();
        patientComboBox.getItems().addAll(patientNames);
        patientComboBox.setPromptText("Select Patient");
        patientComboBox.setStyle("-fx-background-color: #2a2a3d; -fx-text-fill: white;");

        // Set action handler for when a patient is selected
        patientComboBox.setOnAction(e -> onPatientSelected());

        // Create and style the view trends button
        Button viewTrendsButton = new Button("View Trends");
        viewTrendsButton.setStyle(
                "-fx-background-color: #3498db; " + // Blue button
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8;");
        viewTrendsButton.setOnAction(e -> onViewTrends());

        // Container for selection controls
        VBox selectionBox = new VBox(15, patientComboBox, viewTrendsButton);
        selectionBox.setPadding(new Insets(20));
        selectionBox.setStyle("-fx-background-color: #102841; -fx-background-radius: 10;");
        selectionBox.setAlignment(Pos.CENTER);

        // Main content layout
        VBox contentBox = new VBox(20, headerBox, selectionBox);
        contentBox.setPadding(new Insets(20));
        contentBox.setAlignment(Pos.TOP_CENTER);

        mainLayout.setCenter(contentBox);

        // Set up and show the scene
        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setTitle("View Trends for Doctor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Retrieves list of patients assigned to the specified doctor
     * @param doctorId ID of the doctor to look up
     * @return List of patient names assigned to this doctor
     */
    private List<String> getAssignedPatients(int doctorId) {
        List<String> patients = new ArrayList<>();
        String query = "SELECT p.id, p.name FROM hospital_db.patients p " +
                "JOIN hospital_db.doctorpatientassignment d ON p.id = d.PatientID " +
                "WHERE d.DoctorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            // Populate both the list and the ID-to-name map
            while (rs.next()) {
                String patientName = rs.getString("name");
                int patientId = rs.getInt("id");
                patients.add(patientName);
                patientsMap.put(patientId, patientName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return patients;
    }

    /**
     * Handles patient selection from the dropdown
     */
    private void onPatientSelected() {
        String selectedPatientName = patientComboBox.getValue();
        if (selectedPatientName != null && !selectedPatientName.isEmpty()) {
            int selectedPatientId = getPatientIdByName(selectedPatientName);
            showHealthTrends(selectedPatientId);
        }
    }

    /**
     * Looks up patient ID by name using the pre-populated map
     * @param patientName Name of patient to look up
     * @return Patient ID or -1 if not found
     */
    private int getPatientIdByName(String patientName) {
        return patientsMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(patientName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);
    }

    /**
     * Displays health trends for the selected patient
     * @param patientId ID of patient to show trends for
     */
    private void showHealthTrends(int patientId) {
        if (patientId == -1) {
            showAlert("Invalid Patient", "No valid patient selected.");
            return;
        }

        // Launch the health trends visualization
        HealthTrends healthTrends = new HealthTrends(patientId);
        Stage trendsStage = new Stage();
        healthTrends.start(trendsStage);
    }

    /**
     * Handles the view trends button click
     */
    private void onViewTrends() {
        String selectedPatientName = patientComboBox.getValue();
        if (selectedPatientName != null && !selectedPatientName.isEmpty()) {
            int selectedPatientId = getPatientIdByName(selectedPatientName);
            showHealthTrends(selectedPatientId);
        } else {
            showAlert("Patient Not Selected", "Please select a patient to view their health trends.");
        }
    }

    /**
     * Shows a styled error alert dialog
     * @param title Title of the alert
     * @param message Message to display
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply consistent styling to the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #102841; " + // Dark blue background
                        "-fx-text-fill: white;"); // White text
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}