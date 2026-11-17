package com.metriclaundry.ui.components;

import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Rounded text / password field with a leading icon and placeholder text (like the Admin Login mockup). */
public class InputField extends JPanel {
    private final JTextComponent field;
    private String placeholder;

    /** No-argument constructor for the NetBeans GUI designer. */
    public InputField() { this("Text", "user", false); }

    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public InputField(String placeholder, String icon, boolean password) {
        super(new BorderLayout(8, 0));
        this.placeholder = placeholder;
        setOpaque(false);
        setBorder(Theme.pad(0, 12, 0, 12));
        setPreferredSize(new Dimension(260, 42));

        field = password ? new JPasswordField() : new JTextField();
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setOpaque(false);
        field.setFont(Theme.regular(14));
        field.setForeground(Theme.TEXT);
        field.setCaretColor(Theme.PRIMARY_DARK);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e) { repaint(); }
        });
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
        });
        if (icon != null) add(new JLabel(Icons.of(icon, 18, Theme.MUTED)), BorderLayout.WEST);
        add(field, BorderLayout.CENTER);
    }

    public JTextComponent field() { return field; }
    public String getText() { return field.getText(); }
    public void setText(String t) { field.setText(t); }
    public char[] getPassword() { return ((JPasswordField) field).getPassword(); }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g.setColor(field.isFocusOwner() ? Theme.PRIMARY : Theme.BORDER);
        g.setStroke(new BasicStroke(field.isFocusOwner() ? 1.6f : 1f));
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g.dispose();
        super.paintComponent(g0);
    }

    @Override protected void paintChildren(Graphics g0) {
        super.paintChildren(g0);
        if (field.getText().isEmpty() && placeholder != null) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            g.setColor(Theme.MUTED);
            g.setFont(Theme.regular(14));
            Rectangle r = field.getBounds();
            FontMetrics fm = g.getFontMetrics();
            g.drawString(placeholder, r.x + 2, r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}
