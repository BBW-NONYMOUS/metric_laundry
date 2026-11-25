package com.metriclaundry.ui.customer;

import com.metriclaundry.model.Order;
import com.metriclaundry.model.OrderStatus;
import com.metriclaundry.model.User;
import com.metriclaundry.service.OrderService;
import com.metriclaundry.ui.common.Page;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import java.awt.*;

/** "Order Tracking - Real-Time Status of Your Laundry". Auto-refreshes every 30 seconds while visible. */
public class TrackOrderPage extends Page {
    private final User user;
    private final OrderService orders = new OrderService();

    private int wantedId;
    private boolean updatingPicker;

    public TrackOrderPage(User user) {
        super("Order Tracking", "Real-time status of your laundry");
        this.user = user;
        initComponents();
        picker.addActionListener(e -> { if (!updatingPicker) showSelected(); });
        reload.addActionListener(e -> refresh());
        back.addActionListener(e -> frame.navigate("home"));
        new Timer(30_000, e -> { if (isShowing()) refresh(); }).start();
    }

    /** Called from other pages to open a specific order. */
    public void show(int orderId) { this.wantedId = orderId; }

    private static Icon circleIcon(String icon) {
        return new Icon() {
            public int getIconWidth() { return 84; }
            public int getIconHeight() { return 84; }
            public void paintIcon(Component c, Graphics g0, int x, int y) {
                Graphics2D g = (Graphics2D) g0.create();
                Theme.antialias(g);
                g.setColor(Theme.PRIMARY);
                g.fillOval(x, y, 84, 84);
                Icons.of(icon, 44, Color.WHITE, 2f).paintIcon(c, g, x + 20, y + 20);
                g.dispose();
            }
        };
    }

    private void showSelected() {
        Order o = (Order) picker.getSelectedItem();
        if (o == null) return;
        tracker.setStatus(o.getStatus());
        orderNo.setText(":  " + o.getOrderNo());
        service.setText(":  " + o.getServiceName());
        quantity.setText(":  " + o.quantityLabel());
        total.setText(":  " + Formats.peso(o.getTotal()));
        status.setText(o.getStatus().label());
        status.setForeground(o.getStatus() == OrderStatus.CANCELLED ? Theme.DANGER : Theme.PRIMARY_DARK);
        String ic;
        switch (o.getStatus()) {
            case DRYING: ic = "dry"; break;
            case READY: ic = "bag"; break;
            case COMPLETED: ic = "check"; break;
            case CANCELLED: ic = "close"; break;
            case PLACED: ic = "orders"; break;
            default: ic = "washer";
        }
        statusIcon.setIcon(circleIcon(ic));
        eta.setText(o.getStatus() == OrderStatus.COMPLETED ? "Done - thank you!"
                : o.getStatus() == OrderStatus.CANCELLED ? "-" : Formats.friendly(o.getEstimatedCompletion()));
    }

    @Override public void refresh() {
        Order current = (Order) picker.getSelectedItem();
        int keep = wantedId != 0 ? wantedId : current == null ? 0 : current.getId();
        wantedId = 0;
        load(() -> orders.search(user.getId(), null, false, null, 50), list -> {
            updatingPicker = true;
            picker.removeAllItems();
            Order pick = null;
            for (Order o : list) {
                picker.addItem(o);
                if (o.getId() == keep) pick = o;
            }
            if (pick == null) pick = list.stream().filter(o -> o.getStatus().isActive()).findFirst()
                    .orElse(list.isEmpty() ? null : list.get(0));
            picker.setSelectedItem(pick);
            updatingPicker = false;
            if (pick != null) showSelected();
        });
    }

    private static JLabel val() { return Theme.label("-", Font.BOLD, 16, Theme.TEXT); }

