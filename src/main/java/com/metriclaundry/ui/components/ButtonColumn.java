package com.metriclaundry.ui.components;

import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;

/** Renders a small green "View Details"-style pill in a table column and runs an action on click. */
public final class ButtonColumn {
    private ButtonColumn() { }

    /** @param action receives the MODEL row index that was clicked. */
    public static void install(JTable table, int column, String text, IntConsumer action) {
        table.getColumnModel().getColumn(column).setCellRenderer(new Pill(text));
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint()), col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0 && table.convertColumnIndexToModel(col) == column)
                    action.accept(table.convertRowIndexToModel(row));
            }
        });
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                boolean on = col >= 0 && table.convertColumnIndexToModel(col) == column && table.rowAtPoint(e.getPoint()) >= 0;
                table.setCursor(Cursor.getPredefinedCursor(on ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private static final class Pill extends JComponent implements TableCellRenderer {
        private final String text;
        private boolean sel;
        Pill(String text) { this.text = text; setFont(Theme.bold(11.5f)); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            sel = s; return this;
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            if (sel) { g.setColor(Theme.PRIMARY_LIGHT); g.fillRect(0, 0, getWidth(), getHeight()); }
            FontMetrics fm = g.getFontMetrics(getFont());
            int w = fm.stringWidth(text) + 22, h = 26, x = 10, y = (getHeight() - h) / 2;
            g.setColor(Theme.PRIMARY);
            g.fillRoundRect(x, y, w, h, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(getFont());
            g.drawString(text, x + 11, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}
