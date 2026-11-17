package com.metriclaundry.ui.components;

import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/** The METRIC LAUNDRY logo: hanger + leaf + word-mark, painted in Java2D. */
public class Logo extends JComponent {
    /** No-argument constructor for the NetBeans GUI designer. */
    public Logo() { this(130, false); }
    private final int scale;          // overall size (width of the mark)
    private final boolean onDark;
    private String subtitle;

    public Logo(int scale, boolean onDark) {
        this.scale = scale;
        this.onDark = onDark;
        int textH = Math.round(scale * 0.56f);
        setPreferredSize(new Dimension(Math.round(scale * 1.25f), Math.round(scale * 0.62f) + textH));
    }

    public Logo subtitle(String s) {
        this.subtitle = s;
        Dimension d = getPreferredSize();
        setPreferredSize(new Dimension(d.width, d.height + Math.round(scale * 0.2f)));
        return this;
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        Color ink = onDark ? Color.WHITE : Theme.PRIMARY_DEEP;
        int cx = getWidth() / 2;
        double s = scale / 24.0 * 0.62;
        double markW = 24 * s;

        // hanger
        Graphics2D h = (Graphics2D) g.create();
        h.translate(cx - markW / 2, 2);
        h.scale(s, s);
        h.setColor(ink);
        h.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D hook = new Path2D.Double();
        hook.moveTo(9.6, 5.2);
        hook.curveTo(9.6, 3.2, 11, 2.2, 12.2, 2.2);
        hook.curveTo(13.6, 2.2, 14.6, 3.2, 14.6, 4.5);
        hook.curveTo(14.6, 6, 12, 6.3, 12, 8);
        h.draw(hook);
        Path2D body = new Path2D.Double();
        body.moveTo(12, 8); body.lineTo(2, 16.5); body.lineTo(22, 16.5); body.closePath();
        h.draw(body);
        // leaves
        h.setColor(onDark ? Theme.LEAF : Theme.ACCENT);
        h.fill(leaf(18.2, 6.5, 4.2, -35));
        h.setColor(onDark ? new Color(0xA5DCA0) : Theme.LEAF);
        h.fill(leaf(20.6, 9.2, 3.0, 15));
        h.dispose();

        // word-mark
        int top = (int) (17.5 * s) + 6;
        float fs = scale * 0.235f;
        g.setColor(ink);
        g.setFont(Theme.font(Font.BOLD, fs));
        FontMetrics fm = g.getFontMetrics();
        int y1 = top + fm.getAscent();
        g.drawString("METRIC", cx - fm.stringWidth("METRIC") / 2, y1);
        int y2 = y1 + fm.getAscent() - 1;
        g.drawString("LAUNDRY", cx - fm.stringWidth("LAUNDRY") / 2, y2);
        if (subtitle != null) {
            g.setFont(Theme.regular(scale * 0.12f));
            FontMetrics f2 = g.getFontMetrics();
            g.setColor(onDark ? new Color(0xD7EED9) : Theme.MUTED);
            g.drawString(subtitle, cx - f2.stringWidth(subtitle) / 2, y2 + f2.getAscent() + 6);
        }
        g.dispose();
    }

    private static Shape leaf(double x, double y, double len, double angleDeg) {
        Path2D p = new Path2D.Double();
        p.moveTo(0, 0);
        p.curveTo(len * 0.3, -len * 0.45, len * 0.8, -len * 0.4, len, 0);
        p.curveTo(len * 0.8, len * 0.4, len * 0.3, len * 0.45, 0, 0);
        java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform();
        at.translate(x - len / 2, y);
        at.rotate(Math.toRadians(angleDeg), len / 2, 0);
        return at.createTransformedShape(p);
    }
}
