package com.metriclaundry.service;

import com.metriclaundry.dao.OrderDAO;
import com.metriclaundry.dao.ServiceDAO;
import com.metriclaundry.model.*;
import com.metriclaundry.util.AppException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * All order business rules live here (not in the UI):
 *  - total = quantity x unit price (price is snapshotted at order time)
 *  - status moves forward one step at a time; only ADMIN may override
 *  - an order can only be COMPLETED (released) once it is PAID
 *  - customers may cancel only while the order is still PLACED
 */
public class OrderService {
    private static final BigDecimal MAX_QTY = new BigDecimal("100");

    private final OrderDAO orders = new OrderDAO();
    private final ServiceDAO services = new ServiceDAO();

    public BigDecimal computeTotal(LaundryService s, BigDecimal qty) {
        return s.getPrice().multiply(qty).setScale(2, RoundingMode.HALF_UP);
    }

    public Order placeOrder(int customerId, int serviceId, BigDecimal qty, String notes, User actor) {
        LaundryService s = services.findById(serviceId);
        if (s == null || !s.isActive()) throw new AppException("Selected service is not available.");
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) throw new AppException("Quantity must be greater than zero.");
        if (qty.compareTo(MAX_QTY) > 0) throw new AppException("Maximum of 100 " + s.unitLabel() + " per order.");
        if ("PC".equals(s.getUnit()) && qty.stripTrailingZeros().scale() > 0)
            throw new AppException(s.getName() + " is priced per piece - please enter a whole number.");
        if (actor.getRole() == Role.CUSTOMER && actor.getId() != customerId)
            throw new AppException("Customers can only place orders for themselves.");

        Order o = new Order();
        o.setCustomerId(customerId);
        o.setServiceId(serviceId);
        o.setQuantity(qty);
        o.setUnitPrice(s.getPrice());
        o.setTotal(computeTotal(s, qty));
        o.setNotes(notes == null || notes.isBlank() ? null : notes.trim());
        o.setEstimatedCompletion(LocalDateTime.now().plusHours(s.getTurnaroundHours()));
        return orders.insert(o, actor.getId());
    }

    /** Staff/Admin: move to the next step of the flow. */
    public OrderStatus advance(int orderId, User actor) {
        requireStaff(actor);
        Order o = require(orderId);
        OrderStatus next = o.getStatus().next();
        if (next == null) throw new AppException("Order " + o.getOrderNo() + " is already " + o.getStatus().label() + ".");
        if (next == OrderStatus.COMPLETED && !o.isPaid())
            throw new AppException("Order " + o.getOrderNo() + " is not yet paid. Confirm the payment before releasing the laundry.");
        orders.updateStatus(orderId, next, actor.getId(), "Moved to " + next.label());
        return next;
    }

    /** Admin only: set any status (e.g. to correct a mistake). Reason is recorded in history. */
    public void overrideStatus(int orderId, OrderStatus status, String reason, User actor) {
        if (actor.getRole() != Role.ADMIN) throw new AppException("Only an administrator can override a status.");
        if (reason == null || reason.isBlank()) throw new AppException("Please give a reason for the override.");
        Order o = require(orderId);
        if (status == OrderStatus.COMPLETED && !o.isPaid())
            throw new AppException("Cannot complete an unpaid order.");
        orders.updateStatus(orderId, status, actor.getId(), "Override: " + reason.trim());
    }

    public void cancel(int orderId, String reason, User actor) {
        Order o = require(orderId);
        if (!o.getStatus().isActive()) throw new AppException("Order is already " + o.getStatus().label() + ".");
        if (actor.getRole() == Role.CUSTOMER) {
            if (o.getCustomerId() != actor.getId()) throw new AppException("You can only cancel your own orders.");
            if (o.getStatus() != OrderStatus.PLACED)
                throw new AppException("Your laundry has already been received. Please contact the shop to cancel.");
        }
        if (o.getPaidAmount().signum() > 0 || o.getPendingAmount().signum() > 0) throw new AppException("This order is already paid. Refunds must be handled by the admin at the counter.");
        orders.updateStatus(orderId, OrderStatus.CANCELLED, actor.getId(),
                "Cancelled" + (reason == null || reason.isBlank() ? "" : ": " + reason.trim()));
    }

    public Order find(int orderId) { return orders.findById(orderId); }

    public List<Order> search(int customerId, OrderStatus status, boolean activeOnly, String keyword, int limit) {
        return orders.search(customerId, status, activeOnly, keyword, limit);
    }

    public List<StatusHistory> history(int orderId) { return orders.history(orderId); }

    private Order require(int id) {
        Order o = orders.findById(id);
        if (o == null) throw new AppException("Order not found.");
        return o;
    }

    private void requireStaff(User u) {
        if (u.getRole() == Role.CUSTOMER) throw new AppException("Only staff can update order status.");
    }
}
