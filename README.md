# 🏥 Remote Patient Monitoring System (RPMS)

The **Remote Patient Monitoring System (RPMS)** is a Java-based desktop application built using **JavaFX** and **JFoenix**, designed to assist hospitals in monitoring patient health remotely. It provides real-time communication between doctors and patients, visualizes health data, sends emergency alerts, and generates medical reports.

---

## ✨ Key Features

- ✅ **User Roles**: Admin, Doctor, Patient
- 📊 **Vitals Monitoring**: Upload and visualize patient vitals (CSV supported)
- 🧑‍⚕️ **Doctor Recommendations**: Medication & health advice
- 🚨 **Emergency Alert System**: Auto-detection & alert for critical vitals
- 💬 **Live Chat & Video Call**: Real-time patient-doctor communication
- 📄 **PDF Report Generation**: Medical summaries and health trends
- 🔔 **Email & SMS Alerts**: Notifications for emergencies and updates
- 📅 **Appointment Scheduling**
- 🧾 **System Logs**: Admin audit trail of actions

---

## 🧰 Technologies Used

| Technology         | Description                             |
|--------------------|-----------------------------------------|
| Java 17+           | Core programming language               |
| JavaFX 17+         | User Interface                          |
| JFoenix            | Modern Material Design UI components    |
| MySQL              | Backend database                        |
| MySQL Connector/J  | Java-MySQL communication                |
| iText 7            | PDF generation                          |
| Jakarta Mail API   | Sending email alerts                    |
| Twilio             | SMS notifications                       |
| ControlsFX         | Enhanced JavaFX components              |

---

## 📦 External Libraries Used

Make sure the following libraries are added to your project:

| Library                  | Version  | Purpose                      |
|--------------------------|----------|------------------------------|
| mysql-connector-j        | 9.3.0    | MySQL JDBC connectivity      |
| itext7-core              | 7.2.x    | PDF report generation        |
| jakarta.mail             | 2.0.1    | Email notifications          |
| twilio                   | 9.0.0    | SMS notifications            |
| jfoenix                  | 9.0.10   | JavaFX material components   |
| controlsfx               | Latest   | Advanced JavaFX UI controls  |

---

## 🚀 How to Run the Project

### ✅ Prerequisites

- Java 17 or higher  
- JavaFX SDK (17+)  
- MySQL Server  
- IDE: IntelliJ IDEA / NetBeans (preferred)  
- Add all external libraries from `/lib` to your project classpath  

---

### 🖥️ Run via IDE (Recommended)

1. **Clone the project**
   ```bash
   git clone https://github.com/ahmedashrafk13/Ahmad_Dawakhana.git



2. **Open the project** in IntelliJ IDEA or NetBeans.

   
3. **Set up JavaFX SDK paths**:
   - Go to **Project Structure > Libraries**
   - Click **+** and add the path to your `javafx-sdk/lib` folder.

4. **Add external JAR libraries**:
   - Place all required `.jar` files inside the `/lib` folder.
   - Then add them to your project:
     - In IntelliJ: `File > Project Structure > Modules > Dependencies > + JARs or directories...`

5. **Set VM options for JavaFX (if needed)**:
   ```bash
   --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml


## 🗃️ Database Setup (MySQL)

To set up the MySQL database for the RPMS application, follow these steps:

### 1. Install MySQL Server
If you haven't already, install MySQL Server on your system and start the MySQL service.

### 2. Create the Database
Log in to your MySQL client (e.g., MySQL CLI or phpMyAdmin) and run:
```sql
CREATE DATABASE hospital_db;



Now run the following Queries:

-- Users table
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` enum('Admin','Doctor','Patient') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Patients table
CREATE TABLE `patients` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `patients_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Feedback table
CREATE TABLE `feedback` (
  `id` int NOT NULL AUTO_INCREMENT,
  `doctor_id` int DEFAULT NULL,
  `patient_id` int DEFAULT NULL,
  `feedback_text` text,
  `medication` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Prescriptions table
CREATE TABLE `prescriptions` (
  `PrescriptionID` int NOT NULL AUTO_INCREMENT,
  `PatientID` int NOT NULL,
  `DoctorID` int NOT NULL,
  `MedicineName` varchar(255) NOT NULL,
  `Dosage` varchar(255) NOT NULL,
  `Instructions` text,
  `PrescriptionDate` date NOT NULL,
  `Duration` int NOT NULL,
  `Refills` int DEFAULT '0',
  `Status` enum('Active','Completed','Cancelled') DEFAULT 'Active',
  `CreatedAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `UpdatedAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`PrescriptionID`),
  KEY `PatientID` (`PatientID`),
  KEY `DoctorID` (`DoctorID`),
  CONSTRAINT `prescriptions_ibfk_1` FOREIGN KEY (`PatientID`) REFERENCES `patients` (`id`),
  CONSTRAINT `prescriptions_ibfk_2` FOREIGN KEY (`DoctorID`) REFERENCES `doctors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- System logs table
