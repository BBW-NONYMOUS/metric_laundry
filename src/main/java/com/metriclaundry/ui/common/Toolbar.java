package com.metriclaundry.ui.common;

import com.metriclaundry.ui.components.InputField;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/** Search / filter bar shown above management tables. */
public class Toolbar extends JPanel {

    public Toolbar() {
        super(new BorderLayout());

        initComponents();

    }

    /** Adds a search box that calls onChange after the user stops typing (debounced). */
    public InputField search(String placeholder, Runnable onChange) {
        InputField f = new InputField(placeholder, "search", false);
        return search(f, onChange);
    }

    /** Wires a search field supplied by a NetBeans form. */
    public InputField search(InputField f, Runnable onChange) {
        f.setPreferredSize(new Dimension(260, 38));
        Timer t = new Timer(350, e -> onChange.run());
        t.setRepeats(false);
        f.field().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { t.restart(); }
            public void removeUpdate(DocumentEvent e) { t.restart(); }
            public void changedUpdate(DocumentEvent e) { t.restart(); }
        });
        left.add(f);
        return f;
    }

    public Toolbar left(JComponent c) { left.add(c); return this; }
    public Toolbar right(JComponent c) { right.add(c); return this; }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        left = new JPanel();
        right = new JPanel();
        this.setOpaque(false);
        this.setBorder(Theme.pad(0, 0, 12, 0));
        this.setLayout(new BorderLayout());
        left.setOpaque(false);
        left.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        right.setOpaque(false);
        right.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        this.add(left, BorderLayout.WEST);
        this.add(right, BorderLayout.EAST);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel left;
    private JPanel right;
    // End of variables declaration//GEN-END:variables
}
