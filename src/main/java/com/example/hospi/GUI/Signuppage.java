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
 * Signuppage - A JavaFX application for user registration in the hospital system.
 * Supports registration for Doctors, Patients, and Admins with role-specific fields.
 */
public class Signuppage extends Application {

    // Form input fields
    private TextField usernameField, nameField, phoneField, emailField,
            specializationField, genderField, dobField;
    private PasswordField passwordField;
    private ComboBox<String> roleComboBox;  // Dropdown for user role selection
    private GridPane grid;                 // Main form layout
    private Button signupButton;           // Primary action button

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Main entry point for the JavaFX application
     * @param primaryStage The primary stage/window for this application
     */
    @Override
    public void start(Stage primaryStage) {
        // Initialize all input fields
        initializeFields();

        // Setup role selection dropdown
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Doctor", "Patient", "Admin");
        roleComboBox.setPrefWidth(250);
        roleComboBox.setStyle("-fx-background-color: #102841; -fx-text-fill: #D0E8F2; -fx-font-size: 14px;");

        // Configure signup button with hover effects
        configureSignupButton();

        // Create login redirect button
        Button loginRedirectButton = createLoginRedirectButton(primaryStage);

        // Setup main form grid
        grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);
        setupInitialFields(); // Add initial fields to grid

        // Create header with title and subtitle
        VBox header = createHeader();

        // Assemble main layout
        VBox root = new VBox(20, header, grid, loginRedirectButton);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #0d1b2a;"); // Dark blue background

        // Set up and show the window
        Scene scene = new Scene(root, 500, 700);
        primaryStage.setTitle("Sign Up - LifeLine Remote Hospital");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Initializes all text input fields with consistent styling
     */
    private void initializeFields() {
        usernameField = new TextField();
        passwordField = new PasswordField();
        nameField = new TextField();
        phoneField = new TextField();
        emailField = new TextField();
        specializationField = new TextField();
        genderField = new TextField();
        dobField = new TextField();

        // Apply consistent styling to all text fields
        styleTextFields(usernameField, passwordField, nameField, phoneField,
                emailField, specializationField, genderField, dobField);
    }

    /**
     * Configures the signup button with styling and hover effects
     */
    private void configureSignupButton() {
        signupButton = new Button("Sign Up");
        signupButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px;");
        signupButton.setPrefWidth(150);
        signupButton.setOnAction(e -> handleSignup());

        // Hover effects
        signupButton.setOnMouseEntered(e ->
                signupButton.setStyle("-fx-background-color: #2d89ef; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px;"));
        signupButton.setOnMouseExited(e ->
                signupButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px;"));
    }

    /**
     * Creates a button to redirect to the login page
     * @param primaryStage The current stage to close when redirecting
     * @return Configured button
     */
    private Button createLoginRedirectButton(Stage primaryStage) {
        Button button = new Button("Already have an account? Login");
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #87CEEB; " +
                "-fx-font-size: 13px; -fx-underline: true;");
        button.setOnAction(e -> {
            try {
                new Loginpage().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                showAlert("Failed to open login page: " + ex.getMessage());
            }
        });
        return button;
    }

    /**
     * Creates the header section with title and subtitle
     * @return Configured VBox containing header elements
     */
    private VBox createHeader() {
        Label title = new Label("LifeLine Remote Hospital");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#87CEEB")); // Light blue color

        Label subtitle = new Label("Join the future of healthcare. Secure. Fast. Reliable.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#80dfff")); // Lighter blue color

