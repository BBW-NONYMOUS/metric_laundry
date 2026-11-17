package com.metriclaundry.ui.admin;

import com.metriclaundry.dao.ReportDAO;
import com.metriclaundry.model.DashboardStats;
import com.metriclaundry.model.Order;
import com.metriclaundry.model.User;
import com.metriclaundry.service.OrderService;
import com.metriclaundry.ui.common.OrderDetailsDialog;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Admin "Dashboard - Monitor and manage the laundry system". */
public class AdminHomePage extends Page {
    private final User user;

    private final DefaultTableModel model = Tables.model("Order No.", "Service", "Status", "Date");

    private List<Order> rows = new ArrayList<>();

    public AdminHomePage(User user) {
        super("Dashboard", "Monitor and manage the laundry system");
        this.user = user;
        initComponents();
        Tables.badgeColumn(table, 2);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (e.getClickCount() == 2 && r >= 0)
                    new OrderDetailsDialog(SwingUtilities.getWindowAncestor(AdminHomePage.this),
                            rows.get(table.convertRowIndexToModel(r)).getId(), user, AdminHomePage.this::refresh).setVisible(true);
            }
        });
    }

    private static JPanel header(String title, String icon, Runnable viewAll) {
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);
        JLabel t = Theme.label(title, Font.BOLD, 15, Theme.TEXT);
        t.setIcon(Icons.of(icon, 18, Theme.PRIMARY_DARK));
        h.add(t, BorderLayout.WEST);
        if (viewAll != null) {
            JLabel l = Theme.label("View All  \u2192", Font.BOLD, 12, Theme.TEXT);
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            l.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { viewAll.run(); } });
            h.add(l, BorderLayout.EAST);
        }
        return h;
    }

    @Override public void refresh() {
        ReportDAO dao = new ReportDAO();
        load(() -> new Object[]{dao.stats(), dao.dailySales(7), new OrderService().search(0, null, false, null, 8)}, data -> {
            DashboardStats s = (DashboardStats) data[0];
            totalOrders.setValue(String.valueOf(s.totalOrders));
            totalCustomers.setValue(String.valueOf(s.totalCustomers));
            revenue.setValue(Formats.peso(s.totalRevenue));
            completed.setValue(String.valueOf(s.completedOrders));

            @SuppressWarnings("unchecked") Map<LocalDate, BigDecimal> daily = (Map<LocalDate, BigDecimal>) data[1];
            List<String> labels = new ArrayList<>();
            List<Double> values = new ArrayList<>();
            DateTimeFormatter f = DateTimeFormatter.ofPattern("EEE");
            daily.forEach((d, v) -> { labels.add(f.format(d)); values.add(v.doubleValue()); });
            chart.setData(labels, values);

            @SuppressWarnings("unchecked") List<Order> list = (List<Order>) data[2];
            rows = list;
            model.setRowCount(0);
            for (Order o : list)
                model.addRow(new Object[]{o.getOrderNo(), o.getServiceName(), o.getStatus(), Formats.date(o.getCreatedAt())});
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        totalOrders = new StatCard("cart", "Total Orders", Theme.PRIMARY);
        totalCustomers = new StatCard("users", "Total Customers", Theme.PRIMARY);
        revenue = new StatCard("peso", "Total Revenue", Theme.PRIMARY);
        completed = new StatCard("check", "Completed Orders", Theme.PRIMARY);
        chart = new LineChart();
        table = Tables.table(model);
        stats = new JPanel();
        bottom = new JPanel();
        sales = new RoundedPanel();
        view1 = header("Sales Overview (last 7 days)", "chart", null);
        recent = new RoundedPanel();
        view2 = header("Recent Orders", "orders", () -> frame.navigate("orders"));
        view3 = Theme.scroll(null);
        this.setLayout(new BorderLayout(0, 16));
        table.setRowHeight(36);
        stats.setOpaque(false);
        stats.setLayout(new GridLayout(1, 4, 14, 0));
        bottom.setOpaque(false);
        bottom.setLayout(new GridLayout(1, 2, 16, 0));
        sales.setLayout(new BorderLayout(0, 8));
        recent.setLayout(new BorderLayout(0, 8));
        stats.add(totalOrders);
        stats.add(totalCustomers);
        stats.add(revenue);
        stats.add(completed);
        this.add(stats, BorderLayout.NORTH);
        sales.add(view1, BorderLayout.NORTH);
        sales.add(chart, BorderLayout.CENTER);
        bottom.add(sales);
        recent.add(view2, BorderLayout.NORTH);
        view3.setViewportView(table);
        recent.add(view3, BorderLayout.CENTER);
        bottom.add(recent);
        this.add(bottom, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private StatCard totalOrders;
    private StatCard totalCustomers;
    private StatCard revenue;
    private StatCard completed;
    private LineChart chart;
    private JTable table;
    private JPanel stats;
    private JPanel bottom;
    private RoundedPanel sales;
    private JPanel view1;
    private RoundedPanel recent;
    private JPanel view2;
    private JScrollPane view3;
    // End of variables declaration//GEN-END:variables
}
