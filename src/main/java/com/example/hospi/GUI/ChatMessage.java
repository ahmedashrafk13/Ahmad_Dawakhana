package com.example.hospi.GUI;

import java.security.Timestamp;

/**
 * Represents a chat message with sender information and timestamp.
 * This immutable class stores all data related to a single chat message,
 * including who sent it, the message content, and when it was sent.
 */
public class ChatMessage {
    // Immutable fields - can only be set at construction time
    private final int senderId;      // ID of the user who sent the message
    private final String messageText; // Content of the message
    private final Timestamp sentTime; // When the message was sent (with date and time)

    /**
     * Constructs a new ChatMessage with all required fields.
     * @param senderId Unique identifier of the message sender
     * @param messageText The content of the message
     * @param sentTime When the message was sent (including date and time)
     */
    public ChatMessage(int senderId, String messageText, Timestamp sentTime) {
        this.senderId = senderId;
        this.messageText = messageText;
        this.sentTime = sentTime;
    }

    /**
     * @return The ID of the user who sent this message
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * @return The text content of this message
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * @return The timestamp when this message was sent
     */
    public Timestamp getSentTime() {
        return sentTime;
    }
}