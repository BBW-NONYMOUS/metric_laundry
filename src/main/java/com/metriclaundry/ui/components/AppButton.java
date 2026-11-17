package com.metriclaundry.ui.components;

import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/** Rounded button in the brand style. Variants: PRIMARY, OUTLINE, DANGER, LIGHT, GHOST. */
public class AppButton extends JButton {
    /** No-argument constructor for the NetBeans GUI designer. */
    public AppButton() { this("Button", Variant.PRIMARY); }
    public enum Variant { PRIMARY, OUTLINE, DANGER, LIGHT, GHOST, BLUE }

    private final Variant variant;
    private boolean hover;
    private int arc = 12;

    public AppButton(String text, Variant variant) { this(text, null, variant); }

    public AppButton(String text, String icon, Variant variant) {
        super(text);
        this.variant = variant;
        setFont(Theme.bold(13));
        setForeground(fg());
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(Theme.pad(9, 16, 9, 16));
        setIconTextGap(8);
        if (icon != null) setIcon(Icons.of(icon, 16, fg()));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    public static AppButton primary(String text) { return new AppButton(text, Variant.PRIMARY); }
    public static AppButton primary(String text, String icon) { return new AppButton(text, icon, Variant.PRIMARY); }
    public static AppButton outline(String text, String icon) { return new AppButton(text, icon, Variant.OUTLINE); }
    public static AppButton danger(String text, String icon) { return new AppButton(text, icon, Variant.DANGER); }

    public AppButton small() {
        setFont(Theme.bold(12));
        setBorder(Theme.pad(6, 12, 6, 12));
        arc = 10;
        return this;
    }

    public AppButton large() {
        setFont(Theme.bold(15));
        setBorder(Theme.pad(12, 20, 12, 20));
        return this;
    }

    private Color fg() {
        switch (variant) {
            case OUTLINE: case LIGHT: case GHOST: return Theme.PRIMARY_DARK;
            default: return Color.WHITE;
        }
    }

    private Color bg() {
        boolean on = isEnabled();
        switch (variant) {
            case PRIMARY: return !on ? Theme.DISABLED : hover ? Theme.PRIMARY_DARK : Theme.PRIMARY;
            case DANGER: return !on ? Theme.DISABLED : hover ? new Color(0xB3261E) : Theme.DANGER;
            case BLUE: return hover ? new Color(0x0857CC) : Theme.GCASH;
            case LIGHT: return hover ? Theme.PRIMARY_SOFT : Theme.PRIMARY_LIGHT;
            case OUTLINE: return hover ? Theme.PRIMARY_LIGHT : Color.WHITE;
            default: return hover ? Theme.PRIMARY_LIGHT : new Color(0, 0, 0, 0);
        }
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        RoundRectangle2D r = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g.setColor(bg());
        g.fill(r);
        if (variant == Variant.OUTLINE) {
            g.setColor(Theme.BORDER);
            g.draw(r);
        }
        g.dispose();
        setForeground(isEnabled() ? fg() : Color.WHITE);
        super.paintComponent(g0);
    }
}
