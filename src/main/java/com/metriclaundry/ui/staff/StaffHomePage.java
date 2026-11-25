package com.metriclaundry.ui.staff;

import com.metriclaundry.dao.ReportDAO;
import com.metriclaundry.model.DashboardStats;
import com.metriclaundry.model.Order;
import com.metriclaundry.model.User;
import com.metriclaundry.service.OrderService;
import com.metriclaundry.ui.common.*;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Staff home: today's counters and the live work queue (all active orders, oldest first). */
public class StaffHomePage extends Page {
    private final User user;
    private final OrderService orders = new OrderService();

    private final DefaultTableModel model = Tables.model("Order No.", "Customer", "Service", "Per kg", "Status",
            "Payment", "Due", "Action");

    private List<Order> rows = new ArrayList<>();

    public StaffHomePage(User user) {
        super("Good day, " + user.getFirstName() + "!", "Here is today's laundry queue");
        this.user = user;
        initComponents();
        walkIn.addActionListener(e -> new NewOrderDialog(SwingUtilities.getWindowAncestor(this), user, this::refresh).setVisible(true));
        Tables.badgeColumn(table, 4);
        Tables.badgeColumn(table, 5);
        Tables.widths(table, 150, 140, 110, 80, 110, 90, 170, 90);
        ButtonColumn.install(table, 7, "Update", r ->
                new OrderDetailsDialog(SwingUtilities.getWindowAncestor(this), rows.get(r).getId(), user, this::refresh)
                        .setVisible(true));
    }

    @Override public void refresh() {
        load(() -> new Object[]{new ReportDAO().stats(), orders.search(0, null, true, null, 0)}, data -> {
            DashboardStats s = (DashboardStats) data[0];
            newOrders.setValue(String.valueOf(s.pendingOrders));
            inProcess.setValue(String.valueOf(s.inProcessOrders));
            ready.setValue(String.valueOf(s.readyOrders));
            toVerify.setValue(String.valueOf(s.unpaidPayments));
            @SuppressWarnings("unchecked") List<Order> list = new ArrayList<>((List<Order>) data[1]);
            list.sort((a, b) -> {
                if (a.getEstimatedCompletion() == null || b.getEstimatedCompletion() == null) return 0;
                return a.getEstimatedCompletion().compareTo(b.getEstimatedCompletion());
            });
            rows = list;
            model.setRowCount(0);
            for (Order o : list)
                model.addRow(new Object[]{o.getOrderNo(), o.getCustomerName(), o.getServiceName(), o.quantityLabel(),
                        o.getStatus(), o.getPaymentStatus(), Formats.friendly(o.getEstimatedCompletion()), ""});
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        newOrders = new StatCard("orders", "New Orders", new Color(0x607D8B));
        inProcess = new StatCard("washer", "In Process", new Color(0xE0A800));
        ready = new StatCard("bag", "Ready for Pickup", new Color(0x1C5DB6));
        toVerify = new StatCard("peso", "Payments to Verify", Theme.PRIMARY);
        table = Tables.table(model);
        stats = new JPanel();
        queue = new RoundedPanel();
        head = new JPanel();
        t = new JLabel();
        walkIn = AppButton.primary("New Walk-in Order", "plus").small();
        view1 = Theme.scroll(null);
        this.setLayout(new BorderLayout(0, 16));
        stats.setOpaque(false);
        stats.setLayout(new GridLayout(1, 4, 14, 0));
        queue.setLayout(new BorderLayout(0, 10));
        head.setOpaque(false);
        head.setLayout(new BorderLayout());
        t.setText("Work Queue");
        t.setFont(Theme.font(Font.BOLD, 16));
        t.setForeground(Theme.TEXT);
        t.setIcon(Icons.of("orders", 18, Theme.PRIMARY_DARK));
        walkIn.setText("New Walk-in Order");
        stats.add(newOrders);
        stats.add(inProcess);
        stats.add(ready);
        stats.add(toVerify);
        this.add(stats, BorderLayout.NORTH);
        head.add(t, BorderLayout.WEST);
        head.add(walkIn, BorderLayout.EAST);
        queue.add(head, BorderLayout.NORTH);
        view1.setViewportView(table);
        queue.add(view1, BorderLayout.CENTER);
        this.add(queue, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private StatCard newOrders;
    private StatCard inProcess;
    private StatCard ready;
    private StatCard toVerify;
    private JTable table;
    private JPanel stats;
    private RoundedPanel queue;
    private JPanel head;
    private JLabel t;
    private AppButton walkIn;
    private JScrollPane view1;
    // End of variables declaration//GEN-END:variables
}
