package com.metriclaundry.dao;
import com.metriclaundry.config.Database;
import com.metriclaundry.model.*;
import com.metriclaundry.util.AppException;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class AddOnDAO {
    public List<AddOn> list() {
        try(Connection c=Database.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT * FROM addons ORDER BY name"); ResultSet rs=ps.executeQuery()) {
            List<AddOn> list=new ArrayList<>();
            while(rs.next()) list.add(new AddOn(rs.getInt("id"),rs.getString("name"),rs.getString("unit"),rs.getBigDecimal("price"),rs.getBoolean("active")));
            return list;
        } catch(SQLException e) { throw new AppException("Could not load add-ons. Run database/migrate_addons.sql on an existing database. " + e.getMessage(),e); }
    }
    public void save(AddOn item, User actor) {
        if(actor==null || actor.getRole()!=Role.ADMIN) throw new AppException("Only administrators can edit add-on prices.");
        if(item.name()==null || item.name().isBlank() || item.name().length()>100 || item.unit()==null || item.unit().isBlank() || item.unit().length()>30)
            throw new AppException("Enter a name (up to 100 characters) and unit (up to 30 characters).");
        if(item.price()!=null && (item.price().signum()<=0 || item.price().stripTrailingZeros().scale()>2 || item.price().compareTo(new BigDecimal("999999.99"))>0))
            throw new AppException("Enter a positive price with at most two decimal places (maximum 999999.99).");
        if(item.active() && item.price()==null) throw new AppException("Set the actual price before enabling this add-on.");
        String sql=item.id()==0 ? "INSERT INTO addons (name,unit,price,active) VALUES (?,?,?,?)" : "UPDATE addons SET name=?,unit=?,price=?,active=? WHERE id=?";
        try(Connection c=Database.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,item.name().trim());ps.setString(2,item.unit().trim());ps.setBigDecimal(3,item.price());ps.setBoolean(4,item.active());
            if(item.id()!=0) ps.setInt(5,item.id());
            if(ps.executeUpdate()!=1) throw new AppException("Add-on was not found. Refresh and try again.");
        } catch(SQLException e) { throw new AppException("Could not save add-on. Names must be unique. " + e.getMessage(),e); }
    }
    public static List<AddOnLine> history(Connection c,int orderId) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("SELECT * FROM order_addons WHERE order_id=? ORDER BY id")) {
            ps.setInt(1,orderId); List<AddOnLine> list=new ArrayList<>();
            try(ResultSet rs=ps.executeQuery()) { while(rs.next()) list.add(new AddOnLine(rs.getInt("addon_id"),rs.getString("name"),rs.getString("unit"),rs.getBigDecimal("quantity"),rs.getBigDecimal("unit_price"))); }
            return list;
        }
    }
    public List<AddOnLine> history(int orderId) {
        try(Connection c=Database.getConnection()) { return history(c,orderId); }
        catch(SQLException e) { throw new AppException("Could not load order extras: " + e.getMessage(),e); }
    }
    public static List<AddOnLine> validate(Connection c,List<AddOnLine> requested) throws SQLException {
        List<AddOnLine> result=new ArrayList<>(); Set<Integer> seen=new HashSet<>();
        for(AddOnLine line:requested.stream().sorted(Comparator.comparingInt(AddOnLine::addonId)).toList()) {
            if(!seen.add(line.addonId()) || line.quantity()==null || line.quantity().signum()<=0 || line.quantity().compareTo(new BigDecimal("100"))>0 || line.quantity().stripTrailingZeros().scale()>2)
                throw new AppException("Each add-on must have a unique entry and a quantity between 0.01 and 100 (at most two decimals).");
            try(PreparedStatement ps=c.prepareStatement("SELECT * FROM addons WHERE id=? FOR UPDATE")) {
                ps.setInt(1,line.addonId());
                try(ResultSet rs=ps.executeQuery()) {
                    if(!rs.next() || !rs.getBoolean("active") || rs.getBigDecimal("price")==null) throw new AppException("An add-on is no longer available. Reopen the add-on selector.");
                    BigDecimal price=rs.getBigDecimal("price");
                    if(price.signum()<=0 || line.unitPrice()==null || price.compareTo(line.unitPrice())!=0 || !rs.getString("unit").equals(line.unit()) || !rs.getString("name").equals(line.name()))
                        throw new AppException("An add-on price or description changed. Reopen the selector and review the new total.");
                    result.add(new AddOnLine(line.addonId(),rs.getString("name"),rs.getString("unit"),line.quantity(),price));
                }
            }
        }
        return result;
    }
    public static void insert(Connection c,int orderId,List<AddOnLine> lines) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("INSERT INTO order_addons (order_id,addon_id,name,unit,quantity,unit_price) VALUES (?,?,?,?,?,?)")) {
            for(AddOnLine line:lines) {
                ps.setInt(1,orderId);ps.setInt(2,line.addonId());ps.setString(3,line.name());ps.setString(4,line.unit());ps.setBigDecimal(5,line.quantity());ps.setBigDecimal(6,line.unitPrice());ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
