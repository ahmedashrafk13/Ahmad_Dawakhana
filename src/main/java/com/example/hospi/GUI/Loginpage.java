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

/**
 * Loginpage class provides the user authentication interface for the LifeLine Remote Hospital system.
 * It handles user login, validates credentials, and redirects to appropriate dashboards based on user roles.
 */
public class Loginpage extends Application {

    // UI Components
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label messageLabel;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Main entry point for the JavaFX application
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        initializeUIComponents();
        setupLoginButtonAction(primaryStage);

        // Create and configure the main layout
        VBox layout = createMainLayout();

        // Set up the scene and stage
        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setTitle("Login - LifeLine Remote Hospital");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Initializes all UI components with their default values and styles
     */
    private void initializeUIComponents() {
        usernameField = new TextField();
        passwordField = new PasswordField();
        loginButton = new Button("Login");
        messageLabel = new Label();

        styleTextFields(usernameField, passwordField);
        styleButton(loginButton);
    }

    /**
     * Sets up the login button action handler
     * @param primaryStage The primary stage to use for redirection
     */
    private void setupLoginButtonAction(Stage primaryStage) {
        loginButton.setOnAction(event -> handleLogin(primaryStage));
    }

    /**
     * Creates and configures the main layout container
     * @return Configured VBox containing all UI elements
     */
    private VBox createMainLayout() {
        // Create title and subtitle labels
        Label title = new Label("LifeLine Remote Hospital");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#87CEEB"));

        Label subtitle = new Label("Your health, our priority.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.web("#80dfff"));

        // Create sign-up prompt with hyperlink
        Label signUpPrompt = new Label("Don't have an account? ");
        Hyperlink signUpLink = new Hyperlink("Sign up");
        signUpLink.setOnAction(e -> openSignUpPage());

        HBox signUpBox = new HBox(signUpPrompt, signUpLink);
        signUpBox.setAlignment(Pos.CENTER);
        signUpBox.setSpacing(5);
        signUpPrompt.setTextFill(Color.LIGHTGRAY);
        signUpLink.setTextFill(Color.SKYBLUE);

        // Assemble main layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #0d1b2a;"); // Dark blue background

        layout.getChildren().addAll(title, subtitle,
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                loginButton, messageLabel, signUpBox);

        return layout;
    }

    /**
     * Opens the sign-up page in a new window
     */
    private void openSignUpPage() {
        try {
            new Signuppage().start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Applies consistent styling to text input fields
     * @param fields Varargs of TextFields to style
     */
    private void styleTextFields(TextField... fields) {
        for (TextField field : fields) {
            field.setPrefWidth(250);
            field.setFont(Font.font("Segoe UI", 14));
            field.setStyle(
                    "-fx-background-color: #102841;" +
                            "-fx-text-fill: #D0E8F2;" +
                            "-fx-prompt-text-fill: #6c8ebf;" +
                            "-fx-background-radius: 6px;"
            );
        }
        usernameField.setPromptText("Enter username");
        passwordField.setPromptText("Enter password");
    }

    /**
     * Applies styling and hover effects to a button
     * @param button The button to style
     */
    private void styleButton(Button button) {
        button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px;");
        button.setPrefWidth(150);
        // Hover effects
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #2d89ef; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px;"));
    }

    /**
     * Handles the login button click event
     * @param primaryStage The primary stage to use for redirection
     */
    private void handleLogin(Stage primaryStage) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty.");
            return;
        }

        // Attempt authentication
        UserSession session = getUserSession(username, password);
        if (session != null) {
            showSuccess("Login successful! Redirecting...");
            redirectToDashboard(primaryStage, session);
        } else {
            showError("Invalid username or password.");
        }
    }

    /**
     * Authenticates user credentials against the database
     * @param username The username to authenticate
     * @param password The password to verify
     * @return UserSession object if authentication succeeds, null otherwise
     */
    private UserSession getUserSession(String username, String password) {
        String query = "SELECT id, role FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String role = rs.getString("role");
                return new UserSession(userId, role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error.");
        }
        return null;
    }

    /**
     * Redirects to the appropriate dashboard based on user role
     * @param primaryStage The primary stage to close
     * @param session The authenticated user session
     */
    private void redirectToDashboard(Stage primaryStage, UserSession session) {
        try {
            switch (session.role.toLowerCase()) {
                case "patient":
                    int patientId = getPatientIdFromUserId(session.userId);
                    String patientName = getPatientNameById(patientId);
                    new PatientDashboard(patientId, patientName).start(new Stage());
                    break;
                case "doctor":
                    int doctorid = getDoctorIdFromUserId(session.userId);
                    new DoctorDashboard(doctorid).start(new Stage());
                    break;
                case "admin":
                    new AdminDashboard(session.userId);
                    break;
                default:
                    showError("Unknown role.");
                    return;
            }
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open dashboard.");
        }
    }

    /**
     * Retrieves patient ID associated with a user ID
     * @param userId The user ID to look up
     * @return The associated patient ID
     * @throws SQLException If no patient record is found
     */
    private int getPatientIdFromUserId(int userId) throws SQLException {
        String query = "SELECT id FROM patients WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        throw new SQLException("Patient ID not found for user ID: " + userId);
    }

    private int getDoctorIdFromUserId(int userId) throws SQLException {
        String query = "SELECT id FROM doctors WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        throw new SQLException("Doctor ID not found for user ID: " + userId);
    }

    /**
     * Retrieves patient name by patient ID
     * @param patientId The patient ID to look up
     * @return The patient's name
     * @throws SQLException If no patient record is found
     */
    private String getPatientNameById(int patientId) throws SQLException {
        String query = "SELECT name FROM patients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        }
        throw new SQLException("Patient name not found for patient ID: " + patientId);
    }

    /**
     * Displays an error message to the user
     * @param message The error message to display
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.RED);
    }

    /**
     * Displays a success message to the user
     * @param message The success message to display
     */
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.LIMEGREEN);
    }

    /**
     * Inner class representing an authenticated user session
     */
    private static class UserSession {
        int userId;
        String role;

        public UserSession(int userId, String role) {
            this.userId = userId;
            this.role = role;
        }
    }
}