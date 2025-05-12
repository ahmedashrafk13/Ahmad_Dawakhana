package com.example.hospi.GUI;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * EmailNotification - Handles sending email notifications using SMTP protocol.
 * Implements the Notifiable interface for consistent notification handling.
 * Uses Gmail's SMTP server for sending emails.
 */
class EmailNotification implements Notifiable {

    // Email account credentials (should be moved to configuration in production)
    private static final String FROM_EMAIL = "Your Email"; // Sender email address
    private static final String PASSWORD = "Your Password"; // App-specific password (not regular account password)

    /**
     * Sends an email notification to the specified recipient.
     * @param subject The subject line of the email
     * @param message The body content of the email
     * @param recipient The recipient's email address
     */
    @Override
    public void sendNotification(String subject, String message, String recipient) {
        // Configure SMTP server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true"); // Enable authentication
        props.put("mail.smtp.starttls.enable", "true"); // Enable TLS encryption
        props.put("mail.smtp.host", "smtp.gmail.com"); // Gmail SMTP server
        props.put("mail.smtp.port", "587"); // TLS port for Gmail

        // Create mail session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            // Create and configure the email message
            Message email = new MimeMessage(session);
            email.setFrom(new InternetAddress(FROM_EMAIL)); // Set sender
            email.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient)); // Set recipient
            email.setSubject(subject); // Set subject
            email.setText(message); // Set message body

            // Send the email
            Transport.send(email);
            System.out.println("✅ Email sent to " + recipient); // Success log

        } catch (MessagingException e) {
            // Error handling
            e.printStackTrace();
            System.out.println("❌ Failed to send email to " + recipient); // Error log

        }
    }
}