package org.example;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
 * <p>
 * Implementation notes:
 * - "DB URL Properties" selects a properties file that supplies at least `jdbc.url` and optionally `jdbc.driver`.
 * - "User Properties" may supply credentials (`jdbc.username`/`jdbc.password`); otherwise the Username/Password
 *   fields are used.
 * - Successful queries/updates are logged asynchronously to the `operationslog` database using `applog.properties`
 *   (not selectable from the UI).
 */
public class SQLClientApp extends JFrame {
    // Fixed dropdown values per assignment requirements.
    private static final String[] DB_URL_PROPERTIES_OPTIONS = {
            "project3.properties",
            "bikedb.properties",
            "operationslog.properties"
    };
    private static final String[] USER_PROPERTIES_OPTIONS = {
            "root.properties",
            "client1.properties",
            "client2.properties"
    };

    private JComboBox<String> dbUrlPropsCombo;
    private JComboBox<String> userPropsCombo;
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
        dbUrlPropsCombo = new JComboBox<>(DB_URL_PROPERTIES_OPTIONS);
        dbUrlPropsCombo.setSelectedIndex(0);
        connPanel.add(dbUrlPropsCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        connPanel.add(new JLabel("User Properties:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        userPropsCombo = new JComboBox<>(USER_PROPERTIES_OPTIONS);
        userPropsCombo.setSelectedIndex(0);
        userPropsCombo.addActionListener(e -> maybePopulateCredentialsFromUserProps());
        connPanel.add(userPropsCombo, gbc);

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
        maybePopulateCredentialsFromUserProps();

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
        String propsFile = ((String) dbUrlPropsCombo.getSelectedItem());
        String userPropsFile = ((String) userPropsCombo.getSelectedItem());
        if (propsFile == null || propsFile.isBlank()) {
            JOptionPane.showMessageDialog(this, "Select a DB URL properties file.");
            return;
        }
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        try {
            Properties props = loadProperties(propsFile);
            String url = props.getProperty("jdbc.url");
            String driver = props.getProperty("jdbc.driver");
            if (driver != null) Class.forName(driver);

            // If a user properties file contains credentials, let it drive the connection.
            if (userPropsFile != null && !userPropsFile.isBlank()) {
                Properties userProps = loadProperties(userPropsFile);
                String fileUser = firstNonBlank(userProps.getProperty("jdbc.username"), userProps.getProperty("user"));
                String filePass = firstNonBlank(userProps.getProperty("jdbc.password"), userProps.getProperty("password"));
                if (fileUser != null) user = fileUser.trim();
                if (filePass != null) pass = filePass;
            }

            if (user.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter username (or select a user properties file that provides it).");
                return;
            }

            connection = DriverManager.getConnection(url, user, pass);
            currentUsername = user;
            connectionStatusLabel.setText(url);
            connectionStatusLabel.setBackground(new Color(200, 255, 200));
            connectBtn.setEnabled(false);
            disconnectBtn.setEnabled(true);
            executeBtn.setEnabled(true);
            dbUrlPropsCombo.setEnabled(false);
            userPropsCombo.setEnabled(false);
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
        dbUrlPropsCombo.setEnabled(true);
        userPropsCombo.setEnabled(true);
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
        String sql = sqlCommandArea.getText();
        if (sql != null) sql = sql.trim();
        if (sql.isEmpty()) {
            executionStatusLabel.setText("No command entered.");
            return;
        }
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "Not connected.");
            return;
        }
        try {
            List<String> statements = splitSqlStatements(sql);
            if (statements.isEmpty()) {
                executionStatusLabel.setText("No command entered.");
                return;
            }

            int queries = 0;
            int updates = 0;
            int totalRowsAffected = 0;
            Integer lastQueryRows = null;
            Integer lastUpdateCount = null;

            // Execute sequentially so multi-statement inputs like "SELECT; INSERT; SELECT;" work.
            for (String s : statements) {
                try (Statement stmt = connection.createStatement()) {
                    boolean hasResultSet = stmt.execute(s);
                    if (hasResultSet) {
                        ResultSet rs = stmt.getResultSet();
                        ResultSetTableModel model = new ResultSetTableModel(rs);
                        resultTable.setModel(model);
                        queries++;
                        lastQueryRows = model.getRowCount();
                        lastUpdateCount = null;
                        logOperation(true, false);
                    } else {
                        int count = stmt.getUpdateCount();
                        updates++;
                        totalRowsAffected += Math.max(count, 0);
                        lastUpdateCount = count;
                        lastQueryRows = null;
                        logOperation(false, true);
                    }
                }
            }

            if (lastQueryRows != null) {
                executionStatusLabel.setText("Executed " + statements.size() + " statement(s). Queries: " + queries + ", Updates: " + updates + ". Last query rows: " + lastQueryRows);
            } else if (lastUpdateCount != null) {
                executionStatusLabel.setText("Executed " + statements.size() + " statement(s). Queries: " + queries + ", Updates: " + updates + ". Total rows affected: " + totalRowsAffected);
            } else {
                executionStatusLabel.setText("Executed " + statements.size() + " statement(s). Queries: " + queries + ", Updates: " + updates + ".");
            }
        } catch (SQLException ex) {
            executionStatusLabel.setText("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "SQL Error: " + ex.getMessage());
        }
    }

    /**
     * Splits a SQL text block into individual statements (semicolon-delimited) while attempting to ignore semicolons
     * inside quoted strings, identifier quotes, and comments.
     * <p>
     * Supported comment styles: `-- ...`, `# ...`, and `/* ... *\/`.
     * This is not a full SQL parser, but it is sufficient for common multi-statement inputs in this course.
     */
    private static List<String> splitSqlStatements(String sqlText) {
        List<String> out = new ArrayList<>();
        if (sqlText == null) return out;

        StringBuilder cur = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        boolean inBacktick = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean escaped = false;

        for (int i = 0; i < sqlText.length(); i++) {
            char c = sqlText.charAt(i);
            char next = (i + 1 < sqlText.length()) ? sqlText.charAt(i + 1) : '\0';

            if (inLineComment) {
                cur.append(c);
                if (c == '\n') inLineComment = false;
                continue;
            }
            if (inBlockComment) {
                cur.append(c);
                if (c == '*' && next == '/') {
                    cur.append(next);
                    i++;
                    inBlockComment = false;
                }
                continue;
            }

            if (!inSingle && !inDouble && !inBacktick) {
                if (c == '-' && next == '-') {
                    char after = (i + 2 < sqlText.length()) ? sqlText.charAt(i + 2) : '\0';
                    if (after == ' ' || after == '\t' || after == '\r' || after == '\n' || after == '\0') {
                        inLineComment = true;
                        cur.append(c).append(next);
                        i++;
                        continue;
                    }
                }
                if (c == '#') {
                    inLineComment = true;
                    cur.append(c);
                    continue;
                }
                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    cur.append(c).append(next);
                    i++;
                    continue;
                }
            }

            if (escaped) {
                cur.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\' && (inSingle || inDouble)) {
                cur.append(c);
                escaped = true;
                continue;
            }

            if (!inDouble && !inBacktick && c == '\'') {
                inSingle = !inSingle;
                cur.append(c);
                continue;
            }
            if (!inSingle && !inBacktick && c == '"') {
                inDouble = !inDouble;
                cur.append(c);
                continue;
            }
            if (!inSingle && !inDouble && c == '`') {
                inBacktick = !inBacktick;
                cur.append(c);
                continue;
            }

            if (!inSingle && !inDouble && !inBacktick && c == ';') {
                String stmt = cur.toString().trim();
                if (!stmt.isEmpty()) out.add(stmt);
                cur.setLength(0);
                continue;
            }

            cur.append(c);
        }

        String tail = cur.toString().trim();
        if (!tail.isEmpty()) out.add(tail);
        return out;
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

    /**
     * Loads a properties file either from the application classpath (preferred) or from a filesystem path.
     * This supports running in IDEs (resources on classpath) and also ad-hoc external properties files.
     */
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

    /**
     * Loads a properties file strictly from the classpath. Used for hidden app-level configuration.
     */
    private Properties loadPropertiesFromResource(String name) throws java.io.IOException {
        Properties p = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/" + name)) {
            if (is == null) throw new java.io.IOException("Not found: " + name);
            p.load(is);
        }
        return p;
    }

    private void maybePopulateCredentialsFromUserProps() {
        if (usernameField == null || passwordField == null || userPropsCombo == null) return;
        String userPropsFile = (String) userPropsCombo.getSelectedItem();
        if (userPropsFile == null || userPropsFile.isBlank()) return;
        try {
            Properties userProps = loadProperties(userPropsFile);
            String fileUser = firstNonBlank(userProps.getProperty("jdbc.username"), userProps.getProperty("user"));
            String filePass = firstNonBlank(userProps.getProperty("jdbc.password"), userProps.getProperty("password"));
            if (fileUser != null && !fileUser.isBlank()) usernameField.setText(fileUser.trim());
            if (filePass != null) passwordField.setText(filePass);
        } catch (Exception ignored) {
            // Ignore invalid properties file selection; connection will surface errors if needed.
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SQLClientApp app = new SQLClientApp();
            app.setVisible(true);
        });
    }
}
