package org.example;

import javax.swing.table.AbstractTableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Name: [Your Name]
 * Course: CNT 4714 - Spring 2026
 * Assignment: Project 3
 * Date: March 2026
 * <p>
 * Table model for displaying ResultSet data in a JTable.
 * Based on course materials DisplayQueryResults/ResultSetTableModel pattern.
 * <p>
 * This model materializes the entire {@link ResultSet} into memory so it can be displayed after the JDBC
 * statement/connection has moved on to subsequent commands.
 */
public class ResultSetTableModel extends AbstractTableModel {
    private final List<Object[]> rows = new ArrayList<>();
    private final String[] columnNames;
    private final int columnCount;

    public ResultSetTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        columnCount = meta.getColumnCount();
        columnNames = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = meta.getColumnName(i + 1);
        }
        // Materialize the ResultSet so the table model is independent of the JDBC cursor.
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getObject(i + 1);
            }
            rows.add(row);
        }
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex)[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
}
