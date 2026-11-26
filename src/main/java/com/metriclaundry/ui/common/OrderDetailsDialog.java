package com.metriclaundry.ui.common;

import com.metriclaundry.model.*;
import com.metriclaundry.service.OrderService;
import com.metriclaundry.service.PaymentService;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Full details of one order. The available actions depend on the role of the
 * logged-in user (customer: cancel / receipt; staff: advance status, payments; admin: + override).
 */
public class OrderDetailsDialog extends JDialog {
    private Order formOrder;
    private Payment formPayment;
    private final DefaultTableModel hm = Tables.model("Date & Time", "Status", "Updated By", "Remarks");
    private final OrderService orders = new OrderService();
    private final PaymentService payments = new PaymentService();
    private final int orderId;
    private final User actor;
    private final Runnable onChange;

    private Window owner;

    public OrderDetailsDialog(Window owner, int orderId, User actor, Runnable onChange) {
        super(owner, "Order Details", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        this.orderId = orderId;
        this.actor = actor;
        this.onChange = onChange == null ? () -> { } : onChange;
        this.formOrder = orders.find(orderId);
        this.formPayment = payments.findByOrder(orderId);
        initComponents();
        populateHistory();
        tracker.setStatus(formOrder.getStatus());
        revalidate();
        repaint();
        setSize(1120, 740);
        setLocationRelativeTo(owner);
    }

    private void reload() {
        formOrder = orders.find(orderId);
        formPayment = payments.findByOrder(orderId);
        getContentPane().removeAll();
        initComponents();
        tracker.setStatus(formOrder.getStatus());
        populateHistory();
        revalidate();
        repaint();
    }

    private JPanel buildOrderInfo(Order o, Payment p) {
        RoundedPanel info = new RoundedPanel(new GridLayout(0, 4, 12, 8));
        String[][] rows = {
                {"Customer", o.getCustomerName()}, {"Service", o.getServiceName()},
                {"Per kg", o.quantityLabel()}, {"Unit Price", Formats.peso(o.getUnitPrice())},
                {"Laundry subtotal", Formats.peso(o.getServiceSubtotal())}, {"Extras subtotal", Formats.peso(o.getAddOnTotal())},
                {"Total", Formats.peso(o.getTotal())}, {"Placed", Formats.dateTime(o.getCreatedAt())},
                {"Est. Completion", Formats.friendly(o.getEstimatedCompletion())},
                {"Payment", o.getPaymentStatus().label()},
                {"Confirmed paid", Formats.peso(o.getPaidAmount())}, {"Balance due", Formats.peso(o.getBalance())},
                {"Pending verification", Formats.peso(o.getPendingAmount())},
                {"Notes", o.getNotes() == null ? "-" : o.getNotes()}, {"", ""}};
        for (String[] r : rows) {
            info.add(Theme.label(r[0], Font.PLAIN, 12.5f, Theme.MUTED));
            info.add(Theme.label(r[1], Font.BOLD, 13, Theme.TEXT));
        }
        return info;
    }

    private JPanel actions(Order o, Payment p) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bar.setOpaque(false);
        boolean staff = actor.getRole() != Role.CUSTOMER;

        AppButton history = AppButton.outline("Transactions / Print", "print");
        history.addActionListener(e -> new TransactionHistoryDialog(this, orderId, actor, () -> { onChange.run(); reload(); }).setVisible(true));
        bar.add(history);
        if (p != null) {
            AppButton receipt = AppButton.outline("View Receipt", "receipt");
            receipt.addActionListener(e -> new ReceiptDialog(this, o, payments.findByOrder(orderId)).setVisible(true));
            bar.add(receipt);
        }
        if (staff && p != null && p.getStatus() == PaymentStatus.PENDING) {
            AppButton confirm = new AppButton("Confirm Payment", "check", AppButton.Variant.BLUE);
            confirm.addActionListener(e -> {
                if (Dialogs.confirm(this, "Confirm receipt of " + Formats.peso(p.getAmount()) + " via " + p.getMethod()
                        + "?\nReference: " + p.getReferenceNo()))
                    act(() -> payments.confirm(p.getId(), actor), "Payment confirmed.");
            });
            bar.add(confirm);
        }
        if (staff && o.getAvailableAmount().signum() > 0 && o.getStatus() != OrderStatus.CANCELLED) {
            AppButton record = new AppButton("Record Payment", "cash", AppButton.Variant.BLUE);
            record.addActionListener(e -> {
                PaymentPrompt.Result r = PaymentPrompt.ask(this, o);
                if (r != null) act(() -> payments.submit(orderId, r.method, r.reference, r.amount, actor), "Payment recorded.");
            });
            bar.add(record);
        }
        if (actor.getRole() == Role.ADMIN) {
            AppButton override = AppButton.outline("Override Status", "edit");
            override.addActionListener(e -> {
                JComboBox<OrderStatus> box = new JComboBox<>(OrderStatus.values());
                box.setSelectedItem(o.getStatus());
                JTextField reason = new JTextField();
                Object[] form = {"New status:", box, "Reason (required):", reason};
                if (JOptionPane.showConfirmDialog(this, form, "Override Status", JOptionPane.OK_CANCEL_OPTION)
                        == JOptionPane.OK_OPTION)
                    act(() -> orders.overrideStatus(orderId, (OrderStatus) box.getSelectedItem(), reason.getText(), actor),
                            "Status updated.");
            });
            bar.add(override);
        }
        boolean canCancel = o.getStatus().isActive() && o.getPaidAmount().signum() == 0 && o.getPendingAmount().signum() == 0
                && (staff || o.getStatus() == OrderStatus.PLACED);
        if (canCancel) {
            AppButton cancel = AppButton.danger("Cancel Order", "close");
            cancel.addActionListener(e -> {
                String reason = Dialogs.ask(this, "Reason for cancelling " + o.getOrderNo() + ":");
                if (reason != null) act(() -> orders.cancel(orderId, reason, actor), "Order cancelled.");
            });
            bar.add(cancel);
        }
        OrderStatus next = o.getStatus().next();
        if (staff && next != null && o.getStatus() != OrderStatus.CANCELLED) {
            AppButton adv = AppButton.primary("Move to " + next.label(), "arrowRight");
            adv.addActionListener(e -> act(() -> orders.advance(orderId, actor), null));
            bar.add(adv);
        }
        AppButton close = new AppButton("Close", AppButton.Variant.GHOST);
        close.addActionListener(e -> dispose());
        bar.add(close);
        return bar;
    }

