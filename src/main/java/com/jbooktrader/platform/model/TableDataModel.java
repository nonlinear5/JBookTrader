package com.jbooktrader.platform.model;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Eugene Kononov
 */
public class TableDataModel extends AbstractTableModel {
    protected final List<Object> rows;
    private String[] schema;

    protected TableDataModel() {
        rows = new ArrayList<>();
    }

    public void addRow(Object[] item) {
        rows.add(item);
        SwingUtilities.invokeLater(this::fireTableDataChanged);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Object[] item = (Object[]) rows.get(row);
        item[col] = value;
        SwingUtilities.invokeLater(() -> fireTableRowsUpdated(row, row));
    }

    protected void updateRow(int row, AbstractMap<? extends Enum, Object> rowData) {
        Object[] changedItem = (Object[]) rows.get(row);
        for (Map.Entry<? extends Enum, Object> entry : rowData.entrySet()) {
            changedItem[entry.getKey().ordinal()] = entry.getValue();
        }
        SwingUtilities.invokeLater(() -> fireTableRowsUpdated(row, row));
    }

    protected void removeAllData() {
        rows.clear();
        SwingUtilities.invokeLater(this::fireTableDataChanged);
    }

    protected void setSchema(String[] schema) {
        this.schema = schema;
        fireTableStructureChanged();
    }

    public Object getValueAt(int row, int column) {
        Object[] item = (Object[]) rows.get(row);
        return item[column];
    }

    protected Object[] getRow(int row) {
        return (Object[]) rows.get(row);
    }

    @Override
    public String getColumnName(int index) {
        return schema[index];
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return (schema == null) ? 0 : schema.length;
    }
}
