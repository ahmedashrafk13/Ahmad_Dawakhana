package com.example.hospi.GUI;

import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import com.twilio.rest.api.v2010.account.Message;

/**
 * A class for sending SMS messages using the Twilio API.
 */
public class TwilioSMS {
    // Twilio Credentials from your account
    // Replace these with your actual Twilio account credentials
    public static final String ACCOUNT_SID = "Your SID";
    public static final String AUTH_TOKEN = "Your Token";

    // Static initializer block that runs when the class is loaded
    // Initializes the Twilio client with the account credentials
    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    /**
     * Sends an SMS message to the specified phone number.
     *
     * @param toPhoneNumber The recipient's phone number in E.164 format (e.g., "+15551234567")
     * @param messageContent The text content of the SMS message to be sent
     */
    public void sendSMS(String toPhoneNumber, String messageContent) {
        try {
            // Create and send the SMS message using Twilio's API
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber), // To phone number (recipient)
                    new PhoneNumber("+19706603801"), // From phone number (your Twilio number)
                    messageContent // The message body
            ).create();

            // Print the message SID (unique identifier) upon successful sending
            System.out.println("SMS sent: " + message.getSid());
        } catch (Exception e) {
            // Print the stack trace if an error occurs during SMS sending
            e.printStackTrace();
        }
    }
}