package com.metriclaundry.ui.customer;

import com.metriclaundry.model.User;
import com.metriclaundry.ui.common.DashboardFrame;
import com.metriclaundry.ui.common.SettingsPage;

/** Customer portal: Home, Place Order, Track Order, Payment, Order History, About Us. */
public final class CustomerDashboard {
    private CustomerDashboard() { }

    public static DashboardFrame create(User user) {
        DashboardFrame f = new DashboardFrame("Customer Dashboard", user, false, null);
        f.addPage("home", "Home", "home", new CustomerHomePage(user));
        f.addPage("place", "Place Order", "cart", new PlaceOrderPage(user));
        f.addPage("track", "Track Order", "search", new TrackOrderPage(user));
        f.addPage("payment", "Payment", "card", new PaymentPage(user));
        f.addPage("history", "Order History", "history", new OrderHistoryPage(user));
        f.addPage("about", "About Us", "info", new AboutPage());
        f.addPage("settings", null, null, new SettingsPage(user));
        f.navigate("home");
        return f;
    }
}
