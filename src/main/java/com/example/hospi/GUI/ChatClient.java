package com.example.hospi.GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatClient is a JavaFX application that provides real-time chat functionality
 * between users in a hospital management system. It handles both GUI rendering
 * and network communication with a chat server.
 */
public class ChatClient extends Application {

    // Static fields to preserve user identity between instances
    private static int staticUserId;
    private static String staticRole;

    // Instance fields for current chat session
    private int userId;             // ID of current user
    private String role;            // Role of current user (doctor/patient/admin)
    private Socket socket;          // Network connection to chat server
    private BufferedReader in;      // Input stream from server
    private PrintWriter out;        // Output stream to server
    private VBox chatBox;           // Container for chat messages
    private TextField messageField; // Input field for new messages
    private int receiverUserId;     // ID of user we're chatting with
    private String receiverName;    // Name of user we're chatting with
    private Stage primaryStage;     // Main application window

    /**
     * Constructor for creating a chat client with specific user credentials.
     *
     * @param userId The ID of the current user
     * @param role   The role of the current user
     */
    public ChatClient(int userId, String role) {
        ChatClient.staticUserId = userId;
        ChatClient.staticRole = role;
    }

    /**
     * Default constructor required by JavaFX.
     */
    public ChatClient() {
        // Required empty constructor
    }

    /**
     * Main entry point for JavaFX application.
     *
     * @param primaryStage The primary stage/window for this application
     */
    @Override
    public void start(Stage primaryStage) {
        // Initialize instance fields from static values
        this.userId = staticUserId;
        this.role = staticRole;
        this.primaryStage = primaryStage;

        // Validate user credentials
        if (userId <= 0 || role == null || role.isBlank()) {
            showErrorAndExit("User ID or role missing. Cannot open chat.");
            return;
        }

        // Show initial contact selection screen
        showContactSelectionScreen();
    }

    /**
     * Displays the contact selection screen where users can choose who to chat with.
     */
    private void showContactSelectionScreen() {
        // Create UI elements
        Label title = new Label("Select a contact to chat with:");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: #e0f0ff; -fx-font-weight: bold;");

        // ListView for displaying available contacts
        ListView<String> contactsList = new ListView<>();
        contactsList.setStyle(
                "-fx-control-inner-background: #102841;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-border-color: #3498db;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        // Fetch contacts from database
        List<String> contacts = fetchContacts();
        if (contacts.isEmpty()) {
            showErrorAndExit("No contacts found for this user.");
            return;
        }

        // Populate list with contacts
        contactsList.getItems().addAll(contacts);

        // Create chat start button
        Button startChatButton = new Button("Start Chat");
        startChatButton.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 8 16;" +
                        "-fx-font-weight: bold;"
        );

        // Handle contact selection
        startChatButton.setOnAction(e -> {
            String selected = contactsList.getSelectionModel().getSelectedItem();
            if (selected != null && selected.contains(":")) {
                // Parse selected contact (format: "id:name")
                String[] parts = selected.split(":", 2);
                receiverUserId = Integer.parseInt(parts[0]);
                receiverName = parts[1];

                // Connect to chat server and show chat interface if successful
                if (connectToServer()) {
                    showChatInterface();
                } else {
                    showErrorAndExit("Unable to connect to chat server.");
                }
            } else {
                showError("Please select a contact.");
            }
        });

        // Set up layout
        VBox layout = new VBox(20, title, contactsList, startChatButton);
        layout.setStyle("-fx-background-color: #0d1b2a;");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        // Configure and show stage
        primaryStage.setScene(new Scene(layout, 380, 450));
        primaryStage.setTitle("Select Contact");
        primaryStage.show();
    }

