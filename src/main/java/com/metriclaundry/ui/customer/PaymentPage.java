package com.metriclaundry.ui.customer;

import com.metriclaundry.model.*;
import com.metriclaundry.service.OrderService;
import com.metriclaundry.service.PaymentService;
import com.metriclaundry.ui.common.Page;
import com.metriclaundry.ui.common.ReceiptDialog;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;

/** Customer payment: GCash (QR + reference no.), Cash on Delivery, or Card at the counter. */
public class PaymentPage extends Page {
    private final User user;
    private final OrderService orders = new OrderService();
    private final PaymentService payments = new PaymentService();

    private final Map<PaymentMethod, JComponent> options = new EnumMap<>(PaymentMethod.class);

    private PaymentMethod method = PaymentMethod.GCASH;
    private int wantedId;

    public PaymentPage(User user) {
        super("Payment", "Pay for your laundry via GCash, cash or card");
        this.user = user;
        initComponents();
        orderBox.addActionListener(e -> updateAmount());
        options.put(PaymentMethod.GCASH, optionGCASH);
        options.put(PaymentMethod.CASH, optionCASH);
        options.put(PaymentMethod.CARD, optionCARD);
        confirm.addActionListener(e -> submit());
        choose(PaymentMethod.GCASH);
    }

    public void select(int orderId) { this.wantedId = orderId; }

