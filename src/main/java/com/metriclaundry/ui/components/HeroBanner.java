package com.metriclaundry.ui.components;

import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/** Green gradient banner with title, tagline and a painted laundry-basket illustration. */
public class HeroBanner extends JComponent {
    /** No-argument constructor for the NetBeans GUI designer. */
    public HeroBanner() { this("Welcome to", "Metric Laundry!", "Clean Clothes, Happy You!"); }
    private final String line1, line2, tagline;

    public HeroBanner(String line1, String line2, String tagline) {
        this.line1 = line1; this.line2 = line2; this.tagline = tagline;
        setPreferredSize(new Dimension(700, 130));
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        int w = getWidth(), h = getHeight();
        g.setPaint(new GradientPaint(0, 0, new Color(0xF1FAF1), w, 0, new Color(0xD7EFD9)));
        g.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);

        // decorative leaves
        g.setColor(new Color(124, 197, 118, 90));
        for (int i = 0; i < 5; i++) g.fill(leaf(18 + i * 22, h - 10 - (i % 2) * 14, 28, -60 + i * 25));
        g.setColor(new Color(76, 175, 80, 70));
        g.fill(leaf(w - 40, 18, 36, 140));
        g.fill(leaf(w - 70, 8, 30, 170));

        // basket
        int bx = w - 230, by = h - 62;
        g.setColor(new Color(0xC79A5B));
        g.fillRoundRect(bx, by, 150, 50, 14, 14);
        g.setColor(new Color(0xA77B3F));
        g.setStroke(new BasicStroke(2));
        for (int i = 1; i < 9; i++) g.drawLine(bx + i * 16, by + 4, bx + i * 16, by + 46);
        g.drawLine(bx + 4, by + 18, bx + 146, by + 18);
        g.drawLine(bx + 4, by + 32, bx + 146, by + 32);
        // folded towels
        Color[] towels = {new Color(0x4F8FE0), new Color(0x7FB3F0), new Color(0x3C78C8), new Color(0xA9CBF5)};
        for (int i = 0; i < 4; i++) {
            g.setColor(towels[i]);
            g.fillRoundRect(bx + 12 + (i % 2) * 6, by - 16 - i * 14, 126, 16, 12, 12);
            g.setColor(new Color(255, 255, 255, 70));
            g.drawLine(bx + 20, by - 9 - i * 14, bx + 128, by - 9 - i * 14);
        }

        g.setColor(Theme.PRIMARY_DEEP);
        g.setFont(Theme.bold(30));
        FontMetrics fm = g.getFontMetrics();
        int tx = 32;
        g.drawString(line1, tx, 44);
        g.setFont(Theme.bold(36));
        g.drawString(line2, tx, 44 + g.getFontMetrics().getAscent());
        g.setFont(Theme.font(Font.BOLD | Font.ITALIC, 17));
        g.setColor(Theme.PRIMARY_DARK);
        g.drawString(tagline, tx, h - 16);
        Icons.of("droplet", 14, new Color(0x4F8FE0)).paintIcon(this, g, tx + g.getFontMetrics().stringWidth(tagline) + 8, h - 30);
        g.dispose();
    }

    static Shape leaf(double x, double y, double len, double angleDeg) {
        Path2D p = new Path2D.Double();
        p.moveTo(0, 0);
        p.curveTo(len * 0.3, -len * 0.4, len * 0.8, -len * 0.35, len, 0);
        p.curveTo(len * 0.8, len * 0.35, len * 0.3, len * 0.4, 0, 0);
        java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform();
        at.translate(x, y);
        at.rotate(Math.toRadians(angleDeg));
        return at.createTransformedShape(p);
    }
}
