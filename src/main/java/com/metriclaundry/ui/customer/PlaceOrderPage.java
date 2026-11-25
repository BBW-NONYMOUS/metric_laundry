package com.metriclaundry.ui.customer;

import com.metriclaundry.model.LaundryService;
import com.metriclaundry.model.Order;
import com.metriclaundry.model.User;
import com.metriclaundry.service.CatalogService;
import com.metriclaundry.service.OrderService;
import com.metriclaundry.ui.common.Page;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Customer: choose a service, enter weight / pieces, see the live total and place the order. */
public class PlaceOrderPage extends Page {
    private final User user;
    private final OrderService orders = new OrderService();

    private List<LaundryService> services = new ArrayList<>();
    private final List<RoundedPanel> cards = new ArrayList<>();
    private LaundryService selected;
    private int preselectId;

    public PlaceOrderPage(User user) {
        super("Place Order", "Choose a service and tell us how much laundry you have");
        this.user = user;
        initComponents();
        qty.addChangeListener(e -> updateSummary());
        place.addActionListener(e -> placeOrder());
    }

    public void preselect(int serviceId) { this.preselectId = serviceId; }

    private void placeOrder() {
        if (selected == null) { Dialogs.info(this, "Please choose a service."); return; }
        BigDecimal q = BigDecimal.valueOf((Double) qty.getValue());
        if (!Dialogs.confirm(this, "Place this order?\n\n" + selected.getName() + "  -  " + q.stripTrailingZeros().toPlainString()
                + " " + selected.unitLabel() + "\nTotal: " + Formats.peso(orders.computeTotal(selected, q)))) return;
        final Order[] created = new Order[1];
        if (Dialogs.run(this, () -> created[0] = orders.placeOrder(user.getId(), selected.getId(), q, notes.getText(), user))) {
            notes.setText("");
            int go = JOptionPane.showOptionDialog(this, "Order " + created[0].getOrderNo() + " has been placed!\n"
                            + "Bring your laundry to the shop or wait for our rider.\n\nWould you like to pay now?",
                    "Order Placed", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new Object[]{"Pay Now", "Pay Later"}, "Pay Now");
            if (go == 0) {
                PaymentPage pp = frame.page("payment");
                pp.select(created[0].getId());
                frame.navigate("payment");
            } else {
                TrackOrderPage tp = frame.page("track");
                tp.show(created[0].getId());
                frame.navigate("track");
            }
        }
    }

    private void select(LaundryService s) {
        selected = s;
        for (int i = 0; i < cards.size(); i++) {
            boolean on = services.get(i) == s;
            cards.get(i).border(on ? Theme.PRIMARY : Theme.BORDER);
            cards.get(i).setFill(on ? Theme.PRIMARY_LIGHT : Color.WHITE);
        }
        boolean pc = "PC".equals(s.getUnit());
        SpinnerNumberModel m = (SpinnerNumberModel) qty.getModel();
        m.setStepSize(pc ? 1.0 : 0.5);
        m.setMinimum(pc ? 1.0 : 0.5);
        if (pc) qty.setValue(Math.max(1.0, Math.round((Double) qty.getValue())));
        unitLabel.setText(pc ? "piece(s)" : "kg");
        updateSummary();
    }

    private void updateSummary() {
        if (selected == null) return;
        BigDecimal q = BigDecimal.valueOf((Double) qty.getValue());
        sumService.setText(selected.getName());
        sumQty.setText(q.stripTrailingZeros().toPlainString() + " " + selected.unitLabel());
        sumPrice.setText(Formats.peso(selected.getPrice()) + " / " + selected.unitLabel());
        sumEta.setText(Formats.friendly(LocalDateTime.now().plusHours(selected.getTurnaroundHours())));
        sumTotal.setText(Formats.peso(orders.computeTotal(selected, q)));
    }

