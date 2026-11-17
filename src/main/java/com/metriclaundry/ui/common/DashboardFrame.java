package com.metriclaundry.ui.common;

import com.metriclaundry.model.User;
import com.metriclaundry.ui.LoginFrame;
import com.metriclaundry.ui.components.Logo;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared shell for all three roles: sidebar navigation + header + CardLayout content.
 * Customer uses the light sidebar; Admin and Staff use the dark-green sidebar (as in the mockup).
 */
public class DashboardFrame extends JFrame {
    private final User user;
    private final boolean dark;


    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final Map<String, NavItem> items = new LinkedHashMap<>();
    private String currentKey;

    private String windowTitle;
    private boolean darkSidebar;
    private String panelName;

    public DashboardFrame(String windowTitle, User user, boolean darkSidebar, String panelName) {
        super("Metric Laundry - " + windowTitle);
        this.dark = darkSidebar;
        this.windowTitle = windowTitle;
        this.user = user;
        this.darkSidebar = darkSidebar;
        this.panelName = panelName;
        initComponents();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1320, 800);
        setLocationRelativeTo(null);
    }

    public User user() { return user; }

    // ------------------------------------------------------------------ layout
    private JPanel buildSidebar(String panelName) {
        JPanel side = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                if (dark) g.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_DARK, 0, getHeight(), Theme.PRIMARY_DEEP));
                else g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                if (!dark) { g.setColor(Theme.BORDER); g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight()); }
                g.dispose();
            }
        };
        side.setPreferredSize(new Dimension(230, 0));
        side.setBorder(Theme.pad(18, 12, 16, 12));

        Logo logo = new Logo(dark ? 120 : 130, dark);
        if (panelName != null) logo.subtitle(panelName);
        JPanel logoWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoWrap.setOpaque(false);
        logoWrap.setBorder(Theme.pad(0, 0, 18, 0));
        logoWrap.add(logo);
        side.add(logoWrap, BorderLayout.NORTH);

        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        side.add(nav, BorderLayout.CENTER);

        NavItem logout = new NavItem("Logout", "logout", this::logout);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(logout);
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(Theme.pad(18, 22, 10, 22));
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        right.setOpaque(false);
        JLabel date = Theme.label(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")),
                Font.PLAIN, 13, Theme.MUTED);
        date.setIcon(Icons.of("clock", 16, Theme.MUTED));
        right.add(date);

        String chipName = user.getRole() == com.metriclaundry.model.Role.CUSTOMER ? user.getFirstName()
                : Character.toUpperCase(user.getUsername().charAt(0)) + user.getUsername().substring(1);
        JLabel chip = new JLabel(chipName + "  \u25BE", Icons.of("user", 18, Theme.PRIMARY_DARK), SwingConstants.LEFT) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setColor(Color.WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g.setColor(Theme.BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g.dispose();
                super.paintComponent(g0);
            }
        };
        chip.setFont(Theme.bold(13));
        chip.setForeground(Theme.TEXT);
        chip.setBorder(Theme.pad(6, 12, 6, 14));
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.setToolTipText(user.getFullName() + " - " + user.getRole().label());
        JPopupMenu menu = new JPopupMenu();
        JMenuItem who = new JMenuItem(user.getFullName() + " (" + user.getRole().label() + ")");
        who.setEnabled(false);
        menu.add(who);
        menu.addSeparator();
        JMenuItem settings = new JMenuItem("Account Settings");
        settings.addActionListener(e -> { if (pages.containsKey("settings")) navigate("settings"); });
        menu.add(settings);
        JMenuItem out = new JMenuItem("Logout");
        out.addActionListener(e -> logout());
        menu.add(out);
        chip.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { menu.show(chip, 0, chip.getHeight() + 4); }
        });
        right.add(chip);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ------------------------------------------------------------------ pages
    public void addPage(String key, String label, String icon, Page page) {
        page.attach(this);
        pages.put(key, page);
        content.add(page, key);
        if (label != null) {
            NavItem item = new NavItem(label, icon, () -> navigate(key));
            items.put(key, item);
            nav.add(item);
            nav.add(Box.createVerticalStrut(4));
        }
    }

    public void navigate(String key) {
        Page p = pages.get(key);
        if (p == null) return;
        currentKey = key;
        title.setText(p.title());
        subtitle.setText(p.subtitle());
        items.forEach((k, v) -> v.setSelected(k.equals(key)));
        ((CardLayout) content.getLayout()).show(content, key);
        p.refresh();
    }

    @SuppressWarnings("unchecked")
    public <T extends Page> T page(String key) { return (T) pages.get(key); }

    public String currentKey() { return currentKey; }

    public void logout() {
        if (JOptionPane.showConfirmDialog(this, "Log out of Metric Laundry?", "Logout",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Session.clear();
        dispose();
        new LoginFrame().setVisible(true);
    }

    // ------------------------------------------------------------------ nav item
    private final class NavItem extends JComponent {
    private final String text;
    private final String icon;
    private boolean selected, hover;

        NavItem(String text, String icon, Runnable onClick) {
            this.text = text;
            this.icon = icon;
            setPreferredSize(new Dimension(200, dark ? 38 : 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, dark ? 38 : 44));
            setAlignmentX(LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setSelected(boolean s) { selected = s; repaint(); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            Color fg;
            if (dark) {
                if (selected) { g.setColor(Theme.ACCENT); g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); }
                else if (hover) { g.setColor(new Color(255, 255, 255, 30)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); }
                fg = Color.WHITE;
            } else {
                if (selected) {
                    g.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, getWidth(), 0, Theme.ACCENT));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                } else if (hover) { g.setColor(Theme.PRIMARY_LIGHT); g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); }
                fg = selected ? Color.WHITE : Theme.TEXT;
            }
            int is = dark ? 18 : 22;
            Icons.of(icon, is, dark || selected ? fg : Theme.PRIMARY_DARK, 2.2f).paintIcon(this, g, 14, (getHeight() - is) / 2);
            g.setColor(fg);
            g.setFont(selected ? Theme.bold(dark ? 13 : 15) : Theme.regular(dark ? 13 : 15));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, 14 + is + 14, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        nav = new JPanel();
        content = new JPanel();
        title = new JLabel();
        subtitle = new JLabel();
        root = new JPanel();
        view1 = buildSidebar(panelName);
        main = new JPanel();
        view2 = buildHeader();
        setMinimumSize(new Dimension(1180, 720));
        getContentPane().setLayout(new BorderLayout());
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setLayout(new CardLayout());
        title.setText("");
        title.setFont(Theme.font(Font.BOLD, 24));
        title.setForeground(Theme.TEXT);
        subtitle.setText("");
        subtitle.setFont(Theme.font(Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED);
        root.setBackground(Theme.BG);
        root.setLayout(new BorderLayout());
        main.setOpaque(false);
        main.setLayout(new BorderLayout());
        root.add(view1, BorderLayout.WEST);
        main.add(view2, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        getContentPane().add(root, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel nav;
    private JPanel content;
    private JLabel title;
    private JLabel subtitle;
    private JPanel root;
    private JPanel view1;
    private JPanel main;
    private JPanel view2;
    // End of variables declaration//GEN-END:variables
}