    private static JPanel kv(String k, JLabel v) {
        JPanel p = new JPanel(new GridLayout(1, 2));
        p.setOpaque(false);
        p.add(Theme.label(k, Font.BOLD, 16, Theme.TEXT));
        p.add(v);
        return p;
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        picker = new JComboBox<>();
        tracker = new StepTracker();
        orderNo = new JLabel();
        service = new JLabel();
        quantity = new JLabel();
        total = new JLabel();
        status = new JLabel();
        eta = new JLabel();
        statusIcon = new JLabel();
        top = new JPanel();
        view1 = new JLabel();
        reload = AppButton.outline("Refresh", "refresh");
        card = new RoundedPanel();
        details = new RoundedPanel(14, Theme.PRIMARY_LIGHT).shadow(false).border(null);
        left = new JPanel();
        view2 = new JPanel();
        view3 = new JLabel();
        view4 = new JPanel();
        view5 = new JLabel();
        view6 = new JPanel();
        view7 = new JLabel();
        view8 = new JPanel();
        view9 = new JLabel();
        right = new JPanel();
        st = new JPanel();
        view10 = new JLabel();
        view11 = new JLabel();
        back = AppButton.outline("Back to Dashboard", "arrowLeft");
        bw = new JPanel();
        wrap = new JPanel();
        this.setLayout(new BorderLayout(0, 14));
        picker.setPreferredSize(new Dimension(320, 36));
        orderNo.setText("-");
        orderNo.setFont(Theme.font(Font.BOLD, 16));
        orderNo.setForeground(Theme.TEXT);
        service.setText("-");
        service.setFont(Theme.font(Font.BOLD, 16));
        service.setForeground(Theme.TEXT);
        quantity.setText("-");
        quantity.setFont(Theme.font(Font.BOLD, 16));
        quantity.setForeground(Theme.TEXT);
        total.setText("-");
        total.setFont(Theme.font(Font.BOLD, 16));
        total.setForeground(Theme.TEXT);
        status.setText("-");
        status.setFont(Theme.font(Font.BOLD, 30));
        status.setForeground(Theme.PRIMARY_DARK);
        eta.setText("-");
        eta.setFont(Theme.font(Font.BOLD, 16));
        eta.setForeground(Theme.TEXT);
        eta.setIcon(Icons.of("clock", 18, Theme.TEXT));
        statusIcon.setIcon(circleIcon("washer"));
        top.setOpaque(false);
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        view1.setText("Select order:");
        view1.setFont(Theme.font(Font.BOLD, 13));
        view1.setForeground(Theme.TEXT);
        reload.setText("Refresh");
        card.setBorder(Theme.pad(24, 24, 22, 24));
        card.setLayout(new BorderLayout(0, 18));
        details.setBorder(Theme.pad(20, 26, 20, 26));
        details.setLayout(new GridLayout(1, 2, 20, 0));
        left.setOpaque(false);
        left.setLayout(new GridLayout(4, 1, 0, 6));
        view2.setOpaque(false);
        view2.setLayout(new GridLayout(1, 2));
        view3.setText("Order No.");
        view3.setFont(Theme.font(Font.BOLD, 16));
        view3.setForeground(Theme.TEXT);
        view4.setOpaque(false);
        view4.setLayout(new GridLayout(1, 2));
        view5.setText("Service");
        view5.setFont(Theme.font(Font.BOLD, 16));
        view5.setForeground(Theme.TEXT);
        view6.setOpaque(false);
        view6.setLayout(new GridLayout(1, 2));
        view7.setText("Per kg");
        view7.setFont(Theme.font(Font.BOLD, 16));
        view7.setForeground(Theme.TEXT);
        view8.setOpaque(false);
        view8.setLayout(new GridLayout(1, 2));
        view9.setText("Total");
        view9.setFont(Theme.font(Font.BOLD, 16));
        view9.setForeground(Theme.TEXT);
        right.setOpaque(false);
        right.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.PRIMARY_SOFT),
                Theme.pad(0, 24, 0, 0)));
        right.setLayout(new BorderLayout(18, 0));
        st.setOpaque(false);
        st.setLayout(new GridLayout(4, 1));
        view10.setText("Current Status:");
        view10.setFont(Theme.font(Font.BOLD, 15));
        view10.setForeground(Theme.TEXT);
        view11.setText("Estimated Completion:");
        view11.setFont(Theme.font(Font.PLAIN, 13.5f));
        view11.setForeground(Theme.TEXT);
        back.setText("Back to Dashboard");
        bw.setOpaque(false);
        bw.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.setLayout(new BorderLayout());
        top.add(view1);
        top.add(picker);
        top.add(reload);
        this.add(top, BorderLayout.NORTH);
        card.add(tracker, BorderLayout.NORTH);
        view2.add(view3);
        view2.add(orderNo);
        left.add(view2);
        view4.add(view5);
        view4.add(service);
        left.add(view4);
        view6.add(view7);
        view6.add(quantity);
        left.add(view6);
        view8.add(view9);
        view8.add(total);
        left.add(view8);
        details.add(left);
        right.add(statusIcon, BorderLayout.WEST);
        st.add(view10);
        st.add(status);
        st.add(view11);
        st.add(eta);
        right.add(st);
        details.add(right);
        card.add(details, BorderLayout.CENTER);
        bw.add(back);
        card.add(bw, BorderLayout.SOUTH);
        wrap.add(card, BorderLayout.NORTH);
        this.add(wrap, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<Order> picker;
    private StepTracker tracker;
    private JLabel orderNo;
    private JLabel service;
    private JLabel quantity;
    private JLabel total;
    private JLabel status;
    private JLabel eta;
    private JLabel statusIcon;
    private JPanel top;
    private JLabel view1;
    private AppButton reload;
    private RoundedPanel card;
    private RoundedPanel details;
    private JPanel left;
    private JPanel view2;
    private JLabel view3;
    private JPanel view4;
    private JLabel view5;
    private JPanel view6;
    private JLabel view7;
    private JPanel view8;
    private JLabel view9;
    private JPanel right;
    private JPanel st;
    private JLabel view10;
    private JLabel view11;
    private AppButton back;
    private JPanel bw;
    private JPanel wrap;
    // End of variables declaration//GEN-END:variables
}
