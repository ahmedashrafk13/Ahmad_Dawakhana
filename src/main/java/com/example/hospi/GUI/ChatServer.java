package com.example.hospi.GUI;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * ChatServer handles real-time messaging between clients.
 * Maintains connections with multiple clients and routes messages between them.
 */
public class ChatServer {

    // Network port the server listens on
    private static final int PORT = 12345;

    // Thread-safe map to store client output streams keyed by username
    private static Map<String, PrintWriter> clientWriters =
            Collections.synchronizedMap(new HashMap<>());

    /**
     * Main entry point for the chat server.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Chat server started...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Main server loop - accepts new client connections
            while (true) {
                // Wait for new client connection
                Socket clientSocket = serverSocket.accept();

                // Create new thread to handle client communication
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles communication with a single client connection.
     * @param socket The client's socket connection
     */
    private static void handleClient(Socket socket) {
        String username = null;

        try (
                // Create input/output streams for the connection
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Step 1: Receive and validate username
            username = in.readLine();
            if (username == null || username.isBlank()) {
                socket.close();
                return;
            }

            // Register client in the shared map
            clientWriters.put(username, out);
            System.out.println(username + " connected.");

            // Step 2: Listen for incoming messages
            String message;
            while ((message = in.readLine()) != null) {
                // Process messages with "TO:" prefix (outgoing messages)
                if (message.startsWith("TO:")) {
                    String[] parts = message.substring(3).split(":", 2);
                    if (parts.length == 2) {
                        String receiver = parts[0];
                        String text = parts[1];
                        sendMessage(username, receiver, text);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Connection lost with " + username);
        } finally {
            // Cleanup when client disconnects
            if (username != null) {
                clientWriters.remove(username);
                System.out.println(username + " disconnected.");
            }
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore close exceptions
            }
        }
    }

    /**
     * Routes a message from sender to receiver.
     * @param sender Username of message sender
     * @param receiver Username of intended recipient
     * @param text The message content
     */
    private static void sendMessage(String sender, String receiver, String text) {
        // Lookup recipient's output stream
        PrintWriter receiverOut = clientWriters.get(receiver);

        if (receiverOut != null) {
            // Format and send message with "FROM:" prefix
            receiverOut.println("FROM:" + sender + ":" + text);
        } else {
            System.out.println("User " + receiver + " not found.");
        }
    }
}