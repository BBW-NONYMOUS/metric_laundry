package com.metriclaundry.ui.common;
import com.metriclaundry.model.*;
import com.metriclaundry.dao.AddOnDAO;
import com.metriclaundry.util.Formats;
import com.metriclaundry.ui.components.Dialogs;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/** Shared selector for customer and counter orders. Prices are revalidated when saving. */
public final class AddOnSelector {
    private AddOnSelector() { }
    public static BigDecimal total(List<AddOnLine> lines) { return lines.stream().map(AddOnLine::total).reduce(BigDecimal.ZERO,BigDecimal::add); }
    public static String description(List<AddOnLine> lines) {
        return lines.isEmpty() ? "No extras selected" : String.join("\n",lines.stream().map(AddOnLine::description).toList());
    }
    public static List<AddOnLine> ask(Component owner,List<AddOnLine> previous) {
        List<AddOn> items;
        try { items=new AddOnDAO().list(); } catch(Exception e) { Dialogs.error(owner,e); return null; }
        JPanel rows=new JPanel(new GridLayout(0,4,12,8));
        rows.add(new JLabel("Extra")); rows.add(new JLabel("Price per unit")); rows.add(new JLabel("Units to add")); rows.add(new JLabel("Added cost"));
        List<JSpinner> quantities=new ArrayList<>(); List<JLabel> amounts=new ArrayList<>();
        JLabel total=new JLabel();
        Runnable recalc=() -> {
            BigDecimal sum=BigDecimal.ZERO;
            for(int i=0;i<quantities.size();i++) {
                BigDecimal q=new BigDecimal(quantities.get(i).getValue().toString());
                BigDecimal price=items.get(i).price();
                BigDecimal value=price==null ? BigDecimal.ZERO : price.multiply(q).setScale(2,java.math.RoundingMode.HALF_UP);
                amounts.get(i).setText(Formats.peso(value)); sum=sum.add(value);
            }
            total.setText("Extras subtotal: " + Formats.peso(sum));
        };
        for(AddOn item:items) {
            double value=previous.stream().filter(l -> l.addonId()==item.id()).map(l -> l.quantity().doubleValue()).findFirst().orElse(0.0);
            JSpinner qty=new JSpinner(new SpinnerNumberModel(item.available() ? value : 0.0,0.0,100.0,1.0));
            qty.setEnabled(item.available());
            JLabel amount=new JLabel();
            quantities.add(qty);amounts.add(amount);
            rows.add(new JLabel(item.name()));
            rows.add(new JLabel(item.price() == null ? "Price not set - unavailable" : Formats.peso(item.price()) + " / " + item.unit() + (item.available() ? "" : " (Unavailable)")));
            rows.add(qty); rows.add(amount);
            qty.addChangeListener(e -> recalc.run());
        }
        recalc.run();
        JPanel panel=new JPanel(new BorderLayout(8,12));
        panel.add(new JLabel("Optional extras. Enter 0 to leave an item out. Prices are additional to laundry charges."),BorderLayout.NORTH);
        JScrollPane scroll=new JScrollPane(rows); scroll.setPreferredSize(new Dimension(800,Math.min(350,60+items.size()*38)));
        panel.add(scroll);panel.add(total,BorderLayout.SOUTH);
        while(JOptionPane.showConfirmDialog(owner,panel,"Add-ons and prices",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION) {
            List<AddOnLine> selected=new ArrayList<>();
            try {
                for(int i=0;i<items.size();i++) {
                    quantities.get(i).commitEdit();
                    AddOn item=items.get(i); BigDecimal q=new BigDecimal(quantities.get(i).getValue().toString());
                    if(q.signum()<0 || q.compareTo(new BigDecimal("100"))>0 || q.stripTrailingZeros().scale()>2) throw new IllegalArgumentException("Use quantities from 0 to 100 with at most two decimal places.");
                    if(item.available() && q.signum()>0) selected.add(new AddOnLine(item.id(),item.name(),item.unit(),q,item.price()));
                }
                return selected;
            } catch(Exception ex) { Dialogs.error(owner,ex); }
        }
        return null;
    }
}
