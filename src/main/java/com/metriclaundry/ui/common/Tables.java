package com.metriclaundry.ui.common;

import com.metriclaundry.ui.components.Badge;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Small helpers for read-only, styled tables. */
public final class Tables {
    private Tables() { }

    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        Theme.styleTable(t);
        return t;
    }

    public static void badgeColumn(JTable t, int col) { t.getColumnModel().getColumn(col).setCellRenderer(new Badge()); }

    public static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) {
            if (w[i] > 0) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
    }

    /** Hides a column (used for the id column) but keeps it in the model. */
    public static void hide(JTable t, int col) {
        t.removeColumn(t.getColumnModel().getColumn(col));
    }
}
