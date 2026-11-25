package com.metriclaundry.ui.common;

import com.metriclaundry.model.*;
import com.metriclaundry.service.*;
import com.metriclaundry.ui.components.Dialogs;
import com.metriclaundry.util.Formats;
import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.List;

/** Printable statement for an order, including installments and status audit history. */
public class TransactionHistoryDialog extends JDialog {
    private final int orderId;
    private final User actor;
    private final Runnable onChange;
    private final PaymentService payments = new PaymentService();
    private final OrderService orders = new OrderService();
    private final JTextArea statement = new JTextArea();
    private final JComboBox<PaymentChoice> choices = new JComboBox<>();
    private record PaymentChoice(Payment payment) {
        @Override public String toString() {
            return payment.getReceiptNo() + " / " + Formats.peso(payment.getAmount()) + " / " + payment.getStatus().label();
        }
    }

    public TransactionHistoryDialog(Window owner, int orderId, User actor, Runnable onChange) {
        super(owner, "Order transaction history", ModalityType.APPLICATION_MODAL);
        this.orderId=orderId; this.actor=actor; this.onChange=onChange;
        Order order=orders.find(orderId);
        if (order==null || (actor.getRole()==Role.CUSTOMER && order.getCustomerId()!=actor.getId()))
            throw new com.metriclaundry.util.AppException("Order is not accessible.");
        statement.setEditable(false);
        statement.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statement.setLineWrap(true); statement.setWrapStyleWord(true);
        statement.setMargin(new Insets(15,15,15,15));
        add(new JScrollPane(statement),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout());
        actions.add(choices);
        if(actor.getRole()!=Role.CUSTOMER) {
            JButton confirm=new JButton("Confirm received");
            confirm.addActionListener(e -> resolve(true)); actions.add(confirm);
            JButton reject=new JButton("Void pending");
            reject.addActionListener(e -> resolve(false)); actions.add(reject);
        }
        JButton print=new JButton("Print / Save PDF");
        print.addActionListener(e -> {
            try { statement.print(new MessageFormat("Metric Laundry - Transaction History"), new MessageFormat("Page {0}")); }
            catch(Exception ex) { Dialogs.error(this,ex); }
        });
        actions.add(print);
        add(actions,BorderLayout.SOUTH);
        refresh(); setSize(1050,700); setLocationRelativeTo(owner);
    }

    private void resolve(boolean confirm) {
        PaymentChoice choice=(PaymentChoice) choices.getSelectedItem();
        if(choice==null) return;
        Payment p=choice.payment();
        if(p.getStatus()!=PaymentStatus.PENDING) { Dialogs.info(this,"Only pending transactions can be confirmed or voided."); return; }
        String prompt=(confirm ? "Confirm money received" : "Void this unverified transaction") + "?\n" + choice
            + "\nMethod: " + p.getMethod() + "\nReference: " + p.getReferenceNo();
        if(!Dialogs.confirm(this,prompt)) return;
        if(Dialogs.run(this,() -> { if(confirm) payments.confirm(p.getId(),actor); else payments.voidPending(p.getId(),actor); })) {
            refresh(); onChange.run();
        }
    }

    private void refresh() {
        Order o=orders.find(orderId);
        List<Payment> history=payments.history(orderId);
        choices.removeAllItems();
        StringBuilder text=new StringBuilder("METRIC LAUNDRY - ORDER STATEMENT\n");
        text.append("Generated: ").append(Formats.dateTime(java.time.LocalDateTime.now())).append("\n\n");
        text.append("Order: ").append(o.getOrderNo()).append("    Customer: ").append(o.getCustomerName()).append("\n");
        text.append("Service: ").append(o.getServiceName()).append("    Per kg: ").append(o.quantityLabel()).append("\n");
        text.append("Unit price: ").append(Formats.peso(o.getUnitPrice())).append("    Placed: ").append(Formats.dateTime(o.getCreatedAt())).append("\n");
        text.append("Order status: ").append(o.getStatus()).append("    Payment: ").append(o.getPaymentStatus()).append("\n\n");
        text.append("Order total: ").append(Formats.peso(o.getTotal())).append("\nConfirmed paid: ").append(Formats.peso(o.getPaidAmount()));
        text.append("\nBalance due: ").append(Formats.peso(o.getBalance())).append("\nPending verification: ").append(Formats.peso(o.getPendingAmount()));
        text.append("\nPending and voided transactions do not reduce the balance.\n\nPAYMENT TRANSACTIONS\n");
        if(history.isEmpty()) text.append("No payments recorded.\n");
        for(Payment p:history) {
            choices.addItem(new PaymentChoice(p));
            text.append("\n").append(p.getReceiptNo()).append(" | ").append(Formats.peso(p.getAmount())).append(" | ").append(p.getMethod()).append(" | ").append(p.getStatus());
            text.append("\nSubmitted: ").append(Formats.dateTime(p.getCreatedAt())).append("    Confirmed: ").append(p.getPaidAt()==null ? "-" : Formats.dateTime(p.getPaidAt()));
            text.append("\nReference: ").append(p.getReferenceNo()==null ? "-" : p.getReferenceNo());
            text.append("    Handled by: ").append(p.getReceivedByName()==null ? "Awaiting staff" : p.getReceivedByName()).append("\n");
        }
        text.append("\nORDER STATUS HISTORY\n");
        for(StatusHistory h:orders.history(orderId)) {
            text.append(Formats.dateTime(h.getChangedAt())).append(" | ").append(h.getStatus()).append(" | ").append(h.getChangedByName());
            text.append("\n").append(h.getRemarks()==null ? "" : h.getRemarks()).append("\n");
        }
        statement.setText(text.toString()); statement.setCaretPosition(0);
    }
}
