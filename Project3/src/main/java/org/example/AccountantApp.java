package org.example;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Name: Christian Evans
 * Course: CNT 4714 - Spring 2026
 * Assignment: Project 3
 * Date: March 2026
 * Accountant Application - read-only access to operationslog database.
 * The accountant UI blocks non-SELECT statements as a safety measure; it also permits leading parentheses and CTEs
 * (WITH ...) so users can paste in more complex read-only queries.
 */
public class AccountantApp extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton connectBtn;
    private JButton disconnectBtn;
    private JTextArea sqlCommandArea;
    private JButton executeBtn;
    private JButton clearCommandBtn;
    private JTable resultTable;
    private JButton clearResultBtn;
    private JButton closeBtn;
    private JLabel connectionStatusLabel;
    private JLabel executionStatusLabel;

    private Connection connection;
    private static final String OPERATIONLOG_URL = "jdbc:mysql://localhost:3306/operationslog";

    public AccountantApp() {
        setTitle("ACCOUNTANT APPLICATION - (CNT 4714 - SPRING 2026 - PROJECT 3)");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(850, 600);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnectAndClose();
            }
        });
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Connection section - restricted to operationslog
        JPanel connPanel = new JPanel(new GridBagLayout());
        connPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Connection Details (operationslog only)", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        connPanel.add(new JLabel("Username (theaccountant):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        usernameField = new JTextField("theaccountant", 20);
        connPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        connPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        passwordField = new JPasswordField(20);
        connPanel.add(passwordField, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        connectBtn = new JButton("Connect to Database");
        connectBtn.setBackground(new Color(66, 133, 244));
        connectBtn.setForeground(Color.BLACK);
        connectBtn.addActionListener(e -> connect());
        connPanel.add(connectBtn, gbc);

        gbc.gridy = 1;
        disconnectBtn = new JButton("Disconnect From Database");
        disconnectBtn.setBackground(new Color(219, 68, 55));
        disconnectBtn.setForeground(Color.WHITE);
        disconnectBtn.setEnabled(false);
        disconnectBtn.addActionListener(e -> disconnect());
        connPanel.add(disconnectBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; gbc.insets = new Insets(8, 5, 2, 5);
        connPanel.add(new JLabel("CONNECTION STATUS"), gbc);
        gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        connectionStatusLabel = new JLabel("Not connected");
        connectionStatusLabel.setOpaque(true);
        connectionStatusLabel.setBackground(new Color(200, 230, 200));
        connectionStatusLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        connPanel.add(connectionStatusLabel, gbc);

        mainPanel.add(connPanel, BorderLayout.NORTH);

        // SQL command (read-only - SELECT only)
        JPanel sqlPanel = new JPanel(new BorderLayout(5, 5));
        sqlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "SQL Command Input (SELECT only)", TitledBorder.LEFT, TitledBorder.TOP));
        sqlCommandArea = new JTextArea(4, 50);
        sqlCommandArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        sqlCommandArea.setText("SELECT * FROM operationscount");
        JScrollPane sqlScroll = new JScrollPane(sqlCommandArea);
        sqlPanel.add(sqlScroll, BorderLayout.CENTER);

        JPanel sqlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        executeBtn = new JButton("Execute SQL Command");
        executeBtn.setBackground(new Color(52, 168, 83));
        executeBtn.setForeground(Color.WHITE);
        executeBtn.setEnabled(false);
        executeBtn.addActionListener(e -> executeSQL());
        sqlButtons.add(executeBtn);
        clearCommandBtn = new JButton("Clear SQL Command");
        clearCommandBtn.setBackground(new Color(251, 188, 5));
        clearCommandBtn.addActionListener(e -> sqlCommandArea.setText(""));
        sqlButtons.add(clearCommandBtn);
        sqlPanel.add(sqlButtons, BorderLayout.SOUTH);

        mainPanel.add(sqlPanel, BorderLayout.CENTER);

        // Results
        JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
        resultPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "SQL Execution Result Window", TitledBorder.LEFT, TitledBorder.TOP));
        resultTable = new JTable();
        resultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        executionStatusLabel = new JLabel(" ");
        resultPanel.add(executionStatusLabel, BorderLayout.NORTH);

        JPanel resultButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clearResultBtn = new JButton("Clear Result Window");
        clearResultBtn.setBackground(new Color(251, 188, 5));
        clearResultBtn.addActionListener(e -> resultTable.setModel(new javax.swing.table.DefaultTableModel()));
        resultButtons.add(clearResultBtn);
        closeBtn = new JButton("Close Application");
        closeBtn.setBackground(new Color(219, 68, 55));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> disconnectAndClose());
        resultButtons.add(closeBtn);
        resultPanel.add(resultButtons, BorderLayout.SOUTH);

        mainPanel.add(resultPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void connect() {
        String user = usernameField.getText().trim();
        char[] pass = passwordField.getPassword();
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username.");
            return;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(OPERATIONLOG_URL, user, new String(pass));
            connectionStatusLabel.setText(OPERATIONLOG_URL);
            connectionStatusLabel.setBackground(new Color(200, 255, 200));
            connectBtn.setEnabled(false);
            disconnectBtn.setEnabled(true);
            executeBtn.setEnabled(true);
            usernameField.setEnabled(false);
            passwordField.setEnabled(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage());
        }
    }

    private void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {}
            connection = null;
        }
        connectionStatusLabel.setText("Not connected");
        connectionStatusLabel.setBackground(new Color(200, 230, 200));
        connectBtn.setEnabled(true);
        disconnectBtn.setEnabled(false);
        executeBtn.setEnabled(false);
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
    }

    private void disconnectAndClose() {
        disconnect();
        dispose();
        System.exit(0);
    }

    private void executeSQL() {
        String sql = sqlCommandArea.getText().trim();
        if (sql.isEmpty()) {
            executionStatusLabel.setText("No command entered.");
            return;
        }
        // Enforce read-only access at the UI layer as a second line of defense.
        if (!isSelectOrWithQuery(sql)) {
            JOptionPane.showMessageDialog(this, "Accountant application allows SELECT only.");
            return;
        }
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "Not connected.");
            return;
        }
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetTableModel model = new ResultSetTableModel(rs);
            resultTable.setModel(model);
            stmt.close();
            executionStatusLabel.setText("Query executed successfully. Rows: " + model.getRowCount());
        } catch (SQLException ex) {
            executionStatusLabel.setText("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "SQL Error: " + ex.getMessage());
        }
    }

    private static boolean isSelectOrWithQuery(String sql) {
        if (sql == null) return false;
        String s = sql.trim();
        while (s.startsWith("(")) s = s.substring(1).trim();
        String u = s.toUpperCase();
        return u.startsWith("SELECT") || u.startsWith("WITH");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AccountantApp app = new AccountantApp();
            app.setVisible(true);
        });
    }
}
