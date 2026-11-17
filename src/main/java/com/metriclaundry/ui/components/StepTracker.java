package com.metriclaundry.ui.components;

import com.metriclaundry.model.OrderStatus;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Horizontal progress tracker: Order Placed -> Received -> Washing -> Drying -> Ready -> Completed. */
public class StepTracker extends JComponent {
    private static final String[] ICONS = {"orders", "check", "washer", "dry", "bag", "check"};
    private OrderStatus current;

    public StepTracker() { setPreferredSize(new Dimension(700, 120)); }

    public void setStatus(OrderStatus s) { current = s; repaint(); }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        List<OrderStatus> steps = OrderStatus.flow();
        int n = steps.size();
        int idx = current == null ? -1 : steps.indexOf(current);
        boolean cancelled = current == OrderStatus.CANCELLED;
        int pad = 50, r = 22, cy = 34;
        double gap = (getWidth() - 2.0 * pad) / (n - 1);

        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n - 1; i++) {
            int x1 = (int) (pad + i * gap), x2 = (int) (pad + (i + 1) * gap);
            g.setColor(i < idx ? Theme.PRIMARY : new Color(0xD5DDD5));
            g.drawLine(x1 + r, cy, x2 - r, cy);
        }
        for (int i = 0; i < n; i++) {
            int cx = (int) (pad + i * gap);
            boolean done = i < idx || (i == idx && current == OrderStatus.COMPLETED);
            boolean now = i == idx && !done;
            Color fill = cancelled ? new Color(0xC9D1C9) : done ? Theme.PRIMARY : now ? Theme.PRIMARY_LIGHT : new Color(0xC9D1C9);
            if (now) {
                g.setColor(new Color(30, 142, 62, 50));
                g.fillOval(cx - r - 6, cy - r - 6, (r + 6) * 2, (r + 6) * 2);
            }
            g.setColor(fill);
            g.fillOval(cx - r, cy - r, r * 2, r * 2);
            if (now) {
                g.setColor(Theme.PRIMARY);
                g.setStroke(new BasicStroke(3));
                g.drawOval(cx - r, cy - r, r * 2, r * 2);
            }
            Icon ic = Icons.of(done && i > 0 ? "check" : ICONS[i], 22, now ? Theme.PRIMARY_DARK : Color.WHITE, 2.4f);
            ic.paintIcon(this, g, cx - 11, cy - 11);

            String label = steps.get(i).label();
            g.setFont(now ? Theme.bold(14) : Theme.regular(14));
            g.setColor(now || done ? Theme.TEXT : Theme.MUTED);
            FontMetrics fm = g.getFontMetrics();
            String[] lines = label.equals("Ready for Pickup") ? new String[]{"Ready", "for Pickup"}
                    : label.equals("Order Placed") ? new String[]{"Order", "Placed"} : new String[]{label};
            int y = cy + r + 22;
            for (String ln : lines) {
                g.drawString(ln, cx - fm.stringWidth(ln) / 2, y);
                y += fm.getHeight() - 2;
            }
        }
        if (cancelled) {
            g.setFont(Theme.bold(13));
            g.setColor(Theme.DANGER);
            g.drawString("This order was cancelled.", pad, getHeight() - 4);
        }
        g.dispose();
    }
}
