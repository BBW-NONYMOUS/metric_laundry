package com.metriclaundry.ui.components;

import com.metriclaundry.model.OrderStatus;
import com.metriclaundry.model.StatusHistory;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Vertical "Track Your Laundry" list with times taken from the order history. */
public class Timeline extends JComponent {
    private OrderStatus current;
    private List<StatusHistory> history = List.of();

    public Timeline() { setPreferredSize(new Dimension(220, 6 * 34 + 6)); }

    public void set(OrderStatus status, List<StatusHistory> history) {
        this.current = status;
        this.history = history == null ? List.of() : history;
        repaint();
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        List<OrderStatus> steps = OrderStatus.flow();
        int idx = current == null ? -1 : steps.indexOf(current);
        int rowH = 34, x = 18;
        for (int i = 0; i < steps.size(); i++) {
            int y = 4 + i * rowH;
            boolean done = i < idx || (i == idx && current == OrderStatus.COMPLETED);
            boolean now = i == idx && !done;
            if (now) {
                g.setColor(Theme.PRIMARY_LIGHT);
                g.fillRoundRect(0, y - 3, getWidth(), rowH - 2, 10, 10);
            }
            if (i < steps.size() - 1) {
                g.setColor(i < idx ? Theme.PRIMARY : new Color(0xD5DDD5));
                g.setStroke(new BasicStroke(2));
                g.drawLine(x, y + 22, x, y + rowH + 2);
            }
            g.setColor(done ? Theme.PRIMARY : now ? Theme.PRIMARY_DARK : new Color(0xC9D1C9));
            g.fillOval(x - 10, y + 1, 20, 20);
            Icons.of(now ? "washer" : "check", 12, Color.WHITE, 2.6f).paintIcon(this, g, x - 6, y + 5);

            g.setColor(done || now ? Theme.TEXT : Theme.MUTED);
            g.setFont(done || now ? Theme.bold(12.5f) : Theme.regular(12.5f));
            String label = steps.get(i).label() + (now ? "  - In Progress" : "");
            g.drawString(label, x + 20, y + 11);
            StatusHistory h = find(steps.get(i));
            if (h != null) {
                g.setFont(Theme.regular(11));
                g.setColor(Theme.MUTED);
                g.drawString(Formats.dateTime(h.getChangedAt()), x + 20, y + 25);
            }
        }
        g.dispose();
    }

    private StatusHistory find(OrderStatus s) {
        StatusHistory found = null;
        for (StatusHistory h : history) if (h.getStatus() == s) found = h;
        return found;
    }
}
