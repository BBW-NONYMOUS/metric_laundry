package com.metriclaundry.ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Arrays;

/**
 * Metric Laundry design tokens (colours, fonts, spacing) + shared styling helpers.
 * Change the colours here and the whole application follows.
 */
public final class Theme {

    // ---- Brand colours (taken from the mockup) ----
    public static final Color PRIMARY       = new Color(0x1E8E3E);
    public static final Color PRIMARY_DARK  = new Color(0x13672B);
    public static final Color PRIMARY_DEEP  = new Color(0x0F4F22);
    public static final Color PRIMARY_LIGHT = new Color(0xE6F4E8);
    public static final Color PRIMARY_SOFT  = new Color(0xCFEAD3);
    public static final Color ACCENT        = new Color(0x4CAF50);
    public static final Color LEAF          = new Color(0x7CC576);

    // ---- Neutrals ----
    public static final Color BG       = new Color(0xF4F8F4);
    public static final Color CARD     = Color.WHITE;
    public static final Color BORDER   = new Color(0xDCE6DC);
    public static final Color TEXT     = new Color(0x1C2B1F);
    public static final Color MUTED    = new Color(0x667566);
    public static final Color DISABLED = new Color(0xB8C2B8);

    // ---- Status colours ----
    public static final Color WARN_BG   = new Color(0xFFF3BF);
    public static final Color WARN_FG   = new Color(0x8A6100);
    public static final Color OK_BG     = new Color(0xD8F1DC);
    public static final Color OK_FG     = new Color(0x1B7A33);
    public static final Color INFO_BG   = new Color(0xDCEBFF);
    public static final Color INFO_FG   = new Color(0x1C5DB6);
    public static final Color DANGER    = new Color(0xD93025);
    public static final Color DANGER_BG = new Color(0xFDE2E0);
    public static final Color GCASH     = new Color(0x0A6CFF);

    public static final int RADIUS = 14;

    private static String family = Font.SANS_SERIF;

    private Theme() { }

    // ---------------------------------------------------------------- fonts
    public static Font font(int style, float size) { return new Font(family, style, Math.round(size)); }
    public static Font regular(float size) { return font(Font.PLAIN, size); }
    public static Font bold(float size) { return font(Font.BOLD, size); }

    // ---------------------------------------------------------------- look & feel
    /** Uses FlatLaf if it is on the classpath (Maven dependency), otherwise a tuned Metal theme. */
    public static void install() {
        String[] preferred = {"Segoe UI", "Poppins", "Inter", "Roboto", "Noto Sans", "DejaVu Sans"};
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        // Pick the first nice font that can also draw the peso sign and arrows; otherwise use the
        // logical "Dialog" font, which falls back per-glyph so the peso sign always renders.
        family = Font.DIALOG;
        for (String p : preferred) {
            if (Arrays.asList(available).contains(p)
                    && new Font(p, Font.PLAIN, 12).canDisplayUpTo("\u20B1\u2192\u25BE\u203A\u2022") == -1) {
                family = p;
                break;
            }
        }
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        boolean flat = false;
        try {
            Class<?> laf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Component.accentColor", PRIMARY);
            UIManager.put("Component.focusColor", PRIMARY);
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("TitlePane.background", Color.WHITE);
            laf.getMethod("setup").invoke(null);
            flat = true;
        } catch (Exception notOnClasspath) {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) { }
        }

        Font base = regular(13);
        for (String key : new String[]{"Label.font", "Button.font", "TextField.font", "PasswordField.font",
                "TextArea.font", "ComboBox.font", "Table.font", "TableHeader.font", "List.font", "RadioButton.font",
                "CheckBox.font", "Spinner.font", "OptionPane.messageFont", "OptionPane.buttonFont", "ToolTip.font",
                "MenuItem.font", "FormattedTextField.font", "TabbedPane.font"}) {
            UIManager.put(key, base);
        }
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Table.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("List.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("List.selectionForeground", TEXT);
        if (!flat) {
            UIManager.put("ComboBox.background", Color.WHITE);
            UIManager.put("TextField.border", fieldBorder());
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Button.focus", new Color(0, 0, 0, 0));
            UIManager.put("RadioButton.focus", new Color(0, 0, 0, 0));
            UIManager.put("CheckBox.focus", new Color(0, 0, 0, 0));
        }
    }

    // ---------------------------------------------------------------- helpers
    public static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(new RoundedLineBorder(BORDER, 10), new EmptyBorder(6, 10, 6, 10));
    }

    public static Border pad(int t, int l, int b, int r) { return new EmptyBorder(t, l, b, r); }

    /** Applies the clean card-table style used across the app. */
    public static void styleTable(JTable t) {
        t.setRowHeight(42);
        t.setFont(regular(13));
        t.setForeground(TEXT);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(0xEDF2ED));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(PRIMARY_LIGHT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setReorderingAllowed(false);
        JTableHeader h = t.getTableHeader();
        h.setPreferredSize(new Dimension(0, 40));
        DefaultTableCellRenderer hr = new DefaultTableCellRenderer();
        hr.setFont(bold(12.5f));
        hr.setBackground(new Color(0xF1F6F1));
        hr.setForeground(TEXT);
        hr.setBorder(new EmptyBorder(0, 12, 0, 8));
        h.setDefaultRenderer((table, value, sel, focus, row, col) -> {
            JLabel l = (JLabel) hr.getTableCellRendererComponent(table, value, false, false, row, col);
            l.setFont(bold(12.5f));
            l.setBackground(new Color(0xF1F6F1));
            l.setOpaque(true);
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 12, 0, 8)));
            return l;
        });
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                                     boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, sel, false, row, col);
                l.setBorder(new EmptyBorder(0, 12, 0, 8));
                if (!sel) l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFCFA));
                return l;
            }
        };
        t.setDefaultRenderer(Object.class, cell);
        t.setDefaultRenderer(Number.class, cell);
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    public static void antialias(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /** Thin rounded line border. */
    public static class RoundedLineBorder implements Border {
        private final Color color;
        private final int arc;
        public RoundedLineBorder(Color color, int arc) { this.color = color; this.arc = arc; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            antialias(g2);
            g2.setColor(c.hasFocus() ? PRIMARY : color);
            g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(2, 2, 2, 2); }
        @Override public boolean isBorderOpaque() { return false; }
    }
}
