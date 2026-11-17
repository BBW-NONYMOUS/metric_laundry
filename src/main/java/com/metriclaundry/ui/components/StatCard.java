package com.metriclaundry.ui.components;

import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/** Dashboard KPI card: icon on top, title, big value. */
public class StatCard extends RoundedPanel {
    /** No-argument constructor for the NetBeans GUI designer. */
    public StatCard() { this("orders", "Summary", Theme.PRIMARY); }
    private final JLabel value;

    public StatCard(String icon, String title, Color tint) {
        super(new GridBagLayout());
        setBorder(Theme.pad(14, 12, 14, 12));
        JLabel ic = new JLabel(Icons.of(icon, 24, Color.WHITE, 2.3f)) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setColor(tint);
                g.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g.dispose();
                super.paintComponent(g0);
            }
        };
        ic.setPreferredSize(new Dimension(44, 44));
        ic.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel t = Theme.label(title, Font.BOLD, 13, Theme.TEXT);
        value = Theme.label("0", Font.BOLD, 26, Theme.TEXT);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.insets = new Insets(0, 0, 6, 0);
        add(ic, c);
        c.gridy = 1; c.insets = new Insets(0, 0, 0, 0);
        add(t, c);
        c.gridy = 2;
        add(value, c);
    }

    public void setValue(String v) { value.setText(v); }
}
