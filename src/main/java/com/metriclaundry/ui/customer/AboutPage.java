package com.metriclaundry.ui.customer;

import com.metriclaundry.ui.common.Page;
import com.metriclaundry.ui.components.Logo;
import com.metriclaundry.ui.components.RoundedPanel;
import com.metriclaundry.ui.theme.Icons;
import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/** About Metric Laundry: who we are, location and hours. */
public class AboutPage extends Page {

    public AboutPage() {
        super("About Us", "Get to know Metric Laundry");

        initComponents();

    }

    private static JLabel line(String icon, String text) {
        JLabel l = Theme.label(text, Font.PLAIN, 14, Theme.TEXT);
        l.setIcon(Icons.of(icon, 20, Theme.PRIMARY));
        l.setIconTextGap(10);
        return l;
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        card = new RoundedPanel();
        left = new JPanel();
        view1 = new Logo(200, false);
        view2 = new JLabel();
        right = new JPanel();
        view3 = new JLabel();
        about = new JLabel();
        view4 = line("pin", "Kalawag 1, back of Water District");
        view5 = line("clock", "8:00 AM - 5:00 PM (Daily)");
        view6 = line("phone", "0917 000 0001");
        view7 = line("mail", "hello@metriclaundry.ph");
        wrap = new JPanel();
        this.setLayout(new BorderLayout());
        card.setBorder(Theme.pad(30, 36, 30, 36));
        card.setLayout(new BorderLayout(36, 0));
        left.setOpaque(false);
        left.setLayout(new GridBagLayout());
        view2.setText("Clean Clothes, Happy You!");
        view2.setFont(Theme.font(Font.BOLD | Font.ITALIC, 18));
        view2.setForeground(Theme.PRIMARY_DARK);
        right.setOpaque(false);
        right.setLayout(new GridBagLayout());
        view3.setText("About Metric Laundry");
        view3.setFont(Theme.font(Font.BOLD, 22));
        view3.setForeground(Theme.TEXT);
        about.setText("<html><div style='width:430px'>Metric Laundry is your trusted laundry service "
                + "provider, offering washing, dry cleaning, and folding services with care and quality. Order online, "
                + "pay with GCash, and track every step of your laundry in real time.</div></html>");
        about.setFont(Theme.regular(14));
        about.setForeground(Theme.TEXT);
        wrap.setOpaque(false);
        wrap.setLayout(new BorderLayout());
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
        left.add(view1, gridBagConstraints);
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
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        left.add(view2, gridBagConstraints);
        card.add(left, BorderLayout.WEST);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        right.add(view3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        right.add(about, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        right.add(view4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        right.add(view5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        right.add(view6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        right.add(view7, gridBagConstraints);
        card.add(right, BorderLayout.CENTER);
        wrap.add(card, BorderLayout.NORTH);
        this.add(wrap, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private RoundedPanel card;
    private JPanel left;
    private Logo view1;
    private JLabel view2;
    private JPanel right;
    private JLabel view3;
    private JLabel about;
    private JLabel view4;
    private JLabel view5;
    private JLabel view6;
    private JLabel view7;
    private JPanel wrap;
    // End of variables declaration//GEN-END:variables
}
