package com.metriclaundry.ui.components;

import com.metriclaundry.ui.theme.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/** Painted login illustration, available as a NetBeans designer bean. */
public class LoginBrandPanel extends JPanel {

        public LoginBrandPanel() {
            super(new GridBagLayout());
            Logo logo = new Logo(230, false);
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0; add(logo, c);
            JLabel tag = Theme.label("Clean Clothes, Happy You!", Font.BOLD | Font.ITALIC, 20, Theme.PRIMARY_DARK);
            c.gridy = 1; c.insets = new Insets(10, 0, 0, 0); add(tag, c);
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(0xF4FBF4), 0, h, new Color(0xDCF0DE)));
            g.fillRect(0, 0, w, h);
            // rolling green hills at the bottom
            Path2D hill = new Path2D.Double();
            hill.moveTo(0, h);
            hill.lineTo(0, h - 90);
            hill.curveTo(w * 0.3, h - 150, w * 0.6, h - 40, w, h - 110);
            hill.lineTo(w, h);
            hill.closePath();
            g.setColor(new Color(0x8BCB8F));
            g.fill(hill);
            Path2D hill2 = new Path2D.Double();
            hill2.moveTo(0, h);
            hill2.lineTo(0, h - 40);
            hill2.curveTo(w * 0.4, h - 100, w * 0.7, h - 10, w, h - 60);
            hill2.lineTo(w, h);
            hill2.closePath();
            g.setColor(Theme.PRIMARY);
            g.fill(hill2);
            g.setColor(new Color(124, 197, 118, 120));
            for (int i = 0; i < 6; i++) g.fill(leafShape(20 + i * 18, 40 + i * 10, 34, 20 + i * 18));
            for (int i = 0; i < 5; i++) g.fill(leafShape(w - 30 - i * 16, 30 + i * 14, 30, 150 + i * 12));
            g.dispose();
        }
        private static Shape leafShape(double x, double y, double len, double ang) {
            Path2D p = new Path2D.Double();
            p.moveTo(0, 0);
            p.curveTo(len * 0.3, -len * 0.4, len * 0.8, -len * 0.35, len, 0);
            p.curveTo(len * 0.8, len * 0.35, len * 0.3, len * 0.4, 0, 0);
            java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform();
            at.translate(x, y);
            at.rotate(Math.toRadians(ang));
            return at.createTransformedShape(p);
        }
    
}
