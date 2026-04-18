import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.PrintWriter;
import java.sql.*;

public class FeesGUI {

    static final String DB_URL = "jdbc:mysql://localhost:3306/fees_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "dbms";

    JFrame frame;

    public FeesGUI() {
        frame = new JFrame("Fees Management System");
        frame.setSize(1050, 650); 
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.decode("#f4f6f9"));
        frame.setLocationRelativeTo(null); // Center on screen

        // ================= HEADER BANNER =================
        JLabel headerLabel = new JLabel("🎓 FEES MANAGEMENT SYSTEM", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setOpaque(true);
        headerLabel.setBackground(Color.decode("#2c3e50")); 
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        frame.add(headerLabel, BorderLayout.NORTH);

        // ================= TABS =================
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabs.setBackground(Color.WHITE);

        tabs.addTab("🏠 Dashboard", dashboardPanel());
        tabs.addTab("💰 Fees", feesPanel());
        tabs.addTab("🧑‍🎓 Students", studentPanel());
        tabs.addTab("💳 Payments", paymentPanel());

        frame.add(tabs, BorderLayout.CENTER);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // ================= DASHBOARD PANEL =================
    JPanel dashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(Color.decode("#f4f6f9"));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel studentsCard = createDashboardCard("🧑‍🎓 Total Students", "0", "#3498db");
        JLabel paymentsCard = createDashboardCard("💳 Total Payments Collected", "$0", "#2ecc71");
        
        JButton refreshBtn = createButton("🔄 Refresh Stats", "#2c3e50");
        refreshBtn.setPreferredSize(new Dimension(250, 60));
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));

        refreshBtn.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS);
                 Statement s = c.createStatement()) {
                
                ResultSet rs1 = s.executeQuery("SELECT COUNT(*) FROM students");
                if (rs1.next()) updateDashboardCard(studentsCard, "🧑‍🎓 Total Students", String.valueOf(rs1.getInt(1)));

                ResultSet rs2 = s.executeQuery("SELECT SUM(amount) FROM payments");
                if (rs2.next()) updateDashboardCard(paymentsCard, "💳 Total Payments Collected", "$" + rs2.getInt(1));

            } catch (Exception ex) {}
        });

        JPanel refreshWrapper = new JPanel(new GridBagLayout()); 
        refreshWrapper.setBackground(Color.decode("#f4f6f9"));
        refreshWrapper.add(refreshBtn);

        panel.add(studentsCard);
        panel.add(paymentsCard);
        panel.add(refreshWrapper);

        refreshBtn.doClick(); 
        return panel;
    }

    // ================= FEES PANEL =================
    JPanel feesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.decode("#f4f6f9"));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Amount"}, 0);
        JTable table = new JTable(model);
        styleTable(table);

        panel.add(createSearchPanel(model, table), BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.decode("#f4f6f9"));
        
        JButton load = createButton("🔄 Load", "#3498db");
        JButton add = createButton("➕ Add", "#2ecc71");
        JButton update = createButton("✏️ Update", "#f39c12");
        JButton delete = createButton("🗑️ Delete", "#e74c3c");
        JButton export = createButton("📥 Export CSV", "#34495e");

        btnPanel.add(load); btnPanel.add(add); btnPanel.add(update); btnPanel.add(delete); btnPanel.add(export);

        load.addActionListener(e -> {
            model.setRowCount(0);
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS); Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery("SELECT * FROM fees");
                while (rs.next()) model.addRow(new Object[]{ rs.getString(1), rs.getString(2), rs.getInt(3) });
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage()); }
        });

        add.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement ps = c.prepareStatement("INSERT INTO fees VALUES (?, ?, ?)");
                ps.setString(1, JOptionPane.showInputDialog("ID"));
                ps.setString(2, JOptionPane.showInputDialog("Name"));
                ps.setInt(3, Integer.parseInt(JOptionPane.showInputDialog("Amount")));
                ps.executeUpdate();
                load.doClick(); 
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage()); }
        });

        update.addActionListener(e -> {
            // ... (Update Logic)
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(frame, "Select a row!"); return; }
            int mRow = table.convertRowIndexToModel(r); 

            String oldId = model.getValueAt(mRow, 0).toString();
            JTextField idField = new JTextField(oldId); idField.setEditable(false); 
            JTextField nameField = new JTextField(model.getValueAt(mRow, 1).toString());
            JTextField amtField = new JTextField(model.getValueAt(mRow, 2).toString());

            JPanel form = new JPanel(new GridLayout(3, 2, 5, 10));
            form.add(new JLabel("ID:")); form.add(idField);
            form.add(new JLabel("Name:")); form.add(nameField);
            form.add(new JLabel("Amount:")); form.add(amtField);

            if (JOptionPane.showConfirmDialog(frame, form, "Update", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("UPDATE fees SET name=?, amount_paid=? WHERE student_id=?");
                    ps.setString(1, nameField.getText()); ps.setInt(2, Integer.parseInt(amtField.getText())); ps.setString(3, oldId);
                    ps.executeUpdate(); load.doClick(); 
                } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage()); }
            }
        });

        delete.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(frame, "Select a row!"); return; }
            String id = model.getValueAt(table.convertRowIndexToModel(r), 0).toString();
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement ps = c.prepareStatement("DELETE FROM fees WHERE student_id=?");
                ps.setString(1, id);  ps.executeUpdate(); load.doClick(); 
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage()); }
        });

        export.addActionListener(e -> exportToCSV(table, "Fees Info"));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        load.doClick(); 
        return panel;
    }

    // ================= STUDENTS PANEL =================
    JPanel studentPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.decode("#f4f6f9"));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Dept", "Phone"}, 0);
        JTable table = new JTable(model);
        styleTable(table);

        panel.add(createSearchPanel(model, table), BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.decode("#f4f6f9"));

        JButton load = createButton("🔄 Load", "#3498db");
        JButton add = createButton("➕ Add", "#2ecc71");
        JButton update = createButton("✏️ Update", "#f39c12");
        JButton delete = createButton("🗑️ Delete", "#e74c3c");
        JButton export = createButton("📥 Export CSV", "#34495e");

        btnPanel.add(load); btnPanel.add(add); btnPanel.add(update); btnPanel.add(delete); btnPanel.add(export);

        load.addActionListener(e -> {
            model.setRowCount(0);
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS); Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery("SELECT * FROM students");
                while (rs.next()) model.addRow(new Object[]{ rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4) });
            } catch (Exception ex) {}
        });

        add.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement ps = c.prepareStatement("INSERT INTO students VALUES (?, ?, ?, ?)");
                ps.setString(1, JOptionPane.showInputDialog("ID"));
                ps.setString(2, JOptionPane.showInputDialog("Name"));
                ps.setString(3, JOptionPane.showInputDialog("Dept"));
                ps.setString(4, JOptionPane.showInputDialog("Phone"));
                ps.executeUpdate(); load.doClick(); 
            } catch (Exception ex) {}
        });

        update.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) return;
            int mRow = table.convertRowIndexToModel(r); 

            String oldId = model.getValueAt(mRow, 0).toString();
            JTextField idField = new JTextField(oldId); idField.setEditable(false); 
            JTextField nameField = new JTextField(model.getValueAt(mRow, 1).toString());
            JTextField deptField = new JTextField(model.getValueAt(mRow, 2).toString());
            JTextField phoneField = new JTextField(model.getValueAt(mRow, 3).toString());

            JPanel form = new JPanel(new GridLayout(4, 2, 5, 10));
            form.add(new JLabel("ID:")); form.add(idField);
            form.add(new JLabel("Name:")); form.add(nameField);
            form.add(new JLabel("Dept:")); form.add(deptField);
            form.add(new JLabel("Phone:")); form.add(phoneField);

            if (JOptionPane.showConfirmDialog(frame, form, "Update", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("UPDATE students SET name=?, department=?, phone=? WHERE student_id=?");
                    ps.setString(1, nameField.getText()); ps.setString(2, deptField.getText());
                    ps.setString(3, phoneField.getText()); ps.setString(4, oldId);
                    ps.executeUpdate(); load.doClick(); 
                } catch (Exception ex) {}
            }
        });

        delete.addActionListener(e -> {
            int r = table.getSelectedRow();  if (r == -1) return;
            String id = model.getValueAt(table.convertRowIndexToModel(r), 0).toString();
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE student_id=?");
                ps.setString(1, id); ps.executeUpdate(); load.doClick(); 
            } catch (Exception ex) {}
        });

        export.addActionListener(e -> exportToCSV(table, "Students Info"));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        load.doClick(); 
        return panel;
    }

    // ================= PAYMENTS PANEL =================
    JPanel paymentPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.decode("#f4f6f9"));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        DefaultTableModel model = new DefaultTableModel(new String[]{"Payment ID", "Student ID", "Amount", "Date"}, 0);
        JTable table = new JTable(model);
        styleTable(table);

        panel.add(createSearchPanel(model, table), BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.decode("#f4f6f9"));

        JButton load = createButton("🔄 Load", "#3498db");
        JButton add = createButton("➕ Add", "#2ecc71");
        JButton update = createButton("✏️ Update", "#f39c12");
        JButton delete = createButton("🗑️ Delete", "#e74c3c");
        JButton export = createButton("📥 Export CSV", "#34495e");
        JButton receipt = createButton("🖨️ Receipt", "#9b59b6"); // Purple

        btnPanel.add(load); btnPanel.add(add); btnPanel.add(update); btnPanel.add(delete); btnPanel.add(export); btnPanel.add(receipt);

        load.addActionListener(e -> {
            model.setRowCount(0);
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS); Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery("SELECT * FROM payments");
                while (rs.next()) model.addRow(new Object[]{ rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getDate(4) });
            } catch (Exception ex) {}
        });

        add.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement ps = c.prepareStatement("INSERT INTO payments (student_id, amount, payment_date) VALUES (?, ?, ?)");
                ps.setString(1, JOptionPane.showInputDialog("Student ID"));
                ps.setInt(2, Integer.parseInt(JOptionPane.showInputDialog("Amount")));
                ps.setString(3, JOptionPane.showInputDialog("Date (YYYY-MM-DD)"));
                ps.executeUpdate(); load.doClick(); 
            } catch (Exception ex) {}
        });

        update.addActionListener(e -> {
            int r = table.getSelectedRow(); if (r == -1) return;
            int mRow = table.convertRowIndexToModel(r); 

            String payIdStr = model.getValueAt(mRow, 0).toString();
            JTextField payIdField = new JTextField(payIdStr); payIdField.setEditable(false); 
            JTextField stuField = new JTextField(model.getValueAt(mRow, 1).toString());
            JTextField amtField = new JTextField(model.getValueAt(mRow, 2).toString());
            JTextField dateField = new JTextField(model.getValueAt(mRow, 3).toString());

            JPanel form = new JPanel(new GridLayout(4, 2, 5, 10));
            form.add(new JLabel("Payment ID:")); form.add(payIdField);
            form.add(new JLabel("Student ID:")); form.add(stuField);
            form.add(new JLabel("Amount:")); form.add(amtField);
            form.add(new JLabel("Date:")); form.add(dateField);

            if (JOptionPane.showConfirmDialog(frame, form, "Update", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("UPDATE payments SET student_id=?, amount=?, payment_date=? WHERE payment_id=?");
                    ps.setString(1, stuField.getText()); ps.setInt(2, Integer.parseInt(amtField.getText()));
                    ps.setString(3, dateField.getText()); ps.setInt(4, Integer.parseInt(payIdStr));
                    ps.executeUpdate(); load.doClick(); 
                } catch (Exception ex) {}
            }
        });

        delete.addActionListener(e -> {
            int r = table.getSelectedRow(); if (r == -1) return;
            int id = Integer.parseInt(model.getValueAt(table.convertRowIndexToModel(r), 0).toString());
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement ps = c.prepareStatement("DELETE FROM payments WHERE payment_id=?");
                ps.setInt(1, id); ps.executeUpdate(); load.doClick(); 
            } catch (Exception ex) {}
        });

        export.addActionListener(e -> exportToCSV(table, "Payments Info"));

        // ======== HIGHLIGHT: NEW RECEIPT FEATURE ========
        receipt.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(frame, "Please highlight a payment row first!"); return; }
            int mRow = table.convertRowIndexToModel(r);

            String payId = model.getValueAt(mRow, 0).toString();
            String stuId = model.getValueAt(mRow, 1).toString();
            String amt = model.getValueAt(mRow, 2).toString();
            String date = model.getValueAt(mRow, 3).toString();

            String html = "<html><body style='width: 250px; font-family: monospace; text-align:center'>" +
                          "<h2>🎓 FEES ADMIN</h2>" +
                          "<hr><p><b>OFFICIAL PAYMENT RECEIPT</b></p>" +
                          "<p>Receipt No: #" + payId + "</p>" +
                          "<p>Date: " + date + "</p><hr>" +
                          "<p align='left'>Student ID : " + stuId + "<br>" +
                          "Amount Paid: <b style='color:green'>$" + amt + "</b></p>" +
                          "<hr><p><i>Thank you for your payment!</i></p></body></html>";

            JOptionPane.showMessageDialog(frame, new JLabel(html), "Receipt preview", JOptionPane.PLAIN_MESSAGE);
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        load.doClick(); 
        return panel;
    }

    // ================= EXPORT FEATURE =================
    private void exportToCSV(JTable table, String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save " + title + " as CSV");
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(fileChooser.getSelectedFile() + ".csv")) {
                for (int i = 0; i < table.getColumnCount(); i++) { pw.print(table.getColumnName(i) + ","); }
                pw.println();
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) { pw.print(table.getValueAt(i, j).toString() + ","); }
                    pw.println();
                }
                JOptionPane.showMessageDialog(frame, "Data Successfully Exported to Excel / CSV!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Export Error!"); }
        }
    }

    // ================= CUSTOM STYLINGS & SEARCH =================
    private JPanel createSearchPanel(DefaultTableModel model, JTable table) {
        JPanel scrollPnl = new JPanel(new FlowLayout(FlowLayout.LEFT)); scrollPnl.setBackground(Color.decode("#f4f6f9"));
        JLabel lbl = new JLabel("🔍 Search: "); lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JTextField txt = new JTextField(20); txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        scrollPnl.add(lbl); scrollPnl.add(txt);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model); table.setRowSorter(sorter);
        txt.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { s(); } public void removeUpdate(DocumentEvent e) { s(); } public void changedUpdate(DocumentEvent e) { s(); }
            private void s() { sorter.setRowFilter(txt.getText().trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + txt.getText())); }
        });
        return scrollPnl;
    }

    private JLabel createDashboardCard(String t, String v, String c) {
        JLabel l = new JLabel("<html><center>" + t + "<br><br><span style='font-size:42px'>" + v + "</span></center></html>", JLabel.CENTER);
        l.setOpaque(true); l.setBackground(Color.decode(c)); l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18)); l.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); return l;
    }

    private void updateDashboardCard(JLabel l, String t, String v) {
        l.setText("<html><center>" + t + "<br><br><span style='font-size:42px'>" + v + "</span></center></html>"); }

    private JButton createButton(String t, String c) {
        JButton b = new JButton(t); b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(Color.decode(c)); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15)); table.setRowHeight(35);
        table.setSelectionBackground(Color.decode("#3498db")); table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(Color.decode("#2c3e50")); table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(100, 45));
    }

    // ================= BOOT THE SYSTEM =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> launchLoginScreen());
    }

    // NEW AUTHENTICATION SCREEN
    private static void launchLoginScreen() {
        JFrame loginFrame = new JFrame("System Login");
        loginFrame.setSize(400, 350);
        loginFrame.setLayout(new BorderLayout());
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.getContentPane().setBackground(Color.decode("#ecf0f1"));

        JLabel title = new JLabel("🔒 ADMIN LOGIN", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setOpaque(true);
        title.setBackground(Color.decode("#2c3e50"));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 15, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        centerPanel.setBackground(Color.decode("#ecf0f1"));

        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        pf.setHorizontalAlignment(JTextField.CENTER);

        JButton loginBtn = new JButton("ACCESS SECURE SYSTEM");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBackground(Color.decode("#2ecc71"));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setOpaque(true);

        loginBtn.addActionListener(e -> {
            String pass = new String(pf.getPassword());
            if (pass.equals("prabha2232")) {
                loginFrame.dispose();
                new FeesGUI(); // Start real app
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Incorrect Password! Hint: Use 'admin123'", "Security Alert", JOptionPane.ERROR_MESSAGE);
            }
        });

        centerPanel.add(new JLabel("Enter Administrator Password:", JLabel.CENTER));
        centerPanel.add(pf);
        centerPanel.add(loginBtn);

        loginFrame.add(title, BorderLayout.NORTH);
        loginFrame.add(centerPanel, BorderLayout.CENTER);
        loginFrame.setVisible(true);
    }
}
