package com.scc.util;

import com.scc.dao.CustomerDao;
import com.scc.dao.UserDao;
import com.scc.model.Customer;
import com.scc.model.Sales;
import com.scc.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportExporter {
    private final UserDao userDao;
    private final CustomerDao customerDao;

    public ReportExporter(UserDao userDao, CustomerDao customerDao){
        this.userDao = userDao;
        this.customerDao = customerDao;
    }

    private String dueStatus(String due){
        if(due == null || due.isBlank()) return "-";
        try{
            return LocalDate.parse(due).isBefore(LocalDate.now()) ? "OVERDUE" : "Current";
        }catch (Exception e){
            return "-";
        }
    }

    public void exportDailySales(List<Sales> list, String date){
        Map<Integer, String> cashiers = new HashMap<>();
        for(User u : userDao.getAllUser()) cashiers.put(u.user_id(), u.username());

        Map<Integer, String> customers = new HashMap<>();
        for(Customer c : customerDao.getAllCustomer()) customers.put(c.customer_id(), c.full_name());

        List<String[]> rows =new ArrayList<>();
        rows.add(new String[]{"sale_id", "date", "cashier", "payment_type", "customer", "total"});
        for(Sales s : list){
            rows.add(new String[]{
                    String.valueOf(s.sales_id()),
                    s.Sales_date(),
                    cashiers.getOrDefault(s.cashier_id(), "(unknown)"),
                    s.payment_type(),
                    s.customer_id() == 0 ? "-" : customers.getOrDefault(s.customer_id(), "(unknown)"),
                    String.format("%.2f", s.total_amount())
            });
        }
            save("exports/daily_sales_"+ date + ".csv", rows);
    }

    public void exportUtang(List<Customer> utangList){
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"customer_id", "name", "balance", "credit_limit", "due_date", "status"});
        for(Customer c : utangList){
            rows.add(new String[]{
                    String.valueOf(c.customer_id()),
                    c.full_name(),
                    String.format("%.2f", c.balance()),
                    String.format("%.2f", c.credit_limit()),
                    c.due_date() == null ? "-" : c.due_date(),
                    dueStatus(c.due_date())
            });
        }

        save("exports/utang_report_" + LocalDate.now() + ".csv", rows);
    }

    private void save(String path, List<String[]> rows){
        try{
            new CsvExporter(path).writeline(rows);
            System.out.println("\n -> Saved to "+ path);
        }catch (Exception e){
            System.out.println("\n -> [ERROR] Export failed: "+ e.getMessage());
        }
    }
}
