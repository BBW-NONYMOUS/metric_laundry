package com.metriclaundry;
import com.metriclaundry.config.Database;
import com.metriclaundry.dao.*;
import com.metriclaundry.model.*;
import com.metriclaundry.service.*;
import com.metriclaundry.util.AppException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Explicit integration check; only run on a disposable _payment_test database. */
public class AddOnIntegrationCheck {
 static BigDecimal n(String s) { return new BigDecimal(s); }
 static void check(boolean b,String m) {if(!b) throw new AssertionError(m);}
 static void fails(Runnable r) {try {r.run();}catch(AppException e){return;}throw new AssertionError("Expected rejection");}
 static int count() throws Exception { try(Connection c=Database.getConnection();Statement s=c.createStatement();ResultSet rs=s.executeQuery("SELECT COUNT(*) FROM orders")){rs.next();return rs.getInt(1);} }
 public static void main(String[] args) throws Exception {
  if(!System.getProperty("db.url", "").matches(".*_payment_test(?:\\?.*)?")) throw new IllegalStateException("Use a disposable _payment_test database");
  User admin=new AuthService().login("admin","admin123"),staff=new AuthService().login("staff","staff123"),customer=new AuthService().login("juan","customer123");
  AddOnDAO dao=new AddOnDAO();OrderService orders=new OrderService();PaymentService pay=new PaymentService();
  AddOn item=dao.list().stream().filter(a -> a.name().equals("Detergent powder")).findFirst().orElseThrow();
  final int id=item.id();
  fails(() -> dao.save(new AddOn(id,"Detergent powder","sachet",n("10"),true),customer));
  fails(() -> dao.save(new AddOn(id,"Detergent powder","sachet",null,true),admin));
  fails(() -> dao.save(new AddOn(id,"Detergent powder","sachet",n("-1"),true),admin));
  dao.save(new AddOn(id,"Detergent powder","sachet",n("10"),true),admin);
  var extras=List.of(new AddOnLine(id,"Detergent powder","sachet",n("2"),n("10")));
  Order o=orders.placeOrder(customer.getId(),1,n("2"),"Add-on regression",staff,extras);
  check(o.getTotal().compareTo(o.getServiceSubtotal().add(n("20")))==0,"extras in total");
  check(o.getAddOns().size()==1 && o.getAddOnTotal().compareTo(n("20"))==0,"saved lines");
  BigDecimal half=o.getTotal().divide(n("2"));
  pay.submit(o.getId(),PaymentMethod.CASH,null,half,staff);
  check(orders.find(o.getId()).getBalance().compareTo(half)==0,"installment includes extras");
  dao.save(new AddOn(id,"Detergent powder","sachet",n("15"),true),admin);
  check(orders.find(o.getId()).getAddOnTotal().compareTo(n("20"))==0,"historical price preserved");
  int before=count();
  fails(() -> orders.placeOrder(customer.getId(),1,n("2"),null,customer,extras));
  check(count()==before,"stale price rejects entire order");
  var duplicate=List.of(new AddOnLine(id,"Detergent powder","sachet",n("1"),n("15")),new AddOnLine(id,"Detergent powder","sachet",n("1"),n("15")));
  fails(() -> orders.placeOrder(customer.getId(),1,n("2"),null,customer,duplicate));
  dao.save(new AddOn(id,"Detergent powder","sachet",n("15"),false),admin);
  fails(() -> orders.placeOrder(customer.getId(),1,n("2"),null,customer,List.of(new AddOnLine(id,"Detergent powder","sachet",n("1"),n("15")))));
  check(count()==before,"invalid extras leave no order");
  dao.save(new AddOn(id,"Detergent powder","sachet",n("15"),true),admin);
  fails(() -> orders.placeOrder(customer.getId(),1,n("2"),null,customer,List.of(new AddOnLine(id,"Detergent powder","sachet",n("-1"),n("15")))));
  check(orders.placeOrder(customer.getId(),1,n("1"),null,customer).getAddOns().isEmpty(),"optional extras");
  System.out.println("PASS: add-on prices, roles, quantities, totals, installment balance, saved price snapshots, disabled extras and atomic rejection of changed prices");
 }
}
