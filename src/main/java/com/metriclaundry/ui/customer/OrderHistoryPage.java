package com.metriclaundry.ui.customer;

import com.metriclaundry.model.Order;
import com.metriclaundry.model.OrderStatus;
import com.metriclaundry.model.User;
import com.metriclaundry.service.OrderService;
import com.metriclaundry.ui.common.OrderDetailsDialog;
import com.metriclaundry.ui.common.Page;
import com.metriclaundry.ui.common.Tables;
import com.metriclaundry.ui.common.Toolbar;
import com.metriclaundry.ui.components.ButtonColumn;
import com.metriclaundry.ui.components.InputField;
import com.metriclaundry.ui.components.RoundedPanel;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** "Order History - Your past and current orders". */
public class OrderHistoryPage extends Page {
    private final User user;
    private final OrderService orders = new OrderService();
    private final DefaultTableModel model = Tables.model("Order No.", "Service", "Per kg", "Total", "Status", "Payment", "Date", "Action");

    private List<Order> rows = new ArrayList<>();

    public OrderHistoryPage(User user) {
        super("Order History", "Your past and current orders");
        this.user = user;
        initComponents();
        bar.search(search, this::refresh);
        status.addItem("All statuses");
        for (OrderStatus s : OrderStatus.values()) status.addItem(s);
        status.addActionListener(e -> refresh());
        bar.left(status);
        Tables.badgeColumn(table, 4);
        Tables.badgeColumn(table, 5);
        ButtonColumn.install(table, 7, "View Details", r ->
                new OrderDetailsDialog(SwingUtilities.getWindowAncestor(this), rows.get(r).getId(), user, this::refresh)
                        .setVisible(true));
    }

    @Override public void refresh() {
        Object st = status.getSelectedItem();
        OrderStatus s = st instanceof OrderStatus ? (OrderStatus) st : null;
        String kw = search.getText();
        load(() -> orders.search(user.getId(), s, false, kw, 0), list -> {
            rows = list;
            model.setRowCount(0);
            for (Order o : list)
                model.addRow(new Object[]{o.getOrderNo(), o.getServiceName(), o.quantityLabel(), Formats.peso(o.getTotal()),
                        o.getStatus(), o.getPaymentStatus(), Formats.date(o.getCreatedAt()), ""});
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        table = Tables.table(model);
        status = new JComboBox<>();
        search = new InputField("Search order no. or service", "search", false);
        bar = new Toolbar();
        card = new RoundedPanel();
        view1 = Theme.scroll(null);
        this.setLayout(new BorderLayout());
        status.setPreferredSize(new Dimension(170, 36));
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
    private RoundedPanel card;
    private JScrollPane view1;
    // End of variables declaration//GEN-END:variables
}
