package com.metriclaundry;
import com.metriclaundry.config.Database;
import com.metriclaundry.model.*;
import com.metriclaundry.service.*;
import com.metriclaundry.dao.*;
import com.metriclaundry.util.AppException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.concurrent.*;

/** Run explicitly with -Ddb.url pointing to a disposable database ending in _payment_test. */
public class PaymentIntegrationCheck {
    static final PaymentService payments=new PaymentService();
    static final OrderService orders=new OrderService();
    static User staff, customer;
    static BigDecimal money(String value) { return new BigDecimal(value); }
    static void check(boolean yes,String message) { if(!yes) throw new AssertionError(message); }
    static void fails(Runnable action) { try { action.run(); } catch(AppException expected) { return; } throw new AssertionError("Expected rejection"); }
    static Order order() { return orders.placeOrder(customer.getId(),1,money("2"),"Payment regression",staff); }
    public static void main(String[] args) throws Exception {
        if(!System.getProperty("db.url", "").matches(".*_payment_test(?:\\?.*)?")) throw new IllegalStateException("Use a disposable _payment_test database");
        staff=new AuthService().login("staff","staff123"); customer=new AuthService().login("juan","customer123");
        Order o=order(); int id=o.getId(); BigDecimal half=o.getTotal().divide(money("2"));
        check(o.getPaymentStatus()==PaymentStatus.UNPAID,"initial unpaid");
        payments.submit(id,PaymentMethod.CASH,null,half,staff);
        o=orders.find(id); check(o.getPaymentStatus()==PaymentStatus.PARTIAL && o.getBalance().compareTo(half)==0,"half paid");
        fails(() -> orders.cancel(id,"test",staff));
        User admin=new AuthService().login("admin","admin123");
        fails(() -> orders.overrideStatus(id,OrderStatus.CANCELLED,"test",admin));
        fails(() -> orders.overrideStatus(id,OrderStatus.COMPLETED,"test",admin));
        Payment pending=payments.submit(id,PaymentMethod.GCASH,"123456789",half,customer);
        check(orders.find(id).getBalance().compareTo(half)==0,"pending must not reduce balance");
        fails(() -> payments.submit(id,PaymentMethod.CASH,null,half,staff));
        fails(() -> payments.confirm(pending.getId(),customer));
        payments.confirm(pending.getId(),staff);
        check(orders.find(id).isPaid(),"fully paid");
        fails(() -> payments.confirm(pending.getId(),staff));
        check(payments.history(id).size()==2,"history retained");
        orders.overrideStatus(id,OrderStatus.COMPLETED,"paid",admin);
        Order second=order();
        Payment bad=payments.submit(second.getId(),PaymentMethod.CASH,null,second.getTotal(),customer);
        payments.voidPending(bad.getId(),staff);
        check(orders.find(second.getId()).getPaymentStatus()==PaymentStatus.UNPAID,"void restores available balance");
        fails(() -> payments.confirm(bad.getId(),staff));
        fails(() -> payments.submit(second.getId(),PaymentMethod.CASH,null,money("-1"),staff));
        fails(() -> payments.submit(second.getId(),PaymentMethod.CASH,null,money("0.001"),staff));
        fails(() -> payments.submit(second.getId(),PaymentMethod.CASH,null,second.getTotal().add(BigDecimal.ONE),staff));
        User other=new AuthService().login("maria","customer123");
        fails(() -> payments.submit(second.getId(),PaymentMethod.CASH,null,BigDecimal.ONE,other));
        ExecutorService pool=Executors.newFixedThreadPool(2);
        CountDownLatch start=new CountDownLatch(1);
        Callable<Boolean> pay=() -> { start.await(); try {payments.submit(second.getId(),PaymentMethod.CASH,null,second.getTotal(),staff); return true;} catch(AppException ex) {return false;} };
        Future<Boolean> one=pool.submit(pay),two=pool.submit(pay); start.countDown();
        check(one.get() ^ two.get(),"concurrent full payments must accept exactly one"); pool.shutdown();
        check(payments.history(second.getId()).size()==2,"void entry preserved");
        try(Connection c=Database.getConnection(); Statement st=c.createStatement(); ResultSet rs=st.executeQuery("SELECT COUNT(*) FROM orders WHERE status<>'CANCELLED'")) {
            rs.next(); int expected=rs.getInt(1),count=0;
            for(Object[] row:new ReportDAO().salesByService(java.time.LocalDate.now().minusYears(2),java.time.LocalDate.now().plusDays(1))) count+=(Integer)row[2];
            check(count==expected,"installments must not inflate report counts");
        }
        System.out.println("PASS: unpaid, partial, pending, fully paid, void, ownership, validation, release/cancel guards, concurrency, history and report counts");
    }
}