    @Override public void refresh() {
        load(() -> new CatalogService().list(true), list -> {
            services = list;
            cards.clear();
            choices.removeAll();
            for (LaundryService s : list) {
                RoundedPanel card = new RoundedPanel(12, Color.WHITE).shadow(false);
                card.setLayout(new BorderLayout(0, 6));
                card.setBorder(Theme.pad(8, 8, 10, 8));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.add(ServiceVisuals.tile(s, 130, 74), BorderLayout.NORTH);
                JLabel n = Theme.label(s.getName(), Font.BOLD, 14, Theme.TEXT);
                n.setHorizontalAlignment(SwingConstants.CENTER);
                JLabel p = Theme.label("<html><center>" + Formats.peso(s.getPrice()) + " / " + s.unitLabel()
                        + "<br><font color='#667566'>" + (s.getDescription() == null ? "" : s.getDescription())
                        + "</font></center></html>", Font.PLAIN, 12, Theme.TEXT);
                p.setHorizontalAlignment(SwingConstants.CENTER);
                card.add(n, BorderLayout.CENTER);
                card.add(p, BorderLayout.SOUTH);
                card.addMouseListener(new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { select(s); } });
                cards.add(card);
                choices.add(card);
            }
            choices.revalidate();
            LaundryService pick = list.stream().filter(s -> s.getId() == preselectId).findFirst()
                    .orElse(list.isEmpty() ? null : list.get(0));
            preselectId = 0;
            if (pick != null) select(pick);
        });
    }

    private static JLabel value() { return Theme.label("-", Font.BOLD, 13, Theme.TEXT); }

    private static JPanel kv(String k, JLabel v) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(Theme.label(k, Font.PLAIN, 13, Theme.MUTED), BorderLayout.WEST);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private static JLabel step(String n, String text) {
        JLabel l = Theme.label(text, Font.BOLD, 15, Theme.TEXT);
        l.setIcon(new Icon() {
            public int getIconWidth() { return 24; }
            public int getIconHeight() { return 24; }
            public void paintIcon(Component c, Graphics g0, int x, int y) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setColor(Theme.PRIMARY);
                g.fillOval(x, y, 24, 24);
                g.setColor(Color.WHITE);
                g.setFont(Theme.bold(12));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(n, x + 12 - fm.stringWidth(n) / 2, y + 17);
                g.dispose();
            }
        });
        l.setIconTextGap(10);
        return l;
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        choices = new JPanel();
        qty = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 100.0, 0.5));
        unitLabel = new JLabel();
        notes = new JTextArea(3, 20);
        sumService = new JLabel();
        sumQty = new JLabel();
        sumPrice = new JLabel();
        sumEta = new JLabel();
        sumTotal = new JLabel();
        form = new RoundedPanel();
        view1 = step("1", "Select a service");
        view2 = step("2", "Per kg");
        q = new JPanel();
        view3 = new JLabel();
        view4 = step("3", "Special instructions (optional)");
        ns = Theme.scroll(null);
        view5 = new JPanel();
        summary = new RoundedPanel();
        h = new JLabel();
        view6 = new JPanel();
        view7 = new JLabel();
        view8 = new JPanel();
        view9 = new JLabel();
        view10 = new JPanel();
        view11 = new JLabel();
        view12 = new JPanel();
        view13 = new JLabel();
        view14 = new JSeparator();
        view15 = new JLabel();
        place = AppButton.primary("Place Order", "check").large();
        note = new JLabel();
        view16 = new JPanel();
        this.setLayout(new BorderLayout(16, 0));
        choices.setOpaque(false);
        choices.setLayout(new GridLayout(1, 0, 12, 0));
        qty.setPreferredSize(new Dimension(120, 36));
        qty.setFont(Theme.bold(15));
        unitLabel.setText("kg");
        unitLabel.setFont(Theme.font(Font.BOLD, 14));
        unitLabel.setForeground(Theme.MUTED);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(Theme.regular(13));
        notes.setBorder(Theme.pad(8, 10, 8, 10));
        sumService.setText("-");
        sumService.setFont(Theme.font(Font.BOLD, 13));
        sumService.setForeground(Theme.TEXT);
        sumService.setHorizontalAlignment(4);
        sumQty.setText("-");
        sumQty.setFont(Theme.font(Font.BOLD, 13));
        sumQty.setForeground(Theme.TEXT);
        sumQty.setHorizontalAlignment(4);
        sumPrice.setText("-");
        sumPrice.setFont(Theme.font(Font.BOLD, 13));
        sumPrice.setForeground(Theme.TEXT);
        sumPrice.setHorizontalAlignment(4);
        sumEta.setText("-");
        sumEta.setFont(Theme.font(Font.BOLD, 13));
        sumEta.setForeground(Theme.TEXT);
        sumEta.setHorizontalAlignment(4);
        sumTotal.setText(Formats.peso(BigDecimal.ZERO));
        sumTotal.setFont(Theme.font(Font.BOLD, 28));
        sumTotal.setForeground(Theme.PRIMARY_DARK);
        form.setBorder(Theme.pad(20, 22, 20, 22));
        form.setLayout(new GridBagLayout());
        view1.setText("Select a service");
        view1.setFont(Theme.bold(15));
        view1.setForeground(Theme.TEXT);
        view2.setText("Per kg");
        view2.setFont(Theme.bold(15));
        view2.setForeground(Theme.TEXT);
        q.setOpaque(false);
        q.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        view3.setText("   Tip: weigh your laundry at home or let our staff weigh it on drop-off.");
        view3.setFont(Theme.font(Font.ITALIC, 12));
        view3.setForeground(Theme.MUTED);
        view4.setText("Special instructions (optional)");
        view4.setFont(Theme.bold(15));
        view4.setForeground(Theme.TEXT);
        view5.setOpaque(false);
        view5.setPreferredSize(new Dimension(0, 0));
        view5.setMinimumSize(new Dimension(0, 0));
        view5.setLayout(new FlowLayout());
        summary.setBorder(Theme.pad(20, 22, 20, 22));
        summary.setPreferredSize(new Dimension(300, 0));
        summary.setLayout(new GridBagLayout());
        h.setText("Order Summary");
        h.setFont(Theme.font(Font.BOLD, 17));
        h.setForeground(Theme.TEXT);
        h.setIcon(Icons.of("receipt", 20, Theme.PRIMARY));
        view6.setOpaque(false);
        view6.setLayout(new BorderLayout());
        view7.setText("Service");
        view7.setFont(Theme.font(Font.PLAIN, 13));
        view7.setForeground(Theme.MUTED);
        view8.setOpaque(false);
        view8.setLayout(new BorderLayout());
        view9.setText("Per kg");
        view9.setFont(Theme.font(Font.PLAIN, 13));
        view9.setForeground(Theme.MUTED);
        view10.setOpaque(false);
        view10.setLayout(new BorderLayout());
        view11.setText("Price");
        view11.setFont(Theme.font(Font.PLAIN, 13));
        view11.setForeground(Theme.MUTED);
        view12.setOpaque(false);
        view12.setLayout(new BorderLayout());
        view13.setText("Ready by");
        view13.setFont(Theme.font(Font.PLAIN, 13));
        view13.setForeground(Theme.MUTED);
        view15.setText("Total");
        view15.setFont(Theme.font(Font.PLAIN, 13));
        view15.setForeground(Theme.MUTED);
        place.setText("Place Order");
        note.setText("<html><div style='width:200px;color:#667566'>You can pay now via GCash or pay "
                + "upon pickup. Final weight is confirmed by our staff.</div></html>");
        note.setFont(Theme.regular(11.5f));
        view16.setOpaque(false);
        view16.setPreferredSize(new Dimension(0, 0));
        view16.setMinimumSize(new Dimension(0, 0));
        view16.setLayout(new FlowLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 11;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        form.add(view1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 11;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        form.add(choices, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 11;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        form.add(view2, gridBagConstraints);
        q.add(qty);
        q.add(unitLabel);
        q.add(view3);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 11;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        form.add(q, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 11;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        form.add(view4, gridBagConstraints);
        ns.setViewportView(notes);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 11;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        form.add(ns, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 11;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        form.add(view5, gridBagConstraints);
        this.add(form, BorderLayout.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        summary.add(h, gridBagConstraints);
        view6.add(view7, BorderLayout.WEST);
        view6.add(sumService, BorderLayout.EAST);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        summary.add(view6, gridBagConstraints);
        view8.add(view9, BorderLayout.WEST);
        view8.add(sumQty, BorderLayout.EAST);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        summary.add(view8, gridBagConstraints);
        view10.add(view11, BorderLayout.WEST);
        view10.add(sumPrice, BorderLayout.EAST);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        summary.add(view10, gridBagConstraints);
        view12.add(view13, BorderLayout.WEST);
        view12.add(sumEta, BorderLayout.EAST);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        summary.add(view12, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        summary.add(view14, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        summary.add(view15, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        summary.add(sumTotal, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        summary.add(place, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        summary.add(note, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        summary.add(view16, gridBagConstraints);
        this.add(summary, BorderLayout.EAST);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel choices;
    private JSpinner qty;
    private JLabel unitLabel;
    private JTextArea notes;
    private JLabel sumService;
    private JLabel sumQty;
    private JLabel sumPrice;
    private JLabel sumEta;
    private JLabel sumTotal;
    private RoundedPanel form;
    private JLabel view1;
    private JLabel view2;
    private JPanel q;
    private JLabel view3;
    private JLabel view4;
    private JScrollPane ns;
    private JPanel view5;
    private RoundedPanel summary;
    private JLabel h;
    private JPanel view6;
    private JLabel view7;
    private JPanel view8;
    private JLabel view9;
    private JPanel view10;
    private JLabel view11;
    private JPanel view12;
    private JLabel view13;
    private JSeparator view14;
    private JLabel view15;
    private AppButton place;
    private JLabel note;
    private JPanel view16;
    // End of variables declaration//GEN-END:variables
}