    private RoundedPanel option(PaymentMethod m) {
        RoundedPanel p = new RoundedPanel(12, Color.WHITE).shadow(false);
        p.setLayout(new BorderLayout(12, 0));
        p.setBorder(Theme.pad(10, 12, 10, 12));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        String icon = m == PaymentMethod.GCASH ? "phone" : m == PaymentMethod.CASH ? "cash" : "card";
        Color tint = m == PaymentMethod.GCASH ? Theme.GCASH : m == PaymentMethod.CASH ? Theme.PRIMARY : new Color(0x3C78C8);
        JLabel ic = new JLabel(Icons.of(icon, 20, Color.WHITE)) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setColor(tint);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g.dispose();
                super.paintComponent(g0);
            }
        };
        ic.setPreferredSize(new Dimension(36, 36));
        ic.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(ic, BorderLayout.WEST);
        p.add(Theme.label(m.label(), Font.BOLD, 14, Theme.TEXT), BorderLayout.CENTER);
        p.addMouseListener(new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { choose(m); } });
        return p;
    }

    private void choose(PaymentMethod m) {
        method = m;
        options.forEach((k, v) -> {
            RoundedPanel p = (RoundedPanel) v;
            p.border(k == m ? Theme.PRIMARY : Theme.BORDER);
            p.setFill(k == m ? Theme.PRIMARY_LIGHT : Color.WHITE);
        });
        reference.setVisible(m == PaymentMethod.GCASH);
        ((CardLayout) right.getLayout()).show(right, m.name());
        revalidate();
    }

    private JPanel gcashCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setPaint(new GradientPaint(0, 0, new Color(0x1677FF), 0, getHeight(), new Color(0x0A5BD8)));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(Theme.pad(22, 22, 22, 22));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        JLabel logo = Theme.label("GCash", Font.BOLD, 30, Color.WHITE);
        logo.setIcon(new Icon() {
            public int getIconWidth() { return 34; }
            public int getIconHeight() { return 34; }
            public void paintIcon(Component comp, Graphics g0, int x, int y) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(3.5f));
                g.drawOval(x + 3, y + 3, 28, 28);
                g.setFont(Theme.bold(18));
                g.drawString("G", x + 11, y + 24);
                g.dispose();
            }
        });
        c.gridy = 0; c.insets = new Insets(0, 0, 14, 0); card.add(logo, c);
        c.gridy = 1; card.add(new QrCode(200), c);
        c.gridy = 2; c.insets = new Insets(0, 0, 4, 0); card.add(Theme.label("Scan QR Code to Pay", Font.BOLD, 15, Color.WHITE), c);
        c.gridy = 3; card.add(gcashAmount, c);
        JLabel help = new JLabel("<html><center>After paying, enter the reference no.<br>from your GCash receipt.</center></html>");
        help.setFont(Theme.regular(12));
        help.setForeground(new Color(0xDDE9FF));
        c.gridy = 4; card.add(help, c);
        return card;
    }

    private RoundedPanel infoCard(String icon, String title, String text) {
        RoundedPanel p = new RoundedPanel(new GridBagLayout());
        p.setBorder(Theme.pad(30, 26, 30, 26));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.insets = new Insets(0, 0, 14, 0);
        c.gridy = 0; p.add(new JLabel(Icons.of(icon, 64, Theme.PRIMARY, 1.6f)), c);
        c.gridy = 1; p.add(Theme.label(title, Font.BOLD, 17, Theme.TEXT), c);
        JLabel t = new JLabel("<html><div style='width:240px;text-align:center'>" + text + "</div></html>");
        t.setFont(Theme.regular(13));
        t.setForeground(Theme.MUTED);
        c.gridy = 2; p.add(t, c);
        return p;
    }

    private void updateAmount() {
        Order o = (Order) orderBox.getSelectedItem();
        amount.setText(o == null ? "No unpaid orders" : Formats.peso(o.getTotal()));
        gcashAmount.setText(o == null ? "" : "Amount: " + Formats.peso(o.getTotal()));
        pendingNote.setText(o != null && o.getPaymentStatus() == PaymentStatus.PENDING
                ? "A payment is awaiting staff confirmation. Submitting again will replace it." : " ");
    }

    private void submit() {
        Order o = (Order) orderBox.getSelectedItem();
        if (o == null) { Dialogs.info(this, "You have no unpaid orders."); return; }
        final Payment[] p = new Payment[1];
        if (Dialogs.run(this, () -> p[0] = payments.submit(o.getId(), method, reference.getText(), user))) {
            reference.setText("");
            String msg = method == PaymentMethod.GCASH
                    ? "Thank you! Your GCash payment was submitted and will be verified by our staff shortly."
                    : "Noted! Please pay " + Formats.peso(o.getTotal()) + " " + (method == PaymentMethod.CASH
                    ? "in cash upon delivery / pickup." : "by card at our counter.");
            Dialogs.info(this, msg);
            new ReceiptDialog(SwingUtilities.getWindowAncestor(this), orders.find(o.getId()), p[0]).setVisible(true);
            refresh();
        }
    }

    @Override public void refresh() {
        int keep = wantedId;
        wantedId = 0;
        load(() -> orders.search(user.getId(), null, false, null, 0), list -> {
            orderBox.removeAllItems();
            Order pick = null;
            for (Order o : list) {
                if (o.isPaid() || o.getStatus() == OrderStatus.CANCELLED) continue;
                orderBox.addItem(o);
                if (o.getId() == keep) pick = o;
            }
            if (pick != null) orderBox.setSelectedItem(pick);
            updateAmount();
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        orderBox = new JComboBox<>();
        amount = new JLabel();
        reference = new InputField("GCash reference no. (e.g. 1009283745)", "receipt", false);
        right = new JPanel();
        gcashAmount = new JLabel();
        pendingNote = new JLabel();
        left = new RoundedPanel();
        view1 = new JLabel();
        view2 = new JLabel();
        pm = new JLabel();
        optionGCASH = option(PaymentMethod.GCASH);
        optionCASH = option(PaymentMethod.CASH);
        optionCARD = option(PaymentMethod.CARD);
        confirm = AppButton.primary("Confirm Payment", "check").large();
        view3 = new JPanel();
        view4 = gcashCard();
        view5 = infoCard("cash", "Cash on Delivery / Pickup",
                "Pay in cash when our rider delivers your laundry or when you pick it up at the shop. "
                        + "Our staff will confirm the payment and give you a receipt.");
        view6 = infoCard("card", "Credit / Debit Card",
                "For your security we never ask for card numbers in the app. Tap or insert your card on our "
                        + "POS terminal at the counter - staff will confirm the payment.");
        this.setLayout(new BorderLayout(16, 0));
        orderBox.setPreferredSize(new Dimension(300, 36));
        amount.setText(Formats.peso(null));
        amount.setFont(Theme.font(Font.BOLD, 26));
        amount.setForeground(Theme.PRIMARY_DARK);
        reference.setPlaceholder("GCash reference no. (e.g. 1009283745)");
        reference.setPreferredSize(new Dimension(300, 42));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(340, 0));
        right.setLayout(new CardLayout());
        gcashAmount.setText("");
        gcashAmount.setFont(Theme.font(Font.BOLD, 18));
        gcashAmount.setForeground(Color.WHITE);
        pendingNote.setText(" ");
        pendingNote.setFont(Theme.font(Font.PLAIN, 12));
        pendingNote.setForeground(Theme.WARN_FG);
        left.setBorder(Theme.pad(22, 24, 22, 24));
        left.setLayout(new GridBagLayout());
        view1.setText("Order to pay");
        view1.setFont(Theme.font(Font.BOLD, 14));
        view1.setForeground(Theme.TEXT);
        view2.setText("Amount due");
        view2.setFont(Theme.font(Font.PLAIN, 12.5f));
        view2.setForeground(Theme.MUTED);
        pm.setText("Payment Method");
        pm.setFont(Theme.font(Font.BOLD, 16));
        pm.setForeground(Theme.TEXT);
        pm.setIcon(Icons.of("cash", 20, Theme.PRIMARY));
        confirm.setText("Confirm Payment");
        view3.setOpaque(false);
        view3.setPreferredSize(new Dimension(0, 0));
        view3.setMinimumSize(new Dimension(0, 0));
        view3.setLayout(new FlowLayout());
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        left.add(view1, gridBagConstraints);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        left.add(orderBox, gridBagConstraints);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        left.add(view2, gridBagConstraints);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        left.add(amount, gridBagConstraints);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        left.add(pm, gridBagConstraints);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        left.add(optionGCASH, gridBagConstraints);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        left.add(optionCASH, gridBagConstraints);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        left.add(optionCARD, gridBagConstraints);
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
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 4, 0);
        left.add(reference, gridBagConstraints);
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
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 4, 0);
        left.add(pendingNote, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        left.add(confirm, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        left.add(view3, gridBagConstraints);
        this.add(left, BorderLayout.CENTER);
        right.add(view4, "GCASH");
        right.add(view5, "CASH");
        right.add(view6, "CARD");
        this.add(right, BorderLayout.EAST);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<Order> orderBox;
    private JLabel amount;
    private InputField reference;
    private JPanel right;
    private JLabel gcashAmount;
    private JLabel pendingNote;
    private RoundedPanel left;
    private JLabel view1;
    private JLabel view2;
    private JLabel pm;
    private RoundedPanel optionGCASH;
    private RoundedPanel optionCASH;
    private RoundedPanel optionCARD;
    private AppButton confirm;
    private JPanel view3;
    private JPanel view4;
    private RoundedPanel view5;
    private RoundedPanel view6;
    // End of variables declaration//GEN-END:variables
}
