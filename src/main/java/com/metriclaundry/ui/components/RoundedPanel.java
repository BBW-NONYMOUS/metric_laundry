package com.metriclaundry.ui.components;

import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** White card with rounded corners, a soft shadow and an optional border. */
public class RoundedPanel extends JPanel {
    private final int radius;
    private Color fill;
    private Color borderColor = Theme.BORDER;
    private boolean shadow = true;

    public RoundedPanel() { this(Theme.RADIUS, Theme.CARD); }

    public RoundedPanel(int radius, Color fill) {
        this.radius = radius;
        this.fill = fill;
        setOpaque(false);
        setBorder(Theme.pad(14, 16, 14, 16));
    }

    public RoundedPanel(LayoutManager layout) {
        this();
        setLayout(layout);
    }

    public RoundedPanel shadow(boolean s) { this.shadow = s; repaint(); return this; }
    public RoundedPanel border(Color c) { this.borderColor = c; repaint(); return this; }
    public void setFill(Color c) { this.fill = c; repaint(); }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        int w = getWidth(), h = getHeight();
        int s = shadow ? 3 : 0;
        if (shadow) {
            for (int i = 3; i >= 1; i--) {
                g.setColor(new Color(20, 60, 30, 7 * (4 - i)));
                g.fill(new RoundRectangle2D.Double(i - 1, i, w - 2 * (i - 1) - 1, h - i - 1, radius + i, radius + i));
            }
        }
        g.setColor(fill);
        g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1 - s, radius, radius));
        if (borderColor != null) {
            g.setColor(borderColor);
            g.draw(new RoundRectangle2D.Double(0, 0, w - 1, h - 1 - s, radius, radius));
        }
        g.dispose();
    }

    @Override public Insets getInsets() {
        Insets i = super.getInsets();
        return shadow ? new Insets(i.top, i.left, i.bottom + 3, i.right) : i;
    }
}
