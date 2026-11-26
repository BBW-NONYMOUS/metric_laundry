package com.metriclaundry.ui.admin;
import com.metriclaundry.dao.AddOnDAO;
import com.metriclaundry.model.*;
import com.metriclaundry.ui.common.Tables;
import com.metriclaundry.ui.components.Dialogs;
import com.metriclaundry.util.Formats;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class AddOnCatalogDialog extends JDialog {
    private final AddOnDAO dao=new AddOnDAO();
    private final User actor;
    private List<AddOn> rows;
    private final DefaultTableModel model=Tables.model("Extra","Price","Per","Availability");
    private final JTable table=Tables.table(model);
    public AddOnCatalogDialog(Window owner,User actor) {
        super(owner,"Manage add-ons and prices",ModalityType.APPLICATION_MODAL);
        if(actor==null || actor.getRole()!=Role.ADMIN) throw new com.metriclaundry.util.AppException("Only administrators can manage add-ons.");
        this.actor=actor;
        add(new JLabel("Set actual prices and enable items before customers can select them."),BorderLayout.NORTH);
        add(new JScrollPane(table));
        JPanel buttons=new JPanel(); JButton add=new JButton("Add extra"),edit=new JButton("Edit selected");
        add.addActionListener(e -> edit(null));edit.addActionListener(e -> {
            int row=table.getSelectedRow(); if(row<0) { Dialogs.info(this,"Select an extra first."); return; }
            edit(rows.get(table.convertRowIndexToModel(row)));
        });
        buttons.add(add);buttons.add(edit);add(buttons,BorderLayout.SOUTH);
        refresh();setSize(720,400);setLocationRelativeTo(owner);
    }
    private void refresh() { rows=dao.list();model.setRowCount(0);for(AddOn a:rows) model.addRow(new Object[]{a.name(),a.price()==null ? "Price not set" : Formats.peso(a.price()),a.unit(),a.available() ? "Available" : "Unavailable"}); }
    private void edit(AddOn a) {
        JTextField name=new JTextField(a==null ? "" : a.name()),unit=new JTextField(a==null ? "sachet" : a.unit()),price=new JTextField(a==null || a.price()==null ? "" : a.price().toPlainString());
        JCheckBox active=new JCheckBox("Available to customers",a!=null && a.active());
        Object[] fields={"Name:",name,"Price (PHP):",price,"Charge per unit (e.g. sachet, load, kg):",unit,active};
        while(JOptionPane.showConfirmDialog(this,fields,"Add-on price",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            if(Dialogs.run(this,() -> dao.save(new AddOn(a==null ? 0 : a.id(),name.getText(),unit.getText(),price.getText().isBlank() ? null : new BigDecimal(price.getText().trim()),active.isSelected()),actor))) {refresh();return;}
        }
    }
}
