package com.metriclaundry.ui.admin;

import com.metriclaundry.model.LaundryService;
import com.metriclaundry.service.CatalogService;
import com.metriclaundry.ui.common.Page;
import com.metriclaundry.ui.common.Tables;
import com.metriclaundry.ui.common.Toolbar;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** "Manage Services - Add, Edit, or Update Services" (price list). */
public class ServicesPage extends Page {
    private final CatalogService catalog = new CatalogService();
    private final DefaultTableModel model = Tables.model("Service", "Price", "Turnaround", "Description", "Status", "Edit", "Delete");

    private List<LaundryService> rows = new ArrayList<>();

    public ServicesPage() {
        super("Manage Services", "Add, edit or update services and prices");

        initComponents();
        bar.left(hint);
        AppButton extras = AppButton.outline("Add-ons / Prices", "plus");
        extras.addActionListener(e -> Dialogs.run(this, () -> new AddOnCatalogDialog(SwingUtilities.getWindowAncestor(this), com.metriclaundry.util.Session.get()).setVisible(true)));
        bar.right(extras).right(add);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, false, r, c);
                LaundryService svc = rows.get(t.convertRowIndexToModel(r));
                l.setIcon(Icons.of(ServiceVisuals.icon(svc), 22, new Color(0x3C78C8)));
                l.setIconTextGap(12);
                l.setFont(Theme.bold(13.5f));
                l.setBorder(Theme.pad(0, 14, 0, 8));
                return l;
            }
        });
        Tables.badgeColumn(table, 4);
        table.getColumnModel().getColumn(5).setCellRenderer(iconCell("edit", Theme.PRIMARY));
        table.getColumnModel().getColumn(6).setCellRenderer(iconCell("trash", Theme.DANGER));
        Tables.widths(table, 200, 110, 110, 260, 90, 60, 60);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint()), c = table.columnAtPoint(e.getPoint());
                if (r < 0 || c < 0) return;
                LaundryService s = rows.get(table.convertRowIndexToModel(r));
                int col = table.convertColumnIndexToModel(c);
                if (col == 5 || e.getClickCount() == 2) edit(s);
                else if (col == 6) delete(s);
            }
        });
        add.addActionListener(e -> edit(null));
    }

    private static DefaultTableCellRenderer iconCell(String icon, Color color) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int row, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, "", s, false, row, c);
                l.setIcon(new Icon() {
                    public int getIconWidth() { return 32; }
                    public int getIconHeight() { return 32; }
                    public void paintIcon(Component comp, Graphics g0, int x, int y) {
                        Graphics2D g = (Graphics2D) g0.create();
                        Theme.antialias(g);
                        g.setColor(color);
                        g.fillRoundRect(x, y, 32, 32, 8, 8);
                        Icons.of(icon, 18, Color.WHITE).paintIcon(comp, g, x + 7, y + 7);
                        g.dispose();
                    }
                });
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return l;
            }
        };
        return r;
    }

    private void edit(LaundryService existing) {
        LaundryService s = existing == null ? new LaundryService() : existing;
        JTextField name = new JTextField(s.getName() == null ? "" : s.getName());
        JTextField price = new JTextField(s.getPrice().signum() == 0 ? "" : s.getPrice().toPlainString());
        JComboBox<String> unit = new JComboBox<>(new String[]{"KG", "PC"});
        unit.setSelectedItem(s.getUnit());
        JSpinner hours = new JSpinner(new SpinnerNumberModel(s.getTurnaroundHours(), 1, 168, 1));
        JTextField desc = new JTextField(s.getDescription() == null ? "" : s.getDescription());
        JCheckBox active = new JCheckBox("Available to customers", s.isActive());
        Object[] form = {"Service name:", name, "Price (" + Formats.PESO + "):", price, "Priced per:", unit,
                "Turnaround (hours):", hours, "Description:", desc, active};
        int ok = JOptionPane.showConfirmDialog(this, form, existing == null ? "Add Service" : "Edit Service",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        Dialogs.run(this, () -> {
            s.setName(name.getText());
            try { s.setPrice(new BigDecimal(price.getText().trim())); }
            catch (NumberFormatException ex) { throw new com.metriclaundry.util.AppException("Price must be a number, e.g. 30.00"); }
            s.setUnit((String) unit.getSelectedItem());
            s.setTurnaroundHours((Integer) hours.getValue());
            s.setDescription(desc.getText().trim());
            s.setActive(active.isSelected());
            catalog.save(s);
        });
        refresh();
    }

    private void delete(LaundryService s) {
        if (!Dialogs.confirm(this, "Delete \"" + s.getName() + "\"?")) return;
        Dialogs.run(this, () -> {
            boolean deleted = catalog.delete(s.getId());
            if (!deleted) Dialogs.info(this, "\"" + s.getName() + "\" is used by existing orders, so it was "
                    + "deactivated instead of deleted (history is preserved).");
        });
        refresh();
    }

    @Override public void refresh() {
        load(() -> catalog.list(false), list -> {
            rows = list;
            model.setRowCount(0);
            for (LaundryService s : list)
                model.addRow(new Object[]{s.getName(), Formats.peso(s.getPrice()) + " / " + s.unitLabel(),
                        s.getTurnaroundHours() + " hr" + (s.getTurnaroundHours() == 1 ? "" : "s"),
                        s.getDescription(), s.isActive() ? "Active" : "Inactive", "", ""});
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        table = Tables.table(model);
        bar = new Toolbar();
        hint = new JLabel();
        add = AppButton.primary("Add Service", "plus");
        card = new RoundedPanel();
        view1 = Theme.scroll(null);
        this.setLayout(new BorderLayout());
        table.setRowHeight(52);
        hint.setText("Price changes apply to NEW orders only - existing orders keep their original price.");
        hint.setFont(Theme.font(Font.ITALIC, 12));
        hint.setForeground(Theme.MUTED);
        add.setText("Add Service");
        card.setLayout(new BorderLayout());
        this.add(bar, BorderLayout.NORTH);
        view1.setViewportView(table);
        card.add(view1);
        this.add(card, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTable table;
    private Toolbar bar;
    private JLabel hint;
    private AppButton add;
    private RoundedPanel card;
    private JScrollPane view1;
    // End of variables declaration//GEN-END:variables
}
