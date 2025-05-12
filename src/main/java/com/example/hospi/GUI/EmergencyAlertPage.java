package com.example.hospi.GUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * EmergencyAlertPage - Displays critical patient alerts for medical staff.
 * Provides a dashboard showing real-time emergency notifications with
 * patient vitals and acknowledgment functionality.
 */
public class EmergencyAlertPage extends Application {

    /**
     * Main entry point for the JavaFX application.
     * @param stage The primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        // ========== PAGE HEADER ========== //
        Label title = new Label("🚨 Emergency Alerts Dashboard");
        title.setFont(Font.font("Segoe UI Semibold", 30));
        title.setTextFill(Color.web("#d32f2f")); // Red color for emergency indication

        // ========== ALERT LIST CONTAINER ========== //
        VBox alertList = new VBox(20); // 20px vertical spacing between alerts
        alertList.setPadding(new Insets(10)); // Internal padding
        alertList.setStyle("-fx-background-color: #fff8f8;"); // Light red background

        // ========== SAMPLE ALERT DATA ========== //
        // In production, these would come from a database/API
        alertList.getChildren().addAll(
                createAlertCard("👤 John Doe", "❤️ Heart Rate: 145 bpm", "12:30 PM", "#ffebee"),
                createAlertCard("👩 Alice Smith", "🩺 BP: 180/120 mmHg", "12:34 PM", "#fff3e0"),
                createAlertCard("👨 Mike Johnson", "🌬️ Oxygen Level: 83%", "12:35 PM", "#e3f2fd")
        );

        // ========== SCROLLABLE CONTAINER ========== //
        ScrollPane scrollPane = new ScrollPane(alertList);
        scrollPane.setFitToWidth(true); // Prevent horizontal scrolling
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;"); // Clean styling

        // ========== REFRESH BUTTON ========== //
        Button refreshBtn = new Button("🔄 Refresh Alerts");
        refreshBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        refreshBtn.setOnAction(e -> {
            System.out.println("🔁 Refresh clicked");
            // TODO: Implement actual refresh logic
            // Would typically fetch new alerts from backend
        });

        // ========== MAIN LAYOUT ========== //
        VBox layout = new VBox(25, title, scrollPane, refreshBtn); // 25px spacing
        layout.setPadding(new Insets(30)); // Outer padding
        layout.setStyle("-fx-background-color: #fafafa;"); // Light background
        layout.setAlignment(Pos.TOP_CENTER); // Center alignment

        // ========== SCENE SETUP ========== //
        Scene scene = new Scene(layout, 650, 650);
        stage.setScene(scene);
        stage.setTitle("Emergency Alerts Notifications");
        stage.show();
    }

    /**
     * Creates a styled alert card component for each emergency notification.
     * @param patientName The patient's name with emoji
     * @param vitalInfo The critical vital information
     * @param time When the alert was triggered
     * @param bgColor Background color for the card
     * @return Configured HBox containing the alert card
     */
    private HBox createAlertCard(String patientName, String vitalInfo, String time, String bgColor) {
        // Patient Name Label
        Label nameLabel = new Label(patientName);
        nameLabel.setFont(Font.font("Segoe UI", 16));
        nameLabel.setTextFill(Color.web("#2c3e50")); // Dark text for readability

        // Vital Information Label
        Label vitalLabel = new Label(vitalInfo);
        vitalLabel.setFont(Font.font("Segoe UI", 15));
        vitalLabel.setTextFill(Color.web("#d84315")); // Orange-red for critical values

        // Timestamp Label
        Label timeLabel = new Label("🕒 " + time);
        timeLabel.setFont(Font.font("Segoe UI", 13));
        timeLabel.setTextFill(Color.web("#616161")); // Gray for secondary info

        // Details Container
        VBox details = new VBox(6, nameLabel, vitalLabel, timeLabel); // 6px spacing

        // Acknowledge Button
        Button ackBtn = new Button("✔ Acknowledge");
        ackBtn.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-font-weight: bold;");
        ackBtn.setPrefWidth(130);
        ackBtn.setPrefHeight(40);
        ackBtn.setOnAction(e -> {
            // TODO: Implement acknowledgment logic
            // Would typically update alert status in backend
            System.out.println("✅ Acknowledged alert for " + patientName);
        });

        // Card Container
        HBox card = new HBox(20, details, ackBtn); // 20px spacing
        card.setPadding(new Insets(15)); // Internal padding
        card.setAlignment(Pos.CENTER_LEFT); // Left-align details
        card.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 15; " +
                "-fx-border-radius: 15; " +
                "-fx-border-color: #ccc;"); // Rounded corners with border

        HBox.setHgrow(details, Priority.ALWAYS); // Make details expandable
        return card;
    }

    /**
     * Main method to launch the application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args); // Start JavaFX application
    }
}