package com.metriclaundry.ui.components;

import com.metriclaundry.model.LaundryService;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/** Picks an illustration/icon and tint for a service based on its name. */
public final class ServiceVisuals {
    private ServiceVisuals() { }

    public static String icon(LaundryService s) {
        String n = s.getName().toLowerCase();
        if (n.contains("dry")) return "shirt";
        if (n.contains("fold")) return "stack";
        if (n.contains("wash")) return "washer";
        if (n.contains("iron") || n.contains("press")) return "shirt";
        return "droplet";
    }

    public static Color tint(LaundryService s) {
        switch (icon(s)) {
            case "washer": return new Color(0xE3F1FC);
            case "shirt": return new Color(0xE8EEFB);
            case "stack": return new Color(0xE6F4E8);
            default: return new Color(0xFFF6E0);
        }
    }

    /** Coloured illustration tile used on service cards. */
    public static JComponent tile(LaundryService s, int w, int h) {
        JComponent c = new JComponent() {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setPaint(new GradientPaint(0, 0, tint(s), 0, getHeight(), Color.WHITE));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                int size = Math.min(getWidth(), getHeight()) - 26;
                Color ink = icon(s).equals("stack") ? Theme.PRIMARY : new Color(0x3C78C8);
                Icons.of(icon(s), size, ink, 1.6f).paintIcon(this, g, (getWidth() - size) / 2, (getHeight() - size) / 2);
                g.dispose();
            }
        };
        c.setPreferredSize(new Dimension(w, h));
        return c;
    }
}
