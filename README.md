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



