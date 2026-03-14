package org.example;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Name: [Your Name]
 * Course: CNT 4714 - Spring 2026
 * Assignment: Project 3
 * Date: March 2026
 * <p>
 * Main SQL Client Application - two-tier JDBC GUI.
 */
public class SQLClientApp extends JFrame {
    private JTextField dbUrlPropsField;
    private JTextField userPropsField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton connectBtn;
    private JButton disconnectBtn;
    private JTextArea sqlCommandArea;
    private JButton executeBtn;
    private JButton clearCommandBtn;
    private JTable resultTable;
    private JScrollPane resultScrollPane;
    private JButton clearResultBtn;
    private JButton closeBtn;
    private JLabel connectionStatusLabel;
    private JLabel executionStatusLabel;

    private Connection connection;
    private String currentUsername;
    // Background logger to avoid blocking the UI thread on operations log updates.
    private final ExecutorService logExecutor = Executors.newSingleThreadExecutor();

    public SQLClientApp() {
        setTitle("SQL CLIENT APPLICATION - (CNT 4714 - SPRING 2026 - PROJECT 3)");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 700);
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

        // ---- Connection Details (Top) ----
        JPanel connPanel = new JPanel(new GridBagLayout());
        connPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Connection Details", TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        connPanel.add(new JLabel("DB URL Properties:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        dbUrlPropsField = new JTextField("project3.properties", 25);
        connPanel.add(dbUrlPropsField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        connPanel.add(new JLabel("User Properties:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        userPropsField = new JTextField("root.properties", 25);
        connPanel.add(userPropsField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        connPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        usernameField = new JTextField("root", 20);
        connPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        connPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        passwordField = new JPasswordField(20);
        connPanel.add(passwordField, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 2; gbc.weightx = 0;
        connectBtn = new JButton("Connect to Database");
        connectBtn.setBackground(new Color(66, 133, 244));
        connectBtn.setForeground(Color.BLACK);
        connectBtn.addActionListener(e -> connect());
        connPanel.add(connectBtn, gbc);

        gbc.gridy = 2; gbc.gridheight = 1;
        disconnectBtn = new JButton("Disconnect From Database");
        disconnectBtn.setBackground(new Color(219, 68, 55));
        disconnectBtn.setForeground(Color.WHITE);
        disconnectBtn.setEnabled(false);
        disconnectBtn.addActionListener(e -> disconnect());
        connPanel.add(disconnectBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3; gbc.gridheight = 1; gbc.insets = new Insets(8, 5, 2, 5);
        connPanel.add(new JLabel("CONNECTION STATUS"), gbc);
        gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        connectionStatusLabel = new JLabel("Not connected");
        connectionStatusLabel.setOpaque(true);
        connectionStatusLabel.setBackground(new Color(200, 230, 200));
        connectionStatusLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        connPanel.add(connectionStatusLabel, gbc);

        mainPanel.add(connPanel, BorderLayout.NORTH);

        // ---- SQL Command Input (Center) ----
        JPanel sqlPanel = new JPanel(new BorderLayout(5, 5));
        sqlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "SQL Command Input Window", TitledBorder.LEFT, TitledBorder.TOP));
        sqlCommandArea = new JTextArea(6, 50);
        sqlCommandArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
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

        // ---- Results (Bottom) ----
        JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
        resultPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "SQL Execution Result Window", TitledBorder.LEFT, TitledBorder.TOP));
        resultTable = new JTable();
        resultScrollPane = new JScrollPane(resultTable);
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);

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

        executionStatusLabel = new JLabel(" ");
        resultPanel.add(executionStatusLabel, BorderLayout.NORTH);

        mainPanel.add(resultPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void connect() {
        String propsFile = dbUrlPropsField.getText().trim();
        String user = usernameField.getText().trim();
        char[] pass = passwordField.getPassword();
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username.");
            return;
        }
        try {
            Properties props = loadProperties(propsFile);
            String url = props.getProperty("jdbc.url");
            String driver = props.getProperty("jdbc.driver");
            if (driver != null) Class.forName(driver);
            connection = DriverManager.getConnection(url, user, new String(pass));
            currentUsername = user;
            connectionStatusLabel.setText(url);
            connectionStatusLabel.setBackground(new Color(200, 255, 200));
            connectBtn.setEnabled(false);
            disconnectBtn.setEnabled(true);
            executeBtn.setEnabled(true);
            dbUrlPropsField.setEnabled(false);
            userPropsField.setEnabled(false);
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
        dbUrlPropsField.setEnabled(true);
        userPropsField.setEnabled(true);
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
    }

    private void disconnectAndClose() {
        logExecutor.shutdown();
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
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "Not connected.");
            return;
        }
        try {
            // Treat leading SELECT as a query; everything else uses executeUpdate.
            boolean isSelect = sql.toUpperCase().trim().startsWith("SELECT");
            if (isSelect) {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                ResultSetTableModel model = new ResultSetTableModel(rs);
                resultTable.setModel(model);
                stmt.close();
                logOperation(true, false);
                executionStatusLabel.setText("Query executed successfully. Rows: " + model.getRowCount());
            } else {
                Statement stmt = connection.createStatement();
                int count = stmt.executeUpdate(sql);
                stmt.close();
                logOperation(false, true);
                executionStatusLabel.setText("Update successful. Rows affected: " + count);
            }
        } catch (SQLException ex) {
            executionStatusLabel.setText("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "SQL Error: " + ex.getMessage());
        }
    }

    private void logOperation(boolean isQuery, boolean isUpdate) {
        if (currentUsername == null) return;
        logExecutor.submit(() -> {
            try {
                // Use the hidden app-level properties to connect to operationslog.
                Properties logProps = loadPropertiesFromResource("applog.properties");
                String url = logProps.getProperty("jdbc.url");
                String user = logProps.getProperty("jdbc.username");
                String pass = logProps.getProperty("jdbc.password");
                String driver = logProps.getProperty("jdbc.driver");
                if (driver != null) Class.forName(driver);
                try (Connection logConn = DriverManager.getConnection(url, user, pass)) {
                    ensureLogRow(logConn, currentUsername);
                    if (isQuery) {
                        PreparedStatement ups = logConn.prepareStatement("UPDATE operationscount SET num_queries = num_queries + 1 WHERE login_username = ?");
                        ups.setString(1, currentUsername);
                        ups.executeUpdate();
                        ups.close();
                    }
                    if (isUpdate) {
                        PreparedStatement ups = logConn.prepareStatement("UPDATE operationscount SET num_updates = num_updates + 1 WHERE login_username = ?");
                        ups.setString(1, currentUsername);
                        ups.executeUpdate();
                        ups.close();
                    }
                }
            } catch (Exception e) {
                System.err.println("Logging failed: " + e.getMessage());
            }
        });
    }

    private void ensureLogRow(Connection logConn, String username) throws SQLException {
        PreparedStatement ps = logConn.prepareStatement(
                "INSERT INTO operationscount (login_username, num_queries, num_updates) VALUES (?, 0, 0) ON DUPLICATE KEY UPDATE login_username = login_username");
        ps.setString(1, username);
        ps.executeUpdate();
        ps.close();
    }

    private Properties loadProperties(String name) throws java.io.IOException {
        Properties p = new Properties();
        // Try classpath first so bundled resources work, then fall back to a file path.
        try (InputStream is = getClass().getResourceAsStream("/" + name)) {
            if (is != null) {
                p.load(is);
                return p;
            }
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is != null) {
                p.load(is);
                return p;
            }
        }
        try (InputStream is = new java.io.FileInputStream(name)) {
            p.load(is);
            return p;
        } catch (java.io.FileNotFoundException e) {
            throw new java.io.IOException("Properties file not found: " + name);
        }
    }

    private Properties loadPropertiesFromResource(String name) throws java.io.IOException {
        Properties p = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/" + name)) {
            if (is == null) throw new java.io.IOException("Not found: " + name);
            p.load(is);
        }
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SQLClientApp app = new SQLClientApp();
            app.setVisible(true);
        });
    }
}