    private void act(Runnable r, String okMessage) {
        if (Dialogs.run(this, r)) {
            if (okMessage != null) Dialogs.info(this, okMessage);
            onChange.run();
            reload();
        }
    }

    /** Small form used by staff to record a counter payment. */
    public static final class PaymentPrompt {
        public static final class Result {
            public final PaymentMethod method;
            public final String reference;
            public java.math.BigDecimal amount;
            Result(PaymentMethod method, String reference) { this.method = method; this.reference = reference; }
        }

    private PaymentPrompt() { }

        public static Result ask(Component parent, Order o) {
            JComboBox<PaymentMethod> m = new JComboBox<>(PaymentMethod.values());
            m.setSelectedItem(PaymentMethod.CASH);
            JTextField ref = new JTextField();
            JTextField amount = new JTextField(o.getAvailableAmount().toPlainString());
            Object[] form = {"Amount received:", amount, "Order " + o.getOrderNo() + "  -  Amount due: " + Formats.peso(o.getBalance()), " ",
                    "Payment method:", m, "GCash reference no. (GCash only):", ref};
            if (JOptionPane.showConfirmDialog(parent, form, "Record Payment", JOptionPane.OK_CANCEL_OPTION)
                    != JOptionPane.OK_OPTION) return null;
            try {
                Result result = new Result((PaymentMethod) m.getSelectedItem(), ref.getText());
                result.amount = new java.math.BigDecimal(amount.getText().trim());
                return result;
            } catch (NumberFormatException ex) { Dialogs.info(parent, "Enter a valid payment amount."); return null; }
        }
    }

    private void populateHistory() {
        hm.setRowCount(0);
        for (StatusHistory h : orders.history(orderId))
            hm.addRow(new Object[]{Formats.dateTime(h.getChangedAt()), h.getStatus().label(), h.getChangedByName(), h.getRemarks()});
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        root = new JPanel();
        head = new JPanel();
        view1 = new JLabel();
        view2 = new Badge(formOrder.getStatus());
        view3 = new Badge(formOrder.getPaymentStatus());
        center = new JPanel();
        trackCard = new RoundedPanel();
        tracker = new StepTracker();
        view4 = new JPanel();
        info = buildOrderInfo(formOrder, formPayment);
        view5 = new JPanel();
        ht = Tables.table(hm);
        sp = Theme.scroll(null);
        view6 = actions(formOrder, formPayment);
        getContentPane().setLayout(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(Theme.pad(18, 20, 16, 20));
        root.setLayout(new BorderLayout(0, 12));
        head.setOpaque(false);
        head.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        view1.setText("Order " + formOrder.getOrderNo());
        view1.setFont(Theme.font(Font.BOLD, 22));
        view1.setForeground(Theme.TEXT);
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        trackCard.setLayout(new BorderLayout());
        tracker.setPreferredSize(new Dimension(700, formOrder.getStatus() == OrderStatus.CANCELLED ? 112 : 96));
        view4.setOpaque(false);
        view4.setPreferredSize(new Dimension(0, 10));
        view4.setMinimumSize(new Dimension(0, 10));
        view4.setMaximumSize(new Dimension(32767, 10));
        view4.setLayout(new FlowLayout());
        view5.setOpaque(false);
        view5.setPreferredSize(new Dimension(0, 10));
        view5.setMinimumSize(new Dimension(0, 10));
        view5.setMaximumSize(new Dimension(32767, 10));
        view5.setLayout(new FlowLayout());
        ht.setRowHeight(34);
        sp.setPreferredSize(new Dimension(600, 150));
        head.add(view1);
        head.add(view2);
        head.add(view3);
        root.add(head, BorderLayout.NORTH);
        trackCard.add(tracker);
        center.add(trackCard);
        center.add(view4);
        center.add(info);
        center.add(view5);
        sp.setViewportView(ht);
        center.add(sp);
        root.add(center, BorderLayout.CENTER);
        root.add(view6, BorderLayout.SOUTH);
        getContentPane().add(root, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel root;
    private JPanel head;
    private JLabel view1;
    private Badge view2;
    private Badge view3;
    private JPanel center;
    private RoundedPanel trackCard;
    private StepTracker tracker;
    private JPanel view4;
    private JPanel info;
    private JPanel view5;
    private JTable ht;
    private JScrollPane sp;
    private JPanel view6;
    // End of variables declaration//GEN-END:variables
}
