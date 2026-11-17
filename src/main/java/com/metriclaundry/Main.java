package com.metriclaundry;

import com.metriclaundry.config.Database;
import com.metriclaundry.ui.LoginFrame;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;

/** Entry point of the Metric Laundry Management System. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        
        
        
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            try {
                Database.testConnection();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to the database.\n\n" + e.getMessage()
                                + "\n\n1. Start MySQL (e.g. XAMPP).\n2. Import database/metric_laundry.sql.\n"
                                + "3. Check db.url / db.user / db.password in db.properties.",
                        "Metric Laundry - Database", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new LoginFrame().setVisible(true);
        });
    }
}