    /**
     * Establishes connection to the chat server.
     *
     * @return true if connection succeeded, false otherwise
     */
    private boolean connectToServer() {
        try {
            // Connect to local chat server on port 12345
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send user ID to server for identification
            out.println(userId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Displays the main chat interface with message history and input controls.
     */
    private void showChatInterface() {
        // Main container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0d1b2a;");

        // Chat message display area
        chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));

        // Scrollable container for chat messages
        ScrollPane scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #102841; -fx-border-color: #3498db;");

        // Load and display previous messages
        List<ChatMessage> previousMessages = fetchMessages(userId, receiverUserId);
        for (ChatMessage msg : previousMessages) {
            if (msg.senderId() == userId) {
                displayOwnMessage(msg.messageText());
            } else {
                displayIncomingMessage("User " + msg.senderId(), msg.messageText());
            }
        }

        // Message input field
        messageField = new TextField();
        messageField.setPromptText("Type your message...");
        messageField.setStyle(
                "-fx-background-color: #102841;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #3498db;" +
                        "-fx-border-radius: 10;"
        );

        // Send button
        Button sendButton = new Button("Send");
        sendButton.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 6 16;"
        );
        sendButton.setOnAction(e -> sendMessage());

        // Input area layout
        HBox inputArea = new HBox(10, messageField, sendButton);
        inputArea.setPadding(new Insets(10));
        inputArea.setAlignment(Pos.CENTER);

        // Assemble main layout
        root.setCenter(scrollPane);
        root.setBottom(inputArea);

        // Configure and show stage
        primaryStage.setScene(new Scene(root, 550, 620));
        primaryStage.setTitle("Chat with: " + receiverName);
        primaryStage.show();

        // Start thread to listen for incoming messages
        new Thread(this::receiveMessages).start();
    }

