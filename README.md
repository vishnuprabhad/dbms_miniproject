Fees Management System (Java + MySQL)
📌 Project Overview

The Fees Management System is a Java-based desktop application designed to manage student records, fees, and payments efficiently.
It includes both Console-based and GUI (Swing-based) implementations with database integration using MySQL.

🚀 Features
🧑‍🎓 Student Management
Add new students
View student details
Update student information
Delete student records

💰 Fees Management
Add fees details
View fees records
Update fee amount
Delete fee records

💳 Payment Management
Record student payments
View payment history
Calculate total payments per student
Generate payment receipt (GUI feature)

📊 Dashboard (GUI)
Total number of students
Total payments collected
Refresh statistics

🔍 Additional Features
Search functionality (real-time filtering)
Export data to CSV (Excel compatible)
User-friendly GUI with tabs
Secure login screen

🛠️ Technologies Used
Java (JDK 8+)
Swing (GUI)
JDBC (Java Database Connectivity)
MySQL Database

🗄️ Database Setup
1. Create Database
CREATE DATABASE fees_db;
USE fees_db;
2. Create Tables
CREATE TABLE students (
    student_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(50),
    department VARCHAR(50),
    phone VARCHAR(15)
);

CREATE TABLE fees (
    student_id VARCHAR(20),
    name VARCHAR(50),
    amount_paid INT,
    FOREIGN KEY (student_id) REFERENCES students(student_id)
);

CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20),
    amount INT,
    payment_date DATE,
    FOREIGN KEY (student_id) REFERENCES students(student_id)
);
⚙️ Configuration
Update database credentials in both Java files:

static final String DB_URL = "jdbc:mysql://localhost:3306/fees_db";
static final String USER = "root";
static final String PASS = "your_password";

▶️ How to Run
1. Compile the Program
javac FeesManagement.java
javac FeesGUI.java
2. Run Console Application
java FeesManagement
3. Run GUI Application
java FeesGUI
🔐 Login Credentials (GUI)
Password: prabha2232
(Hint shown in app: admin123)

📸 Screens (Optional - Add screenshots in GitHub)
Login Screen
Dashboard
Students Tab
Fees Tab
Payments Tab

💡 Future Enhancements
Role-based login system
Email/SMS notifications
Online payment integration
Report generation (PDF)
Cloud database support

🙌 Author
Vishnuprabha.D
