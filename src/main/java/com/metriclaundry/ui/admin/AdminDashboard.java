package com.metriclaundry.ui.admin;

import com.metriclaundry.model.Role;
import com.metriclaundry.model.User;
import com.metriclaundry.ui.common.*;

/** Admin panel - full control: dashboard, orders, customers, staff, services, payments, reports, settings. */
public final class AdminDashboard {
    private AdminDashboard() { }

    public static DashboardFrame create(User user) {
        DashboardFrame f = new DashboardFrame("Admin Dashboard", user, true, "Admin Panel");
        f.addPage("home", "Dashboard", "home", new AdminHomePage(user));
        f.addPage("orders", "Manage Orders", "orders", new OrdersPage(user));
        f.addPage("customers", "Manage Customers", "users",
                new UsersPage("Manage Customers", "Customer accounts", user, Role.CUSTOMER, Role.CUSTOMER));
        f.addPage("staff", "Manage Staff", "user",
                new UsersPage("Manage Staff", "Staff and administrator accounts", user, Role.STAFF, Role.STAFF, Role.ADMIN));
        f.addPage("services", "Manage Services", "shirt", new ServicesPage());
        f.addPage("payments", "Manage Payments", "peso", new PaymentsPage(user));
        f.addPage("reports", "Reports", "chart", new ReportsPage());
        f.addPage("settings", "Settings", "gear", new SettingsPage(user));
        f.navigate("home");
        return f;
    }
}
