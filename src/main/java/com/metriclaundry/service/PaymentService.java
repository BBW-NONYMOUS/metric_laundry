package com.metriclaundry.service;

import com.metriclaundry.dao.OrderDAO;
import com.metriclaundry.dao.PaymentDAO;
import com.metriclaundry.model.*;
import com.metriclaundry.util.AppException;

import java.util.List;

/** Records installments; customer submissions require staff verification. */
public class PaymentService {
    private final PaymentDAO payments = new PaymentDAO();
    private final OrderDAO orders = new OrderDAO();

    public Payment submit(int orderId, PaymentMethod method, String referenceNo, User actor) {
        Order o = orders.findById(orderId);
        if (o == null) throw new AppException("Order not found.");
        return submit(orderId, method, referenceNo, o.getAvailableAmount(), actor);
    }

    public Payment submit(int orderId, PaymentMethod method, String referenceNo, java.math.BigDecimal amount, User actor) {
        if (actor == null) throw new AppException("Please sign in.");
        if (amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 2)
            throw new AppException("Enter a positive payment amount with at most two decimal places.");
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

        return payments.insert(orderId, method, amount, ref.isEmpty() ? null : ref, actor);
    }

    public void confirm(int paymentId, User actor) {
        if (actor.getRole() == Role.CUSTOMER) throw new AppException("Only staff can confirm payments.");
        payments.resolve(paymentId, actor.getId(), true);
    }

    public void voidPending(int paymentId, User actor) {
        if (actor.getRole() == Role.CUSTOMER) throw new AppException("Only staff can void pending payments.");
        payments.resolve(paymentId, actor.getId(), false);
    }
    public List<Payment> history(int orderId) { return payments.history(orderId); }

    public Payment findByOrder(int orderId) { return payments.findByOrder(orderId); }

    public List<Payment> search(PaymentStatus status, String keyword) { return payments.search(status, keyword); }
}