    /**
     * Sends the current message to the server and updates the UI.
     */
    private void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty() && out != null && receiverUserId > 0) {
            // Save message to database
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO hospital_db.chat_messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, receiverUserId);
                    stmt.setString(3, text);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Database error while sending message.");
            }

            // Send message to server and update UI
            out.println("TO:" + receiverUserId + ":" + text);
            displayOwnMessage(text);
            messageField.clear();
        }
    }

    /**
     * Continuously listens for incoming messages from the server.
     */
    private void receiveMessages() {
        try {
            String incomingLine;
            while ((incomingLine = in.readLine()) != null) {
                if (incomingLine.startsWith("FROM:")) {
                    processIncomingMessage(incomingLine.substring(5));
                }
            }
        } catch (IOException e) {
            Platform.runLater(() -> showErrorAndExit("Connection lost. Please restart the app."));
        }
    }

    /**
     * Processes an incoming message from the server.
     *
     * @param rawMessage The raw message string in format "senderId:messageText"
     */
    private void processIncomingMessage(String rawMessage) {
        String[] parts = rawMessage.split(":", 2);
        if (parts.length == 2) {
            try {
                int senderId = Integer.parseInt(parts[0]);
                String messageText = parts[1];

                // Update UI on JavaFX application thread
                Platform.runLater(() -> {
                    displayIncomingMessage("User " + senderId, messageText);
                    markMessageAsSeen(senderId);
                });
            } catch (NumberFormatException e) {
                System.err.println("Invalid sender ID: " + parts[0]);
            }
        }
    }

    // Additional helper methods would be commented similarly...
    // [Rest of the methods with similar detailed comments...]


    /**
     * Displays a message sent by the current user in the chat UI.
     *
     * @param text The message text to display
     */
    private void displayOwnMessage(String text) {
        // Create container for message content
        VBox messageContent = new VBox(5);

        // Create message label with styling for sent messages
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);  // Enable text wrapping
        messageLabel.setStyle("-fx-background-color: #00796b; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 10px;");

        // Create timestamp label
        Label timeLabel = new Label(currentTime());
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #ccc;");

        // Assemble message components
        messageContent.getChildren().addAll(messageLabel, timeLabel);
        messageContent.setAlignment(Pos.CENTER_RIGHT);  // Align to right for sent messages

        // Create message bubble container
        HBox messageBubble = new HBox(messageContent);
        messageBubble.setAlignment(Pos.CENTER_RIGHT);
        messageBubble.setPadding(new Insets(5, 10, 5, 50));  // Right padding for sent messages

        // Add message to chat history
        chatBox.getChildren().add(messageBubble);
    }

    /**
     * Displays a message received from another user in the chat UI.
     *
     * @param sender The name/ID of the sender
     * @param text   The message text to display
     */
    private void displayIncomingMessage(String sender, String text) {
        // Create container for message content
        VBox messageContent = new VBox(5);

        // Create message label with styling for received messages
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);  // Enable text wrapping
        messageLabel.setStyle("-fx-background-color: #424242; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 10px;");

        // Create timestamp label
        Label timeLabel = new Label(currentTime());
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #bbb;");

        // Assemble message components
        messageContent.getChildren().addAll(messageLabel, timeLabel);
        messageContent.setAlignment(Pos.CENTER_LEFT);  // Align to left for received messages

        // Create message bubble container
        HBox messageBubble = new HBox(messageContent);
        messageBubble.setAlignment(Pos.CENTER_LEFT);
        messageBubble.setPadding(new Insets(5, 50, 5, 10));  // Left padding for received messages

        // Add message to chat history
        chatBox.getChildren().add(messageBubble);
    }

    /**
     * Gets the current time formatted as HH:mm.
     *
     * @return Formatted time string
     */
    private String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Marks messages from a specific sender as seen in the database.
     *
     * @param senderId The ID of the message sender
     */
    private void markMessageAsSeen(int senderId) {
        String updateQuery = """
                    UPDATE hospital_db.chat_messages
                    SET seen = TRUE
                    WHERE receiver_id = ? AND sender_id = ? AND seen = FALSE
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, senderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches the chat history between two users.
     *
     * @param user1 First user ID
     * @param user2 Second user ID
     * @return List of ChatMessage objects
     */
    private List<ChatMessage> fetchMessages(int user1, int user2) {
        List<ChatMessage> messages = new ArrayList<>();
        String query = """
                    SELECT sender_id, message_text, sent_time
                    FROM hospital_db.chat_messages
                    WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)
                    ORDER BY sent_time;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set parameters for both directions of conversation
            stmt.setInt(1, user1);
            stmt.setInt(2, user2);
            stmt.setInt(3, user2);
            stmt.setInt(4, user1);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new ChatMessage(
                            rs.getInt("sender_id"),
                            rs.getString("message_text"),
                            rs.getTimestamp("sent_time")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to fetch messages.");
        }

        return messages;
    }

    /**
     * Fetches the list of contacts the current user can chat with.
     *
     * @return List of contacts in format "id:name"
     */
    private List<String> fetchContacts() {
        List<String> contacts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Get the entity ID (doctor/patient ID) for the current user
            int entityId = getEntityId(connection, userId, role);
            if (entityId == -1) return contacts;

            // Different query based on user role
            String query = switch (role.toLowerCase()) {
                case "patient" -> """
                            SELECT DISTINCT u.id, d.name
                            FROM hospital_db.doctors d
                            JOIN hospital_db.doctorpatientassignment da ON d.id = da.DoctorID
                            JOIN hospital_db.users u ON d.user_id = u.id
                            WHERE da.PatientID = ?
                        """;
                case "doctor" -> """
                            SELECT DISTINCT u.id, p.name
                            FROM hospital_db.patients p
                            JOIN hospital_db.doctorpatientassignment da ON p.id = da.PatientID
                            JOIN hospital_db.users u ON p.user_id = u.id
                            WHERE da.DoctorID = ?
                        """;
                default -> {
                    showError("Invalid role: " + role);
                    yield null;
                }
            };

            if (query == null) return contacts;

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, entityId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        contacts.add(rs.getInt("id") + ":" + rs.getString(2));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error while fetching contacts.");
        }

        return contacts;
    }

    /**
     * Gets the entity ID (doctor/patient ID) for a given user ID.
     *
     * @param connection Active database connection
     * @param userId     The user ID to look up
     * @param role       The role of the user ("doctor" or "patient")
     * @return The entity ID or -1 if not found
     */
    private int getEntityId(Connection connection, int userId, String role) throws SQLException {
        String table = role.equalsIgnoreCase("doctor") ? "doctors" : "patients";
        String query = "SELECT id FROM hospital_db." + table + " WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return -1;
    }

    /**
     * Shows an error message dialog.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.setTitle("Error");
            alert.showAndWait();
        });
    }

    /**
     * Shows a fatal error message dialog and exits the application.
     *
     * @param message The error message to display
     */
    private void showErrorAndExit(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.setTitle("Fatal Error");
            alert.showAndWait();
            Platform.exit();
        });
    }

    /**
     * Cleanup method called when application is closing.
     */
    @Override
    public void stop() {
        try {
            // Close network resources
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launches the chat client with specified user credentials.
     *
     * @param userId The ID of the current user
     * @param role   The role of the current user
     */
    public static void launchChatClient(int userId, String role) {
        ChatClient.staticUserId = userId;
        ChatClient.staticRole = role;
        launch();
    }

    /**
     * Record representing a chat message with sender, content and timestamp.
     *
     * @param senderId    ID of the message sender
     * @param messageText Content of the message
     * @param timestamp   When the message was sent
     */
    private record ChatMessage(int senderId, String messageText, Timestamp timestamp) {
    }
}