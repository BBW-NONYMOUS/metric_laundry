package com.metriclaundry.ui.common;

import com.metriclaundry.model.Order;
import com.metriclaundry.model.Payment;
import com.metriclaundry.model.PaymentStatus;
import com.metriclaundry.ui.components.AppButton;
import com.metriclaundry.ui.components.Dialogs;
import com.metriclaundry.ui.components.Logo;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;

/** "Digital Laundry - Receipt" window with Print and Save-as-image. */
public class ReceiptDialog extends JDialog {

    private Window owner;
    private Order o;
    private Payment p;

    public ReceiptDialog(Window owner, Order o, Payment p) {
        super(owner, "Digital Laundry - Receipt", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        this.o = o;
        this.p = p;
        initComponents();
        print.addActionListener(e -> print(receipt));
        save.addActionListener(e -> save(receipt, p.getReceiptNo()));
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JPanel buildReceipt(Order o, Payment p) {
        JPanel r = new JPanel(new GridBagLayout());
        r.setBackground(Color.WHITE);
        r.setBorder(Theme.pad(18, 30, 8, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridwidth = 4; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        int y = 0;

        JPanel head = new JPanel(new FlowLayout(FlowLayout.CENTER));
        head.setOpaque(false);
        head.add(new Logo(90, false));
        c.gridy = y++; r.add(head, c);
        JLabel title = Theme.label("Digital Receipt", Font.BOLD, 20, Theme.TEXT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = y++; r.add(title, c);
        JLabel thanks = Theme.label("Thank you for choosing Metric Laundry!", Font.ITALIC, 12, Theme.MUTED);
        thanks.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = y++; c.insets = new Insets(0, 0, 14, 0); r.add(thanks, c);
        c.insets = new Insets(2, 0, 2, 0);

        String[][] info = {
                {"Receipt No.", p.getReceiptNo()},
                {"Order No.", o.getOrderNo()},
                {"Customer", o.getCustomerName()},
                {"Date", Formats.dateTime(p.getPaidAt() != null ? p.getPaidAt() : p.getCreatedAt())},
                {"Payment Method", p.getMethod().label() + (p.getReferenceNo() != null ? "  (Ref: " + p.getReferenceNo() + ")" : "")},
        };
        for (String[] row : info) {
            c.gridy = y++;
            r.add(kv(row[0], row[1]), c);
        }
        c.gridy = y++; c.insets = new Insets(12, 0, 0, 0);
        r.add(lineRow(Theme.bold(12.5f), "Service", "Per kg", "Price", "Total", new Color(0xF1F6F1)), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 0, 0);
        r.add(lineRow(Theme.regular(12.5f), o.getServiceName(), o.quantityLabel(),
                Formats.peso(o.getUnitPrice()), Formats.peso(o.getTotal()), Color.WHITE), c);
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        c.gridy = y++; c.insets = new Insets(6, 0, 6, 0); r.add(sep, c);

        JPanel total = new JPanel(new BorderLayout());
        total.setOpaque(false);
        total.add(Theme.label("This transaction:", Font.BOLD, 14, Theme.TEXT), BorderLayout.WEST);
        total.add(Theme.label(Formats.peso(p.getAmount()), Font.BOLD, 18, Theme.PRIMARY_DARK), BorderLayout.EAST);
        c.gridy = y++; c.insets = new Insets(0, 0, 10, 0); r.add(total, c);

        c.gridy = y++; r.add(kv("Order total", Formats.peso(o.getTotal())), c);
        c.gridy = y++; r.add(kv("Confirmed paid to date", Formats.peso(o.getPaidAmount())), c);
        c.gridy = y++; r.add(kv("Balance due", Formats.peso(o.getBalance())), c);
        c.gridy = y++; r.add(kv("Awaiting verification", Formats.peso(o.getPendingAmount())), c);
        c.gridy = y++; r.add(kv("Order payment status", o.getPaymentStatus().label()), c);
        boolean paid = p.getStatus() == PaymentStatus.PAID;
        JLabel status = Theme.label("Payment Status: " + p.getStatus().label(), Font.BOLD, 13,
                paid ? Theme.OK_FG : Theme.WARN_FG);
        status.setIcon(Icons.of(paid ? "check" : "clock", 16, paid ? Theme.OK_FG : Theme.WARN_FG, 2.6f));
        status.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = y++; r.add(status, c);
        if (!paid) {
            JLabel note = Theme.label("This transaction does not count as a confirmed payment.", Font.PLAIN, 11.5f, Theme.MUTED);
            note.setHorizontalAlignment(SwingConstants.CENTER);
            c.gridy = y; r.add(note, c);
        }
        r.setPreferredSize(new Dimension(430, r.getPreferredSize().height));
        return r;
    }

    private JPanel kv(String k, String v) {
        JPanel p = new JPanel(new GridLayout(1, 2));
        p.setOpaque(false);
        p.add(Theme.label(k, Font.PLAIN, 12.5f, Theme.MUTED));
        p.add(Theme.label(": " + v, Font.BOLD, 12.5f, Theme.TEXT));
        return p;
    }

    private JPanel lineRow(Font f, String a, String b, String cc, String d, Color bg) {
        JPanel p = new JPanel(new GridLayout(1, 4));
        p.setBackground(bg);
        p.setBorder(Theme.pad(6, 8, 6, 8));
        for (String s : new String[]{a, b, cc, d}) {
            JLabel l = new JLabel(s);
            l.setFont(f);
            l.setForeground(Theme.TEXT);
            p.add(l);
        }
        return p;
    }

    private static BufferedImage snapshot(JComponent c) {
        BufferedImage img = new BufferedImage(c.getWidth() * 2, c.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.scale(2, 2);
        c.printAll(g);
        g.dispose();
        return img;
    }

    private void save(JComponent receipt, String name) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(name + ".png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".png")) f = new File(f.getPath() + ".png");
            ImageIO.write(snapshot(receipt), "png", f);
            Dialogs.info(this, "Receipt saved to:\n" + f.getAbsolutePath());
        } catch (Exception e) { Dialogs.error(this, e); }
    }

    /** Prints the receipt. Choose "Microsoft Print to PDF" / "Save as PDF" in the dialog to get a PDF. */
    private void print(JComponent receipt) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Metric Laundry Receipt");
        job.setPrintable((Graphics g, PageFormat pf, int page) -> {
            if (page > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            double s = Math.min(1.0, Math.min(pf.getImageableWidth() / receipt.getWidth(), pf.getImageableHeight() / receipt.getHeight()));
            g2.scale(s, s);
            receipt.printAll(g2);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try { job.print(); } catch (Exception e) { Dialogs.error(this, e); }
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        receipt = buildReceipt(o, p);
        buttons = new JPanel();
        print = AppButton.outline("Print Receipt", "print");
        save = AppButton.outline("Save as Image", "download");
        root = new JPanel();
        getContentPane().setLayout(new BorderLayout());
        buttons.setBackground(Color.WHITE);
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 10));
        print.setText("Print Receipt");
        save.setText("Save as Image");
        root.setBackground(Color.WHITE);
        root.setLayout(new BorderLayout());
        root.add(receipt, BorderLayout.CENTER);
        buttons.add(print);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);
        getContentPane().add(root, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel receipt;
    private JPanel buttons;
    private AppButton print;
    private AppButton save;
    private JPanel root;
    // End of variables declaration//GEN-END:variables
}
