package com.example.hospi.GUI;


interface Notifiable {
    void sendNotification(String subject, String message, String recipient);
}




public class Main {
    public static void main(String[] args) {
        // Create an instance of EmailNotification
        EmailNotification emailNotification = new EmailNotification();

        // Message and recipient
        String message = "This is a test emergency notification from Hospital Management System!";
        String recipient = "recipientemail@example.com"; // <-- replace with actual recipient email

    }
}




