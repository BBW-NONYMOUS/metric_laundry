package com.metriclaundry;

import com.metriclaundry.config.Database;
import com.metriclaundry.model.Role;
import com.metriclaundry.model.User;
import com.metriclaundry.service.AuthService;
import com.metriclaundry.ui.admin.AdminDashboard;
import com.metriclaundry.ui.customer.CustomerDashboard;
import com.metriclaundry.ui.staff.StaffDashboard;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.AppException;
import com.metriclaundry.util.PasswordUtil;
import com.metriclaundry.util.Session;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Explicit integration check against a local database with the default accounts. */
public final class LoginDatabaseCheck {
    public static void main(String[] args) throws Exception {
        Database.testConnection();
        AuthService auth = new AuthService();
        String[][] accounts = {{"admin", "admin123", "ADMIN"}, {"staff", "staff123", "STAFF"},
                {"juan", "customer123", "CUSTOMER"}, {"maria", "customer123", "CUSTOMER"}};
        for (String[] account : accounts) {
            Session.clear();
            User user = auth.login(account[0], account[1]);
            check(user.getRole() == Role.valueOf(account[2]), "Wrong database role for " + account[0]);
            check(Session.get() == user, "Session was not established");
            SwingUtilities.invokeAndWait(() -> {
                Theme.install();
                JFrame dashboard = switch (user.getRole()) {
                    case ADMIN -> AdminDashboard.create(user);
                    case STAFF -> StaffDashboard.create(user);
                    case CUSTOMER -> CustomerDashboard.create(user);
                };
                dashboard.dispose();
            });
            check(auth.login(user.getEmail(), account[1]).getId() == user.getId(), "Email login failed");
            Session.clear();
            try {
                auth.login(account[0], "incorrect-password");
                throw new AssertionError("Wrong password accepted");
            } catch (AppException expected) {
                check("Invalid username or password.".equals(expected.getMessage()), "Unexpected login error");
                check(Session.get() == null, "Failed login established a session");
            }
            System.out.println("PASS " + account[0] + ": role, password, email login, dashboard, wrong-password rejection");
        }
        check(!PasswordUtil.verify("test", "invalid salt!", "invalid hash!"), "Malformed salt accepted");
        check(!PasswordUtil.verify("test", "", "pbkdf2_sha256$99999999$a$b"), "Invalid iterations accepted");
        System.out.println("PASS malformed credentials rejected");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
