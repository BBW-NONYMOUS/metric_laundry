package com.metriclaundry.ui.components;

import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/** Small area/line chart for the "Sales Overview" panel. */
public class LineChart extends JComponent {
    private List<String> labels = new ArrayList<>();
    private List<Double> values = new ArrayList<>();
    private int hover = -1;

    public LineChart() {
        setPreferredSize(new Dimension(420, 220));
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int n = values.size();
                if (n < 2) return;
                double gap = (getWidth() - 60.0) / (n - 1);
                int i = (int) Math.round((e.getX() - 46) / gap);
                hover = i >= 0 && i < n ? i : -1;
                repaint();
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseExited(java.awt.event.MouseEvent e) { hover = -1; repaint(); }
        });
    }

    public void setData(List<String> labels, List<Double> values) {
        this.labels = labels;
        this.values = values;
        repaint();
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        int n = values.size();
        int left = 46, right = 14, top = 12, bottom = 28;
        int w = getWidth() - left - right, h = getHeight() - top - bottom;
        double max = 0;
        for (double v : values) max = Math.max(max, v);
        max = niceMax(max);

        g.setFont(Theme.regular(11));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i <= 4; i++) {
            int y = top + h - h * i / 4;
            g.setColor(new Color(0xEEF2EE));
            g.drawLine(left, y, left + w, y);
            g.setColor(Theme.MUTED);
            String t = String.format("%,.0f", max * i / 4);
            g.drawString(t, left - 8 - fm.stringWidth(t), y + 4);
        }
        if (n < 2) { g.dispose(); return; }

        double gap = (double) w / (n - 1);
        Path2D line = new Path2D.Double(), area = new Path2D.Double();
        for (int i = 0; i < n; i++) {
            double x = left + i * gap, y = top + h - (values.get(i) / max) * h;
            if (i == 0) { line.moveTo(x, y); area.moveTo(x, top + h); area.lineTo(x, y); }
            else { line.lineTo(x, y); area.lineTo(x, y); }
        }
        area.lineTo(left + w, top + h);
        area.closePath();
        g.setPaint(new GradientPaint(0, top, new Color(76, 175, 80, 90), 0, top + h, new Color(76, 175, 80, 8)));
        g.fill(area);
        g.setColor(Theme.ACCENT);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(line);
        for (int i = 0; i < n; i++) {
            int x = (int) (left + i * gap), y = (int) (top + h - (values.get(i) / max) * h);
            g.setColor(Color.WHITE);
            g.fillOval(x - 4, y - 4, 8, 8);
            g.setColor(Theme.PRIMARY);
            g.setStroke(new BasicStroke(2));
            g.drawOval(x - 4, y - 4, 8, 8);
            g.setColor(Theme.MUTED);
            String lb = labels.get(i);
            g.drawString(lb, x - fm.stringWidth(lb) / 2, top + h + 18);
        }
        if (hover >= 0) {
            int x = (int) (left + hover * gap), y = (int) (top + h - (values.get(hover) / max) * h);
            String t = Formats.PESO + String.format("%,.2f", values.get(hover));
            g.setFont(Theme.bold(11.5f));
            FontMetrics f2 = g.getFontMetrics();
            int bw = f2.stringWidth(t) + 14, bx = Math.min(Math.max(x - bw / 2, 0), getWidth() - bw), by = Math.max(y - 30, 0);
            g.setColor(Theme.PRIMARY_DEEP);
            g.fillRoundRect(bx, by, bw, 22, 8, 8);
            g.setColor(Color.WHITE);
            g.drawString(t, bx + 7, by + 15);
        }
        g.dispose();
    }

    private static double niceMax(double v) {
        if (v <= 0) return 100;
        double mag = Math.pow(10, Math.floor(Math.log10(v)));
        for (double m : new double[]{1, 2, 2.5, 5, 10}) if (m * mag >= v * 1.1) return m * mag;
        return 10 * mag;
    }
}
