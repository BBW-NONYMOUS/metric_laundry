package com.metriclaundry.model;

import java.math.BigDecimal;

public class DashboardStats {
    public int totalOrders;
    public int totalCustomers;
    public BigDecimal totalRevenue = BigDecimal.ZERO;
    public int completedOrders;
    public int pendingOrders;
    public int inProcessOrders;
    public int readyOrders;
    public int completedToday;
    public int unpaidPayments;
}
