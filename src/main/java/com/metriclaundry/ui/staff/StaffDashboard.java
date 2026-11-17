package com.metriclaundry.ui.staff;

import com.metriclaundry.model.Role;
import com.metriclaundry.model.User;
import com.metriclaundry.ui.common.*;

/** Staff panel: daily queue, orders, payments and customers (no prices / staff management / reports). */
public final class StaffDashboard {
    private StaffDashboard() { }

    public static DashboardFrame create(User user) {
        DashboardFrame f = new DashboardFrame("Staff Dashboard", user, true, "Staff Panel");
        f.addPage("home", "Dashboard", "home", new StaffHomePage(user));
        f.addPage("orders", "Manage Orders", "orders", new OrdersPage(user));
        f.addPage("payments", "Manage Payments", "peso", new PaymentsPage(user));
        f.addPage("customers", "Customers", "users",
                new UsersPage("Customers", "Look up customers and register walk-ins", user, Role.CUSTOMER, Role.CUSTOMER));
        f.addPage("settings", "Settings", "gear", new SettingsPage(user));
        f.navigate("home");
        return f;
    }
}
