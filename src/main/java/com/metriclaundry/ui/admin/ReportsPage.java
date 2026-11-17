package com.metriclaundry.ui.admin;

import com.metriclaundry.dao.ReportDAO;
import com.metriclaundry.ui.common.Page;
import com.metriclaundry.ui.common.Tables;
import com.metriclaundry.ui.common.Toolbar;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Sales report by date range and by service, with CSV export (opens in Excel). */
public class ReportsPage extends Page {
    private final ReportDAO dao = new ReportDAO();

    private final DefaultTableModel model = Tables.model("Service", "Orders", "Quantity", "Paid Sales", "Share");

    private List<Object[]> data = new ArrayList<>();

    public ReportsPage() {
        super("Reports", "Sales performance by date range and service");

        initComponents();
        bar.left(Theme.label("From", Font.BOLD, 13, Theme.TEXT)).left(from)
           .left(Theme.label("To", Font.BOLD, 13, Theme.TEXT)).left(to);
        bar.left(today).left(week).left(month).right(run).right(export);
        today.addActionListener(e -> range(LocalDate.now(), LocalDate.now()));
        week.addActionListener(e -> range(LocalDate.now().minusDays(6), LocalDate.now()));
        month.addActionListener(e -> range(LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        run.addActionListener(e -> refresh());
        export.addActionListener(e -> export());
    }

    private void range(LocalDate a, LocalDate b) {
        from.setValue(toDate(a));
        to.setValue(toDate(b));
        refresh();
    }

    private static JSpinner dateSpinner(LocalDate d) {
        JSpinner s = new JSpinner(new SpinnerDateModel(toDate(d), null, null, java.util.Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "MMM d, yyyy"));
        s.setPreferredSize(new Dimension(140, 34));
        return s;
    }

    private static Date toDate(LocalDate d) { return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()); }

    private static LocalDate value(JSpinner s) {
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override public void refresh() {
        LocalDate a = value(from), b = value(to);
        if (b.isBefore(a)) { Dialogs.info(this, "\"To\" date must be on or after \"From\" date."); return; }
        load(() -> dao.salesByService(a, b), rows -> {
            data = rows;
            int count = 0;
            BigDecimal total = BigDecimal.ZERO;
            for (Object[] r : rows) { count += (Integer) r[2]; total = total.add((BigDecimal) r[4]); }
            orders.setValue(String.valueOf(count));
            sales.setValue(Formats.peso(total));
            average.setValue(count == 0 ? Formats.peso(null) : Formats.peso(total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)));
            model.setRowCount(0);
            for (Object[] r : rows) {
                BigDecimal s = (BigDecimal) r[4];
                String share = total.signum() == 0 ? "0%" : s.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP) + "%";
                String unit = "PC".equals(r[1]) ? " pcs" : " kg";
                model.addRow(new Object[]{r[0], r[2], ((BigDecimal) r[3]).stripTrailingZeros().toPlainString() + unit,
                        Formats.peso(s), share});
            }
        });
    }

    private void export() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("metric-laundry-sales_" + value(from) + "_to_" + value(to) + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter w = new PrintWriter(fc.getSelectedFile(), StandardCharsets.UTF_8)) {
            w.println("Metric Laundry Sales Report," + value(from) + " to " + value(to));
            w.println("Service,Unit,Orders,Quantity,Paid Sales");
            for (Object[] r : data)
                w.println("\"" + r[0] + "\"," + r[1] + "," + r[2] + "," + ((BigDecimal) r[3]).toPlainString() + "," + ((BigDecimal) r[4]).toPlainString());
            Dialogs.info(this, "Report exported to:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (Exception e) { Dialogs.error(this, e); }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        from = dateSpinner(LocalDate.now().withDayOfMonth(1));
        to = dateSpinner(LocalDate.now());
        orders = new StatCard("orders", "Orders", Theme.PRIMARY);
        sales = new StatCard("peso", "Paid Sales", Theme.PRIMARY);
        average = new StatCard("chart", "Average per Order", Theme.PRIMARY);
        table = Tables.table(model);
        bar = new Toolbar();
        today = AppButton.outline("Today", null).small();
        week = AppButton.outline("Last 7 days", null).small();
        month = AppButton.outline("This Month", null).small();
        run = AppButton.primary("Generate", "chart");
        export = AppButton.outline("Export CSV", "download");
        top = new JPanel();
        cards = new JPanel();
        card = new RoundedPanel();
        view1 = new JLabel();
        view2 = Theme.scroll(null);
        this.setLayout(new BorderLayout(0, 14));
        today.setText("Today");
        week.setText("Last 7 days");
        month.setText("This Month");
        run.setText("Generate");
        export.setText("Export CSV");
        top.setOpaque(false);
        top.setLayout(new BorderLayout(0, 4));
        cards.setOpaque(false);
        cards.setLayout(new GridLayout(1, 3, 14, 0));
        card.setLayout(new BorderLayout(0, 8));
        view1.setText("Sales by Service");
        view1.setFont(Theme.font(Font.BOLD, 15));
        view1.setForeground(Theme.TEXT);
        top.add(bar, BorderLayout.NORTH);
        cards.add(orders);
        cards.add(sales);
        cards.add(average);
        top.add(cards, BorderLayout.CENTER);
        this.add(top, BorderLayout.NORTH);
        card.add(view1, BorderLayout.NORTH);
        view2.setViewportView(table);
        card.add(view2, BorderLayout.CENTER);
        this.add(card, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JSpinner from;
    private JSpinner to;
    private StatCard orders;
    private StatCard sales;
    private StatCard average;
    private JTable table;
    private Toolbar bar;
    private AppButton today;
    private AppButton week;
    private AppButton month;
    private AppButton run;
    private AppButton export;
    private JPanel top;
    private JPanel cards;
    private RoundedPanel card;
    private JLabel view1;
    private JScrollPane view2;
    // End of variables declaration//GEN-END:variables
}
