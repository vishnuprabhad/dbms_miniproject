import java.sql.*;
import java.util.Scanner;

public class FeesManagement {

    static final String DB_URL = "jdbc:mysql://localhost:3306/fees_db?useSSL=false&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "dbms";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {

            while (true) {

                System.out.println("\n===== FEES MANAGEMENT SYSTEM =====");
                System.out.println("1. View Students");
                System.out.println("2. Add Student");
                System.out.println("3. View Fees");
                System.out.println("4. Update Fees");
                System.out.println("5. Delete Student Fees");
                System.out.println("6. Add Payment");
                System.out.println("7. View Payments");
                System.out.println("8. Total Paid by Student");
                System.out.println("9. Exit");
                System.out.print("Enter choice: ");

                int choice = sc.nextInt();

                switch (choice) {

                    case 1:
                        viewStudents(conn);
                        break;

                    case 2:
                        addStudent(conn, sc);
                        break;

                    case 3:
                        viewFees(conn);
                        break;

                    case 4:
                        updateFees(conn, sc);
                        break;

                    case 5:
                        deleteFees(conn, sc);
                        break;

                    case 6:
                        addPayment(conn, sc);
                        break;

                    case 7:
                        viewPayments(conn);
                        break;

                    case 8:
                        totalPaid(conn, sc);
                        break;

                    case 9:
                        System.out.println("Exiting...");
                        return;

                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= STUDENTS =================

    static void viewStudents(Connection conn) throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM students");

        System.out.println("\n--- Students ---");
        while (rs.next()) {
            System.out.println(
                rs.getString("student_id") + " | " +
                rs.getString("name") + " | " +
                rs.getString("department") + " | " +
                rs.getString("phone")
            );
        }
    }

    static void addStudent(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter ID: ");
        String id = sc.next();

        System.out.print("Enter Name: ");
        String name = sc.next();

        System.out.print("Enter Department: ");
        String dept = sc.next();

        System.out.print("Enter Phone: ");
        String phone = sc.next();

        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO students VALUES (?, ?, ?, ?)"
        );

        ps.setString(1, id);
        ps.setString(2, name);
        ps.setString(3, dept);
        ps.setString(4, phone);

        ps.executeUpdate();
        System.out.println("Student Added!");
    }

    // ================= FEES =================

    static void viewFees(Connection conn) throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM fees");

        System.out.println("\n--- Fees ---");
        while (rs.next()) {
            System.out.println(
                rs.getString("student_id") + " | " +
                rs.getString("name") + " | " +
                rs.getInt("amount_paid")
            );
        }
    }

    static void updateFees(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter Student ID: ");
        String id = sc.next();

        System.out.print("Enter New Amount: ");
        int amount = sc.nextInt();

        PreparedStatement ps = conn.prepareStatement(
                "UPDATE fees SET amount_paid=? WHERE student_id=?"
        );

        ps.setInt(1, amount);
        ps.setString(2, id);

        ps.executeUpdate();
        System.out.println("Fees Updated!");
    }

    static void deleteFees(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter Student ID: ");
        String id = sc.next();

        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM fees WHERE student_id=?"
        );

        ps.setString(1, id);

        ps.executeUpdate();
        System.out.println("Fees Deleted!");
    }

    // ================= PAYMENTS =================

    static void addPayment(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter Student ID: ");
        String id = sc.next();

        System.out.print("Enter Amount: ");
        int amount = sc.nextInt();

        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = sc.next();

        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO payments (student_id, amount, payment_date) VALUES (?, ?, ?)"
        );

        ps.setString(1, id);
        ps.setInt(2, amount);
        ps.setString(3, date);

        ps.executeUpdate();
        System.out.println("Payment Added!");
    }

    static void viewPayments(Connection conn) throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM payments");

        System.out.println("\n--- Payments ---");
        while (rs.next()) {
            System.out.println(
                rs.getInt("payment_id") + " | " +
                rs.getString("student_id") + " | " +
                rs.getInt("amount") + " | " +
                rs.getDate("payment_date")
            );
        }
    }

    static void totalPaid(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter Student ID: ");
        String id = sc.next();

        PreparedStatement ps = conn.prepareStatement(
                "SELECT SUM(amount) FROM payments WHERE student_id=?"
        );

        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println("Total Paid: " + rs.getInt(1));
        }
    }
}