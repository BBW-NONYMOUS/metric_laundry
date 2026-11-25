package com.metriclaundry.ui.customer;

import com.metriclaundry.model.*;
import com.metriclaundry.service.CatalogService;
import com.metriclaundry.service.OrderService;
import com.metriclaundry.ui.common.Page;
import com.metriclaundry.ui.common.Tables;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/** Customer Home - matches the "Customer Dashboard" mockup. */
public class CustomerHomePage extends Page {
    private final User user;
    private final OrderService orders = new OrderService();

    private final DefaultTableModel recent = Tables.model("Order No.", "Service", "Per kg", "Total", "Status", "Date");

    private List<Order> recentRows = List.of();

    public CustomerHomePage(User user) {
        super("Hi, " + user.getFirstName() + "!", "Here is what's happening with your laundry today");
        this.user = user;
        initComponents();
        Tables.badgeColumn(recentTable, 4);
        Tables.widths(recentTable, 150, 100, 70, 80, 110, 100);
        recentTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = recentTable.getSelectedRow();
                if (e.getClickCount() == 2 && r >= 0) {
                    TrackOrderPage tp = frame.page("track");
                    tp.show(recentRows.get(recentTable.convertRowIndexToModel(r)).getId());
                    frame.navigate("track");
                }
            }
        });
    }

    private JLabel thankYou() {
        JLabel l = new JLabel("<html>Thank you for choosing<br>Metric Laundry!</html>", Icons.of("heart", 20, Color.WHITE), SwingConstants.LEFT) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_DARK, getWidth(), 0, Theme.ACCENT));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g.dispose();
                super.paintComponent(g0);
            }
        };
        l.setFont(Theme.font(Font.BOLD | Font.ITALIC, 12.5f));
        l.setForeground(Color.WHITE);
        l.setBorder(Theme.pad(12, 12, 12, 12));
        return l;
    }

    static JPanel cardHeader(String title, String icon, String link, Runnable onLink) {
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);
        JLabel t = Theme.label(title, Font.BOLD, 16, Theme.TEXT);
        t.setIcon(Icons.of(icon, 18, Theme.PRIMARY_DARK));
        t.setIconTextGap(8);
        h.add(t, BorderLayout.WEST);
        if (link != null) {
            JLabel l = Theme.label(link + "  \u2192", Font.BOLD, 12, Theme.TEXT);
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            l.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { onLink.run(); } });
            h.add(l, BorderLayout.EAST);
        }
        return h;
    }

    private JComponent serviceCard(LaundryService s) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE).shadow(false);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(Theme.pad(6, 6, 10, 6));
        card.add(ServiceVisuals.tile(s, 150, 90), BorderLayout.NORTH);
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel name = Theme.label(s.getName(), Font.BOLD, 15, Theme.TEXT);
        name.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel price = Theme.label(Formats.peso(s.getPrice()) + " / " + s.unitLabel(), Font.PLAIN, 12.5f, Theme.TEXT);
        price.setHorizontalAlignment(SwingConstants.CENTER);
        info.add(name);
        info.add(price);
        card.add(info, BorderLayout.CENTER);
        AppButton order = AppButton.primary("Order Now  \u203A").small();
        order.addActionListener(e -> {
            PlaceOrderPage p = frame.page("place");
            p.preselect(s.getId());
            frame.navigate("place");
        });
        JPanel bw = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bw.setOpaque(false);
        bw.add(order);
        card.add(bw, BorderLayout.SOUTH);
        return card;
    }

    @Override public void refresh() {
        load(() -> {
                    List<Order> active = orders.search(user.getId(), null, true, null, 1);
                    return new Object[]{new CatalogService().list(true), orders.search(user.getId(), null, false, null, 5),
                            active, active.isEmpty() ? null : orders.history(active.get(0).getId())};
                },
                data -> {
                    @SuppressWarnings("unchecked") List<LaundryService> svc = (List<LaundryService>) data[0];
                    @SuppressWarnings("unchecked") List<Order> last = (List<Order>) data[1];
                    @SuppressWarnings("unchecked") List<Order> active = (List<Order>) data[2];
                    @SuppressWarnings("unchecked") List<StatusHistory> hist = (List<StatusHistory>) data[3];
                    serviceRow.removeAll();
                    svc.stream().limit(4).forEach(s -> serviceRow.add(serviceCard(s)));
                    serviceRow.revalidate();
                    serviceRow.repaint();

                    recentRows = last;
                    recent.setRowCount(0);
                    for (Order o : last)
                        recent.addRow(new Object[]{o.getOrderNo(), o.getServiceName(), o.quantityLabel(),
                                Formats.peso(o.getTotal()), o.getStatus(), Formats.date(o.getCreatedAt())});

                    if (active.isEmpty()) {
                        timeline.set(null, null);
                        trackTitle.setText("No active order - place one today!");
                        eta.setText("-");
                    } else {
                        Order o = active.get(0);
                        timeline.set(o.getStatus(), hist);
                        trackTitle.setText(o.getOrderNo() + "  \u2022  " + o.getServiceName());
                        eta.setText(Formats.friendly(o.getEstimatedCompletion()));
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        serviceRow = new JPanel();
        recentTable = Tables.table(recent);
        timeline = new Timeline();
        trackTitle = new JLabel();
        eta = new JLabel();
        view1 = new HeroBanner("Welcome to", "Metric Laundry!", "Clean Clothes, Happy You!");
        body = new JPanel();
        services = new RoundedPanel();
        view2 = cardHeader("Our Services", "shirt", "View All", () -> frame.navigate("place"));
        recentCard = new RoundedPanel();
        view3 = cardHeader("Your Recent Orders", "orders", "View All", () -> frame.navigate("history"));
        sp = Theme.scroll(null);
        track = new RoundedPanel();
        th = new JPanel();
        t = new JLabel();
        etaBox = new RoundedPanel(10, Theme.PRIMARY_LIGHT).shadow(false).border(null);
        view4 = new JLabel();
        etaText = new JPanel();
        view5 = new JLabel();
        south = new JPanel();
        view6 = thankYou();
        this.setLayout(new BorderLayout(0, 14));
        serviceRow.setOpaque(false);
        serviceRow.setLayout(new GridLayout(1, 0, 14, 0));
        recentTable.setRowHeight(36);
        trackTitle.setText("No active order");
        trackTitle.setFont(Theme.font(Font.PLAIN, 12));
        trackTitle.setForeground(Theme.MUTED);
        eta.setText("-");
        eta.setFont(Theme.font(Font.BOLD, 14));
        eta.setForeground(Theme.TEXT);
        body.setOpaque(false);
        body.setLayout(new GridBagLayout());
        services.setLayout(new BorderLayout(0, 10));
        recentCard.setLayout(new BorderLayout(0, 8));
        sp.setPreferredSize(new Dimension(400, 150));
        track.setPreferredSize(new Dimension(290, 400));
        track.setLayout(new BorderLayout(0, 8));
        th.setOpaque(false);
        th.setLayout(new GridLayout(2, 1));
        t.setText("Track Your Laundry");
        t.setFont(Theme.font(Font.BOLD, 16));
        t.setForeground(Theme.TEXT);
        t.setIcon(Icons.of("search", 18, Theme.PRIMARY_DARK));
        etaBox.setBorder(Theme.pad(8, 10, 8, 10));
        etaBox.setLayout(new BorderLayout(10, 0));
        view4.setIcon(Icons.of("clock", 26, Theme.PRIMARY_DARK));
        etaText.setOpaque(false);
        etaText.setLayout(new GridLayout(2, 1));
        view5.setText("Estimated Completion:");
        view5.setFont(Theme.font(Font.PLAIN, 12));
        view5.setForeground(Theme.TEXT);
        south.setOpaque(false);
        south.setLayout(new BorderLayout(0, 10));
        this.add(view1, BorderLayout.NORTH);
        services.add(view2, BorderLayout.NORTH);
        services.add(serviceRow, BorderLayout.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 1;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0.55;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        body.add(services, gridBagConstraints);
        recentCard.add(view3, BorderLayout.NORTH);
        sp.setViewportView(recentTable);
        recentCard.add(sp, BorderLayout.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 1;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        body.add(recentCard, gridBagConstraints);
        th.add(t);
        th.add(trackTitle);
        track.add(th, BorderLayout.NORTH);
        track.add(timeline, BorderLayout.CENTER);
        etaBox.add(view4, BorderLayout.WEST);
        etaText.add(view5);
        etaText.add(eta);
        etaBox.add(etaText);
        south.add(etaBox, BorderLayout.NORTH);
        south.add(view6, BorderLayout.SOUTH);
        track.add(south, BorderLayout.SOUTH);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = 1;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        body.add(track, gridBagConstraints);
        this.add(body, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel serviceRow;
    private JTable recentTable;
    private Timeline timeline;
    private JLabel trackTitle;
    private JLabel eta;
    private HeroBanner view1;
    private JPanel body;
    private RoundedPanel services;
    private JPanel view2;
    private RoundedPanel recentCard;
    private JPanel view3;
    private JScrollPane sp;
    private RoundedPanel track;
    private JPanel th;
    private JLabel t;
    private RoundedPanel etaBox;
    private JLabel view4;
    private JPanel etaText;
    private JLabel view5;
    private JPanel south;
    private JLabel view6;
    // End of variables declaration//GEN-END:variables
}
