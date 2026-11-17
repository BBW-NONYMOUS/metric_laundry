package com.metriclaundry.service;

import com.metriclaundry.dao.OrderDAO;
import com.metriclaundry.dao.PaymentDAO;
import com.metriclaundry.model.*;
import com.metriclaundry.util.AppException;

import java.util.List;

/**
 * Payment rules:
 *  - one payment per order; amount always equals the order total (no tampering from the UI)
 *  - GCash needs a 13-digit-or-less reference number; staff verifies it -> PAID
 *  - Cash / Card are PENDING until staff receives the money at the counter
 *  - Staff recording a payment at the counter marks it PAID immediately
 *  - Card numbers are never captured (card is tapped on the shop's POS terminal)
 */
public class PaymentService {
    private final PaymentDAO payments = new PaymentDAO();
    private final OrderDAO orders = new OrderDAO();

    public Payment submit(int orderId, PaymentMethod method, String referenceNo, User actor) {
        Order o = orders.findById(orderId);
        if (o == null) throw new AppException("Order not found.");
        if (o.getStatus() == OrderStatus.CANCELLED) throw new AppException("Order is cancelled.");
        if (actor.getRole() == Role.CUSTOMER && o.getCustomerId() != actor.getId())
            throw new AppException("You can only pay for your own orders.");
        if (o.isPaid()) throw new AppException("Order " + o.getOrderNo() + " is already paid.");
        if (method == null) throw new AppException("Please choose a payment method.");

        String ref = referenceNo == null ? "" : referenceNo.replaceAll("\\s", "");
        if (method == PaymentMethod.GCASH && !ref.matches("\\d{6,13}"))
            throw new AppException("Enter the GCash reference number (6-13 digits) from your GCash receipt.");

        boolean staffAtCounter = actor.getRole() != Role.CUSTOMER;
        PaymentStatus status = staffAtCounter ? PaymentStatus.PAID : PaymentStatus.PENDING;
        String receiptNo = "RC-" + o.getOrderNo().substring(3);
        payments.upsert(orderId, receiptNo, method, o.getTotal(), ref.isEmpty() ? null : ref, status,
                staffAtCounter ? actor.getId() : null);
        return payments.findByOrder(orderId);
    }

    public void confirm(int paymentId, User actor) {
        if (actor.getRole() == Role.CUSTOMER) throw new AppException("Only staff can confirm payments.");
        payments.markPaid(paymentId, actor.getId());
    }

    public Payment findByOrder(int orderId) { return payments.findByOrder(orderId); }

    public List<Payment> search(PaymentStatus status, String keyword) { return payments.search(status, keyword); }
}