CREATE TABLE `system_logs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_username` varchar(255) DEFAULT NULL,
  `action` varchar(100) DEFAULT NULL,
  `description` text,
  `timestamp` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Video call appointments table
CREATE TABLE `video_call_appointments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `doctor_id` int DEFAULT NULL,
  `patient_id` int DEFAULT NULL,
  `appointment_time` datetime DEFAULT NULL,
  `status` enum('PENDING','ACCEPTED','REJECTED') DEFAULT NULL,
  `meeting_link` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Video call requests table
CREATE TABLE `video_call_requests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `appointment_id` int DEFAULT NULL,
  `requested_time` datetime NOT NULL,
  `status` enum('pending','accepted','rejected') DEFAULT 'pending',
  `meeting_link` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `patient_id` (`patient_id`),
  KEY `doctor_id` (`doctor_id`),
  KEY `appointment_id` (`appointment_id`),
  CONSTRAINT `video_call_requests_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`),
  CONSTRAINT `video_call_requests_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`),
  CONSTRAINT `video_call_requests_ibfk_3` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Video calls table
CREATE TABLE `video_calls` (
  `id` int NOT NULL AUTO_INCREMENT,
  `appointment_id` int DEFAULT NULL,
  `doctor_id` int DEFAULT NULL,
  `patient_id` int DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `call_status` varchar(50) DEFAULT NULL,
  `video_url` text,
  PRIMARY KEY (`id`),
  KEY `appointment_id` (`appointment_id`),
  KEY `doctor_id` (`doctor_id`),
  KEY `patient_id` (`patient_id`),
  CONSTRAINT `video_calls_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`),
  CONSTRAINT `video_calls_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`),
  CONSTRAINT `video_calls_ibfk_3` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Vitals table
CREATE TABLE `vitals` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `heart_rate` int DEFAULT NULL,
  `oxygen_level` int DEFAULT NULL,
  `temperature` float DEFAULT NULL,
  `blood_pressure` varchar(20) DEFAULT NULL,
  `recorded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `patient_id` (`patient_id`),
  CONSTRAINT `vitals_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- Users table should be created first since other tables reference it
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  -- Add other user fields as needed
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Admins table
CREATE TABLE `admins` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `admins_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Doctors table
CREATE TABLE `doctors` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `specialization` varchar(100) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `doctors_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Patients table should be created before tables that reference it
CREATE TABLE `patients` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  -- Add other patient fields as needed
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `patients_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Vitals table (needed for emergency_alerts)
CREATE TABLE `vitals` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  -- Add vital sign fields as needed
  PRIMARY KEY (`id`),
  KEY `patient_id` (`patient_id`),
  CONSTRAINT `vitals_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Doctor Availability table
CREATE TABLE `doctor_availability` (
  `id` int NOT NULL AUTO_INCREMENT,
  `doctor_id` int DEFAULT NULL,
  `day_of_week` varchar(20) DEFAULT NULL,
  `available_date` date DEFAULT NULL,
  `start_time` varchar(10) DEFAULT NULL,
  `end_time` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `doctor_id` (`doctor_id`),
  CONSTRAINT `doctor_availability_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Appointments table
CREATE TABLE `appointments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `doctor_id` int DEFAULT NULL,
  `patient_id` int DEFAULT NULL,
  `appointment_date` date DEFAULT NULL,
  `start_time` time DEFAULT NULL,
  `end_time` time DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `doctor_id` (`doctor_id`),
  KEY `patient_id` (`patient_id`),
  CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`),
  CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Doctor-Patient Assignment table
CREATE TABLE `doctorpatientassignment` (
  `AssignmentID` int NOT NULL AUTO_INCREMENT,
  `DoctorID` int NOT NULL,
  `PatientID` int NOT NULL,
  `AppointmentID` int DEFAULT NULL,
  `AssignedDate` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`AssignmentID`),
  KEY `DoctorID` (`DoctorID`),
  KEY `PatientID` (`PatientID`),
  KEY `AppointmentID` (`AppointmentID`),
  CONSTRAINT `doctorpatientassignment_ibfk_1` FOREIGN KEY (`DoctorID`) REFERENCES `doctors` (`id`),
  CONSTRAINT `doctorpatientassignment_ibfk_2` FOREIGN KEY (`PatientID`) REFERENCES `patients` (`id`),
  CONSTRAINT `doctorpatientassignment_ibfk_3` FOREIGN KEY (`AppointmentID`) REFERENCES `appointments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Emergency Alerts table
CREATE TABLE `emergency_alerts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `vitals_id` int DEFAULT NULL,
  `alert_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `alert_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `patient_id` (`patient_id`),
  KEY `vitals_id` (`vitals_id`),
  CONSTRAINT `emergency_alerts_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE,
  CONSTRAINT `emergency_alerts_ibfk_2` FOREIGN KEY (`vitals_id`) REFERENCES `vitals` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Chat Messages table
CREATE TABLE `chat_messages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int NOT NULL,
  `receiver_id` int NOT NULL,
  `message_text` text NOT NULL,
  `sent_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `seen` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
