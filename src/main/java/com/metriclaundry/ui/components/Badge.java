package com.metriclaundry.ui.components;

import com.metriclaundry.model.OrderStatus;
import com.metriclaundry.model.PaymentStatus;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/** Coloured pill (e.g. "In Progress", "Completed", "Paid"). Also usable as a table cell renderer. */
public class Badge extends JComponent implements TableCellRenderer {
    private String text = "";
    private Color bg = Theme.PRIMARY_LIGHT, fg = Theme.PRIMARY_DARK;
    private boolean selected;

    public Badge() { setFont(Theme.bold(11.5f)); }

    public Badge(Object value) { this(); set(value); }

    public void set(Object value) {
        if (value instanceof OrderStatus) {
            OrderStatus s = (OrderStatus) value;
            text = s.badgeText();
            switch (s) {
                case COMPLETED: bg = Theme.OK_BG; fg = Theme.OK_FG; break;
                case CANCELLED: bg = Theme.DANGER_BG; fg = Theme.DANGER; break;
                case READY: bg = Theme.INFO_BG; fg = Theme.INFO_FG; break;
                case PLACED: bg = new Color(0xECEFF1); fg = new Color(0x455A64); break;
                default: bg = Theme.WARN_BG; fg = Theme.WARN_FG;
            }
        } else if (value instanceof PaymentStatus) {
            boolean paid = value == PaymentStatus.PAID;
            text = ((PaymentStatus) value).label();
            bg = paid ? Theme.OK_BG : Theme.WARN_BG;
            fg = paid ? Theme.OK_FG : Theme.WARN_FG;
        } else if (value == null) {
            text = "Unpaid"; bg = Theme.DANGER_BG; fg = Theme.DANGER;
        } else {
            text = String.valueOf(value);
            boolean on = text.equalsIgnoreCase("Active");
            bg = on ? Theme.OK_BG : new Color(0xECEFF1);
            fg = on ? Theme.OK_FG : new Color(0x455A64);
        }
        repaint();
    }

    @Override public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(text) + 22, fm.getHeight() + 8);
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        if (selected) { g.setColor(Theme.PRIMARY_LIGHT); g.fillRect(0, 0, getWidth(), getHeight()); }
        FontMetrics fm = g.getFontMetrics(getFont());
        int w = fm.stringWidth(text) + 20, h = fm.getHeight() + 6;
        int x = getParent() instanceof JTable || getParent() instanceof CellRendererPane ? 12 : (getWidth() - w) / 2;
        int y = (getHeight() - h) / 2;
        g.setColor(bg);
        g.fillRoundRect(x, y, w, h, h, h);
        g.setColor(fg);
        g.setFont(getFont());
        g.drawString(text, x + 10, y + (h - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }

    @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
        set(v);
        selected = sel;
        return this;
    }
}
