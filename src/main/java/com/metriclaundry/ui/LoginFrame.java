package com.metriclaundry.ui;

import com.metriclaundry.model.User;
import com.metriclaundry.service.AuthService;
import com.metriclaundry.ui.admin.AdminDashboard;
import com.metriclaundry.ui.components.*;
import com.metriclaundry.ui.customer.CustomerDashboard;
import com.metriclaundry.ui.staff.StaffDashboard;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Single login for Admin, Staff and Customer. After authentication the user is
 * routed to the dashboard for their role (role-based access control).
 */
public class LoginFrame extends JFrame {
    private final AuthService auth = new AuthService();

    public LoginFrame() {
        super("Metric Laundry - Login");

        initComponents();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 600);
        setLocationRelativeTo(null);
        register.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { new RegisterDialog(LoginFrame.this).setVisible(true); }
        });
        login.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(login);
    }

    private JLabel link(String text) {
        JLabel l = Theme.label(text, Font.BOLD, 13, Theme.PRIMARY);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }

    private void doLogin() {
        error.setText(" ");
        login.setEnabled(false);
        String u = username.getText();
        String p = new String(password.getPassword());
        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() { return auth.login(u, p); }
            @Override protected void done() {
                login.setEnabled(true);
                try {
                    open(get());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    error.setText(cause.getMessage());
                    password.setText("");
                }
            }
        }.execute();
    }

    private void open(User user) {
        JFrame next;
        switch (user.getRole()) {
            case ADMIN: next = AdminDashboard.create(user); break;
            case STAFF: next = StaffDashboard.create(user); break;
            default: next = CustomerDashboard.create(user);
        }
        dispose();
        next.setVisible(true);
    }

    /** Left half: brand illustration like the "Admin Login" mockup. */

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        username = new InputField("Username", "user", false);
        password = new InputField("Password", "lock", true);
        login = AppButton.primary("Login").large();
        error = new JLabel();
        root = new JPanel();
        view1 = new LoginBrandPanel();
        side = new JPanel();
        card = new RoundedPanel();
        h = new JLabel();
        sub = new JLabel();
        register = link("New customer? Create an account");
        forgot = new JLabel();
        roles = new JLabel();
        setMinimumSize(new Dimension(860, 560));
        getContentPane().setLayout(new BorderLayout());
        username.setPlaceholder("Username");
        username.setPreferredSize(new Dimension(300, 44));
        password.setPlaceholder("Password");
        password.setPreferredSize(new Dimension(300, 44));
        login.setText("Login");
        error.setText(" ");
        error.setFont(Theme.font(Font.PLAIN, 12.5f));
        error.setForeground(Theme.DANGER);
        root.setLayout(new GridLayout(1, 2));
        side.setBackground(Theme.BG);
        side.setLayout(new GridBagLayout());
        card.setBorder(Theme.pad(30, 34, 28, 34));
        card.setLayout(new GridBagLayout());
        h.setText("Welcome Back!");
        h.setFont(Theme.font(Font.BOLD, 26));
        h.setForeground(Theme.TEXT);
        h.setHorizontalAlignment(SwingConstants.CENTER);
        sub.setText("Enter your credentials to continue");
        sub.setFont(Theme.font(Font.PLAIN, 13.5f));
        sub.setForeground(Theme.MUTED);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        register.setText("New customer? Create an account");
        register.setFont(Theme.bold(13));
        register.setForeground(Theme.PRIMARY);
        register.setHorizontalAlignment(0);
        forgot.setText("Forgot password? Please ask the shop admin to reset it.");
        forgot.setFont(Theme.font(Font.PLAIN, 11.5f));
        forgot.setForeground(Theme.MUTED);
        forgot.setHorizontalAlignment(SwingConstants.CENTER);
        roles.setText("One login for Admin, Staff and Customers");
        roles.setFont(Theme.font(Font.PLAIN, 12));
        roles.setForeground(Theme.MUTED);
        root.add(view1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        card.add(h, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 22, 0);
        card.add(sub, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        card.add(username, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        card.add(password, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 8, 0);
        card.add(error, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        card.add(login, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        card.add(register, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        card.add(forgot, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = -1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        side.add(card, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = -1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(16, 0, 0, 0);
        side.add(roles, gridBagConstraints);
        root.add(side);
        getContentPane().add(root, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private InputField username;
    private InputField password;
    private AppButton login;
    private JLabel error;
    private JPanel root;
    private LoginBrandPanel view1;
    private JPanel side;
    private RoundedPanel card;
    private JLabel h;
    private JLabel sub;
    private JLabel register;
    private JLabel forgot;
    private JLabel roles;
    // End of variables declaration//GEN-END:variables
}
