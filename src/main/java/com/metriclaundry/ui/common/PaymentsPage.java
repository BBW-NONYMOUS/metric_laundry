package com.metriclaundry.ui.common;

import com.metriclaundry.model.Payment;
import com.metriclaundry.model.PaymentStatus;
import com.metriclaundry.model.User;
import com.metriclaundry.service.OrderService;
import com.metriclaundry.service.PaymentService;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** "Manage Payments": verify GCash references, receive cash/card payments, reprint receipts. */
public class PaymentsPage extends Page {
    private final PaymentService service = new PaymentService();
    private final User actor;
    private final DefaultTableModel model = Tables.model("Receipt No.", "Order No.", "Customer", "Method", "Amount",
            "Reference No.", "Status", "Date", "Received By");

    private List<Payment> rows = new ArrayList<>();

    public PaymentsPage(User actor) {
        super("Manage Payments", "Verify GCash payments and receive cash / card at the counter");
        this.actor = actor;
        initComponents();
        bar.search(search, this::refresh);
        status.addActionListener(e -> refresh());
        bar.left(status);
        bar.right(receipt).right(confirm);
        Tables.badgeColumn(table, 6);
        Tables.widths(table, 130, 130, 150, 130, 90, 110, 90, 110, 120);
        confirm.addActionListener(e -> {
            Payment p = selected();
            if (p == null) return;
            if (p.getStatus() == PaymentStatus.PAID) { Dialogs.info(this, "This payment is already confirmed."); return; }
            String what = p.getMethod().label() + " " + Formats.peso(p.getAmount())
                    + (p.getReferenceNo() != null ? "\nGCash Ref: " + p.getReferenceNo() + "\n\nCheck that this reference appears in the shop's GCash app." : "");
            if (Dialogs.confirm(this, "Confirm payment for " + p.getOrderNo() + "?\n\n" + what)
                    && Dialogs.run(this, () -> service.confirm(p.getId(), actor))) refresh();
        });
        receipt.addActionListener(e -> {
            Payment p = selected();
            if (p != null) new ReceiptDialog(SwingUtilities.getWindowAncestor(this),
                    new OrderService().find(p.getOrderId()), p).setVisible(true);
        });
    }

    private Payment selected() {
        int v = table.getSelectedRow();
        if (v < 0) { Dialogs.info(this, "Select a payment first."); return null; }
        return rows.get(table.convertRowIndexToModel(v));
    }

    @Override public void refresh() {
        Object st = status.getSelectedItem();
        PaymentStatus s = st instanceof PaymentStatus ? (PaymentStatus) st : null;
        String kw = search.getText();
        load(() -> service.search(s, kw), list -> {
            rows = list;
            model.setRowCount(0);
            for (Payment p : list) {
                model.addRow(new Object[]{p.getReceiptNo(), p.getOrderNo(), p.getCustomerName(), p.getMethod().label(),
                        Formats.peso(p.getAmount()), p.getReferenceNo() == null ? "-" : p.getReferenceNo(), p.getStatus(),
                        Formats.date(p.getCreatedAt()), p.getReceivedByName() == null ? "-" : p.getReceivedByName()});
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        table = Tables.table(model);
        status = new JComboBox<>(new Object[]{"All payments", PaymentStatus.PENDING, PaymentStatus.PAID});
        search = new InputField("Search receipt, order, customer, reference", "search", false);
        bar = new Toolbar();
        receipt = AppButton.outline("View Receipt", "receipt");
        confirm = new AppButton("Confirm Payment", "check", AppButton.Variant.BLUE);
        card = new RoundedPanel();
        view1 = Theme.scroll(null);
        this.setLayout(new BorderLayout());
        status.setPreferredSize(new Dimension(160, 36));
        receipt.setText("View Receipt");
        confirm.setText("Confirm Payment");
        card.setLayout(new BorderLayout());
        this.add(bar, BorderLayout.NORTH);
        view1.setViewportView(table);
        card.add(view1);
        this.add(card, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTable table;
    private JComboBox<Object> status;
    private InputField search;
    private Toolbar bar;
    private AppButton receipt;
    private AppButton confirm;
    private RoundedPanel card;
    private JScrollPane view1;
    // End of variables declaration//GEN-END:variables
}
