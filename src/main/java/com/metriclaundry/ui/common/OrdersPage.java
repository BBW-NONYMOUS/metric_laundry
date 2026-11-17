package com.metriclaundry.ui.common;

import com.metriclaundry.model.Order;
import com.metriclaundry.model.OrderStatus;
import com.metriclaundry.model.User;
import com.metriclaundry.service.OrderService;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** "Manage Orders" for Staff and Admin: search, filter, walk-in orders and one-click status updates. */
public class OrdersPage extends Page {
    private final OrderService service = new OrderService();
    private final User actor;
    private final DefaultTableModel model = Tables.model("Order No.", "Customer", "Service", "Quantity", "Total",
            "Status", "Payment", "Date", "Action");

    private List<Order> rows = new ArrayList<>();

    public OrdersPage(User actor) {
        super("Manage Orders", "Receive laundry, update progress and release finished orders");
        this.actor = actor;
        initComponents();
        status.addItem("All statuses");
        for (OrderStatus s : OrderStatus.values()) status.addItem(s);
        status.addActionListener(e -> refresh());
        activeOnly.addActionListener(e -> refresh());
        bar.search(search, this::refresh);
        bar.left(status).left(activeOnly);
        bar.right(advance).right(add);
        Tables.badgeColumn(table, 5);
        Tables.badgeColumn(table, 6);
        Tables.widths(table, 150, 160, 110, 80, 90, 110, 90, 110, 80);
        ButtonColumn.install(table, 8, "Open", r -> open(rows.get(r)));
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0)
                    open(rows.get(table.convertRowIndexToModel(table.getSelectedRow())));
            }
        });
        add.addActionListener(e -> new NewOrderDialog(SwingUtilities.getWindowAncestor(this), actor, this::refresh).setVisible(true));
        advance.addActionListener(e -> advanceSelected());
    }

    private void open(Order o) {
        new OrderDetailsDialog(SwingUtilities.getWindowAncestor(this), o.getId(), actor, this::refresh).setVisible(true);
    }

    private void advanceSelected() {
        int[] sel = table.getSelectedRows();
        if (sel.length == 0) { Dialogs.info(this, "Select one or more orders first."); return; }
        List<String> errors = new ArrayList<>();
        int moved = 0;
        for (int v : sel) {
            Order o = rows.get(table.convertRowIndexToModel(v));
            try { service.advance(o.getId(), actor); moved++; }
            catch (Exception ex) { errors.add(ex.getMessage()); }
        }
        refresh();
        String msg = moved + " order(s) moved to the next step.";
        if (!errors.isEmpty()) msg += "\n\nNot updated:\n- " + String.join("\n- ", errors);
        Dialogs.info(this, msg);
    }

    @Override public void refresh() {
        Object st = status.getSelectedItem();
        OrderStatus s = st instanceof OrderStatus ? (OrderStatus) st : null;
        String kw = search.getText();
        boolean active = activeOnly.isSelected();
        load(() -> service.search(0, s, active, kw, 0), list -> {
            rows = list;
            model.setRowCount(0);
            for (Order o : list) {
                model.addRow(new Object[]{o.getOrderNo(), o.getCustomerName(), o.getServiceName(), o.quantityLabel(),
                        Formats.peso(o.getTotal()), o.getStatus(), o.getPaymentStatus(), Formats.date(o.getCreatedAt()), ""});
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        table = Tables.table(model);
        status = new JComboBox<>();
        activeOnly = new JCheckBox("Active orders only");
        search = new InputField("Search orders", "search", false);
        bar = new Toolbar();
        advance = AppButton.outline("Next Step", "arrowRight");
        add = AppButton.primary("New Walk-in Order", "plus");
        card = new RoundedPanel();
        view1 = Theme.scroll(null);
        this.setLayout(new BorderLayout());
        status.setPreferredSize(new Dimension(170, 36));
        activeOnly.setOpaque(false);
        advance.setText("Next Step");
        add.setText("New Walk-in Order");
        card.setLayout(new BorderLayout());
        this.add(bar, BorderLayout.NORTH);
        view1.setViewportView(table);
        card.add(view1);
        this.add(card, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTable table;
    private JComboBox<Object> status;
    private JCheckBox activeOnly;
    private InputField search;
    private Toolbar bar;
    private AppButton advance;
    private AppButton add;
    private RoundedPanel card;
    private JScrollPane view1;
    // End of variables declaration//GEN-END:variables
}