        return new VBox(5, title, subtitle);
    }

    /**
     * Applies consistent styling to multiple text fields
     * @param fields Varargs parameter for text fields to style
     */
    private void styleTextFields(TextField... fields) {
        for (TextField field : fields) {
            field.setPrefWidth(250);
            field.setFont(Font.font("Segoe UI", 14));
            field.setStyle(
                    "-fx-background-color: #102841;" +  // Dark background
                            "-fx-text-fill: #D0E8F2;" +        // Light text
                            "-fx-prompt-text-fill: #6c8ebf;" + // Placeholder color
                            "-fx-background-radius: 6px;"      // Rounded corners
            );
        }
    }

    /**
     * Sets up the initial fields in the form grid (username, password, role)
     */
    private void setupInitialFields() {
        grid.getChildren().clear();
        int row = 0;

        // Add basic account fields
        grid.add(makeLabel("Username:"), 0, row);
        grid.add(usernameField, 1, row++);

        grid.add(makeLabel("Password:"), 0, row);
        grid.add(passwordField, 1, row++);

        grid.add(makeLabel("Role:"), 0, row);
        grid.add(roleComboBox, 1, row++);

        // Add listener for role changes to update form fields
        roleComboBox.setOnAction(e -> updateRoleFields());
        updateRoleFields(); // Initial field update
    }

    /**
     * Updates the form fields based on the selected role
     * Shows/hides role-specific fields dynamically
     */
    private void updateRoleFields() {
        // Clear all fields below role selection
        grid.getChildren().removeIf(node -> {
            Integer rowIndex = GridPane.getRowIndex(node);
            return rowIndex != null && rowIndex > 2;
        });

        String role = roleComboBox.getValue();
        int row = 3; // Start adding new fields at row 3

        if (role == null) return;

        // Common fields for all roles
        grid.add(makeLabel("Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(makeLabel("Phone:"), 0, row);
        grid.add(phoneField, 1, row++);

        grid.add(makeLabel("Email:"), 0, row);
        grid.add(emailField, 1, row++);

        // Role-specific fields
        if (role.equals("Doctor")) {
            grid.add(makeLabel("Specialization:"), 0, row);
            grid.add(specializationField, 1, row++);
        } else if (role.equals("Patient")) {
            grid.add(makeLabel("Gender:"), 0, row);
            grid.add(genderField, 1, row++);

            grid.add(makeLabel("Date of Birth (YYYY-MM-DD):"), 0, row);
            grid.add(dobField, 1, row++);
        }

        // Add signup button at the bottom
        grid.add(signupButton, 1, row);
    }

    /**
     * Creates a styled label with consistent formatting
     * @param text The label text
     * @return Configured Label object
     */
    private Label makeLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.LIGHTGRAY);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        return label;
    }

    /**
     * Handles the signup process including:
     * 1. Field validation
     * 2. Database operations
     * 3. User feedback
     */
    private void handleSignup() {
        // Get form values
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String specialization = specializationField.getText();
        String gender = genderField.getText();
        String dob = dobField.getText();

        // Validate required fields
        if (username.isEmpty() || password.isEmpty() || role == null ||
                name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showAlert("Please fill all required fields.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);

            try {
                // 1. Insert into users table
                String insertUser = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                PreparedStatement userStmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.setString(3, role);
                userStmt.executeUpdate();

                // Get generated user ID
                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                int userId = -1;
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1);
                }

                if (userId == -1) {
                    showAlert("Error creating user.");
                    conn.rollback();
                    return;
                }

                // 2. Insert into role-specific table
                switch (role) {
                    case "Doctor":
                        if (specialization.isEmpty()) {
                            showAlert("Please enter specialization.");
                            conn.rollback();
                            return;
                        }
                        String doctorSQL = "INSERT INTO doctors (user_id, name, specialization, phone, email) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement doctorStmt = conn.prepareStatement(doctorSQL)) {
                            doctorStmt.setInt(1, userId);
                            doctorStmt.setString(2, name);
                            doctorStmt.setString(3, specialization);
                            doctorStmt.setString(4, phone);
                            doctorStmt.setString(5, email);
                            doctorStmt.executeUpdate();
                        }
                        break;

                    case "Patient":
                        if (gender.isEmpty() || dob.isEmpty()) {
                            showAlert("Please enter gender and date of birth.");
                            conn.rollback();
                            return;
                        }
                        String patientSQL = "INSERT INTO patients (user_id, name, gender, dob, phone, address) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement patientStmt = conn.prepareStatement(patientSQL)) {
                            patientStmt.setInt(1, userId);
                            patientStmt.setString(2, name);
                            patientStmt.setString(3, gender);
                            patientStmt.setDate(4, Date.valueOf(dob));
                            patientStmt.setString(5, phone);
                            patientStmt.setString(6, email); // Using email as address
                            patientStmt.executeUpdate();
                        }
                        break;

                    case "Admin":
                        String adminSQL = "INSERT INTO admins (user_id, name, email, phone) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement adminStmt = conn.prepareStatement(adminSQL)) {
                            adminStmt.setInt(1, userId);
                            adminStmt.setString(2, name);
                            adminStmt.setString(3, email);
                            adminStmt.setString(4, phone);
                            adminStmt.executeUpdate();
                        }
                        break;
                }

                // Commit transaction if all operations succeeded
                conn.commit();
                showAlert("Signup successful!");
                clearForm();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            showAlert("Signup failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showAlert("Invalid date format. Please use YYYY-MM-DD.");
        }
    }

    /**
     * Clears all form fields
     */
    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        specializationField.clear();
        genderField.clear();
        dobField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }

    /**
     * Shows an information alert dialog
     * @param msg The message to display
     */
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}