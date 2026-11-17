package com.metriclaundry.ui.common;

import com.metriclaundry.dao.UserDAO;
import com.metriclaundry.model.Role;
import com.metriclaundry.model.User;
import com.metriclaundry.service.AuthService;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.theme.Theme;
import com.metriclaundry.util.Formats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic user list: "Manage Customers" (admin + staff) and "Manage Staff" (admin only).
 * Staff may only view and add customers; admin can edit, (de)activate and reset passwords.
 */
public class UsersPage extends Page {
    private final UserDAO dao = new UserDAO();
    private final User actor;
    private final Role[] roles;
    private final DefaultTableModel model = Tables.model("Full Name", "Username", "Role", "Mobile No.", "Email", "Status", "Joined");

    private List<User> rows = new ArrayList<>();

    private String title;
    private String subtitle;
    private Role defaultRole;
    private boolean admin;

    public UsersPage(String title, String subtitle, User actor, Role defaultRole, Role... roles) {
        super(title, subtitle);
        this.title = title;
        this.subtitle = subtitle;
        this.actor = actor;
        this.defaultRole = defaultRole;
        this.roles = roles;
        this.admin = actor.getRole() == Role.ADMIN;
        initComponents();
        bar.search(search, this::refresh);
        if (admin) {
            AppButton edit = AppButton.outline("Edit", "edit");
            AppButton toggle = AppButton.outline("Activate / Deactivate", "lock");
            AppButton reset = AppButton.outline("Reset Password", "refresh");
            bar.right(reset).right(toggle).right(edit);
            edit.addActionListener(e -> {
                User u = selected();
                if (u == null) return;
                UserFormDialog d = new UserFormDialog(SwingUtilities.getWindowAncestor(this), u, u.getRole(), true);
                d.setVisible(true);
                refresh();
            });
            toggle.addActionListener(e -> {
                User u = selected();
                if (u == null) return;
                if (u.getId() == actor.getId()) { Dialogs.info(this, "You cannot deactivate your own account."); return; }
                u.setActive(!u.isActive());
                if (Dialogs.run(this, () -> new AuthService().updateUser(u))) refresh();
            });
            reset.addActionListener(e -> {
                User u = selected();
                if (u == null) return;
                String pw = Dialogs.ask(this, "New temporary password for " + u.getFullName() + " (min. 6 characters):");
                if (pw != null && Dialogs.run(this, () -> new AuthService().resetPassword(u.getId(), pw)))
                    Dialogs.info(this, "Password reset. Ask " + u.getFirstName() + " to change it after logging in.");
            });
        }
        bar.right(add);
        add.addActionListener(e -> {
            UserFormDialog d = new UserFormDialog(SwingUtilities.getWindowAncestor(this), null, defaultRole, admin);
            d.setVisible(true);
            refresh();
        });
        Tables.badgeColumn(table, 5);
    }

    private User selected() {
        int v = table.getSelectedRow();
        if (v < 0) { Dialogs.info(this, "Select a user first."); return null; }
        return rows.get(table.convertRowIndexToModel(v));
    }

    @Override public void refresh() {
        String kw = search.getText();
        load(() -> dao.search(kw, roles), list -> {
            rows = list;
            model.setRowCount(0);
            for (User u : list) {
                model.addRow(new Object[]{u.getFullName(), u.getUsername(), u.getRole().label(), dash(u.getPhone()),
                        dash(u.getEmail()), u.isActive() ? "Active" : "Inactive", Formats.date(u.getCreatedAt())});
            }
        });
    }

    private static String dash(String s) { return s == null || s.isBlank() ? "-" : s; }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        table = Tables.table(model);
        search = new InputField("Search name, username, phone, email", "search", false);
        bar = new Toolbar();
        add = AppButton.primary("Add " + defaultRole.label(), "plus");
        card = new RoundedPanel();
        view1 = Theme.scroll(null);
        this.setLayout(new BorderLayout());
        add.setText("Add " + defaultRole.label());
        card.setLayout(new BorderLayout());
        this.add(bar, BorderLayout.NORTH);
        view1.setViewportView(table);
        card.add(view1);
        this.add(card, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTable table;
    private InputField search;
    private Toolbar bar;
    private AppButton add;
    private RoundedPanel card;
    private JScrollPane view1;
    // End of variables declaration//GEN-END:variables
}
