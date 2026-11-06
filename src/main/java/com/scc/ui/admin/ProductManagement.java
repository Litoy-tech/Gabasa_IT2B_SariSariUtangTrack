package com.scc.ui.admin;

import com.scc.dao.AuditLogDao;
import com.scc.dao.ProductDao;
import com.scc.model.Product;
import com.scc.model.User;

import static com.scc.ui.InputHelper.*;

import java.util.List;
import java.util.Scanner;

public class ProductManagement {
    private final ProductDao productDao;
    private final AuditLogDao logDao;
    private final Scanner sc;

    public ProductManagement(ProductDao productDao, AuditLogDao logDao,Scanner sc){
        this.productDao = productDao;
        this.logDao = logDao;
        this.sc = sc;
    }

    public void show(User admin){
        while (true){
            System.out.println("\n===============================");
            System.out.println("        MANAGE PRODUCT");
            System.out.println("===============================");
            System.out.println("[1] Add Product\n[2] View Products\n[3] Edit Product\n[4] Restock\n[5] Deactivate / Activate Product\n[6] Delete Product\n[0] Back");
            String choice = readString(sc, "Enter Choice: ");

            switch (choice){
                case "1" -> addProduct(admin);
                case "2" -> viewProducts();
                case "3" -> editProduct(admin);
                case "4" -> restockProduct(admin);
                case "5" -> updateStatus(admin);
                case "6" -> deleteProduct(admin);
                case "0" -> { return; }
                default -> System.out.println("Invalid choice! 0 to 6 only.");
            }
        }
    }

    private void addProduct(User admin){
        System.out.println("\n===============================");
        System.out.println("          ADD PRODUCT");
        System.out.println("===============================");

        String name;
        while (true){
            name = readString(sc,"Enter Product Name(0 to Cancel): ");

            if(name.equals("0")){
                System.out.println("\n -> Operation Cancelled.");
                return;
            }
            if(productDao.productExist(name)) {
                System.out.println(" -> [ERROR] Product already exists.");
            }else {
                break;
            }
        }

        String category = readString(sc, "Enter Category: ");

        double price;
        while (true){
            price = readDouble(sc, "Enter Price: ");
            if(price > 0) break;
            System.out.println(" -> [ERROR] Price must be above 0.");
        }

        int stock;
        while(true){
            stock = readInt(sc, "Enter Starting Stock: ");
            if(stock >= 0) break;
            System.out.println(" -> [ERROR] Stock cannot be negative.");
        }

        System.out.println("-------------------------------");
        System.out.println("Name     : "+ name);
        System.out.println("Category : "+ category);
        System.out.printf("Price    : ₱ %.2f\n", price);
        System.out.println("Stock    : "+ stock);
        System.out.println("-------------------------------");

        if(readYesNo(sc, "Add this Product?")){
            int id = (productDao.addProduct(name, category, price, stock));
            if(id != -1){
                logDao.logs(admin.user_id(), "ADD PRODUCT", "product_tbl", null, "Added product: " + name +
                        " | Category: " + category + " | Price: " + price + " | Stock: " + stock);
            }
        }else {
            System.out.println(" -> Cancelled.");
        }
    }

    private  void displayProductTable(List<Product> productList, String title){
        System.out.println("\n==================================================================================================");
        System.out.println("                                         " +title+ " " );
        if(productList.isEmpty()){
            System.out.println("\n-> No Records.");
            System.out.println("---------------------------------------------------------------------------------------------------");
            return;
        }
        System.out.println("+------+--------------------------------+----------------------+----------+-------+------------+");
        System.out.printf ("| %-4s | %-30s | %-20s | %8s | %5s | %-10s |\n", "ID", "Name", "Category", "Price", "Stock", "Status");
        System.out.println("+------+--------------------------------+----------------------+----------+-------+------------+");
        for(Product p: productList){
            System.out.printf("| %-4d | %-30s | %-20s | %8.2f | %5d | %-10s |\n", p.product_id(), p.p_name(), p.p_category(), p.p_price(),
                    p.stock_qty(), p.p_status());
        }
        System.out.println("+------+--------------------------------+----------------------+----------+-------+------------+");
    }

    private List<Product> filterProducts(String status, String keyword){
        return productDao.getProducts().stream()
                .filter(p -> status == null || p.p_status().equalsIgnoreCase(status))
                .filter(p-> keyword == null || p.p_name().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    private void viewProducts(){
        String status = null;
        String keyword = null;
        while (true){
            String title = (status == null) ? "ALL PRODUCTS": status + " PRODUCTS";
            if(keyword != null) title += " (search: " + keyword + ")";
            displayProductTable(filterProducts(status, keyword), title);

            String choice = readString(sc, "Filter ([1] ALL | [2] AVAILABLE  | [3] UNAVAILABLE  | [4] Search | [0] Back): ").trim();

            switch (choice){
                case "1" -> {status = null; keyword = null; }
                case "2" -> status = "AVAILABLE";
                case "3" -> status = "UNAVAILABLE";
                case "4" -> keyword = readString(sc, "Enter product name to search: ");
                case "0" -> {return;}
                default ->  System.out.println(" -> [ERROR] Invalid choice. 0 to 4 only.");
            }
        }
    }

    private Product selectProduct(String status, String title){
        List<Product> products = productDao.getProducts().stream().filter(p-> status == null || p.p_status().equalsIgnoreCase(status)).toList();
        displayProductTable(products, title);

        if(products.isEmpty()) return null;

        int p_id = readInt(sc, "Enter product ID (0 to cancel):  ");
        if(p_id == 0){
            System.out.println(" -> Operation Cancelled.");
            return null;
        }
        Product target_pId = products.stream().filter(p -> p.product_id() == p_id).findFirst().orElse(null);

        if(target_pId == null){
            System.out.println(" -> [ERROR] Product ID not found.");
            return null;
        }
        return target_pId;
    }

    private void editProduct(User admin){
        Product target_pro = selectProduct(null, "EDIT PRODUCT");
        if(target_pro == null) return;

        System.out.println("\n--------------------------------------------");
        System.out.println("Click [Enter] to Keep");
        System.out.print("\nEnter name [ "+target_pro.p_name()+ " ]:");
        String name = sc.nextLine().trim();

        if(name.isEmpty()) name = target_pro.p_name();

        if(!name.equalsIgnoreCase(target_pro.p_name()) && productDao.productExist(name)){
            System.out.println(" -> [ERROR] Product already exists.");
            return;
        }

        System.out.print("Enter Category [ "+target_pro.p_category()+" ]:  ");
        String category = sc.nextLine().trim();

        if(category.isEmpty()) category = target_pro .p_category();

        double price = target_pro.p_price();
        while(true){
            System.out.print("Enter new Price [ "+ target_pro.p_price() + " ]:  ");
            String in = sc.nextLine().trim();

            if(in.isEmpty()) break;
            try{
                double newPrice = Double.parseDouble(in);
                if(newPrice > 0){
                    price = newPrice;
                    break;
                }
                System.out.println(" -> [ERROR] Price must be above 0.");
            }catch (NumberFormatException e){
                System.out.println(" -> [ERROR] Invalid number.");
            }
        }

        if(readYesNo(sc, "Save Changes? ")){
            if(productDao.updateProduct(target_pro.product_id(), name, category, price)){
                logDao.logs(admin.user_id(), "UPDATE PRODUCT", "product_tbl", target_pro.product_id(), "Updated product: " + name +
                        " | Category: " + category + " | Price: " + price);
            }
        }else {
            System.out.println(" -> Update Cancelled.");
        }
    }

    private void restockProduct(User admin){
        Product target_pId = selectProduct("AVAILABLE", "RESTOCK PRODUCT");
        if(target_pId == null) return;

        int addStock;
        while(true) {
            addStock = readInt(sc, "Enter quantity to add: ");
            if(addStock > 0) break;
            System.out.println(" -> [ERROR] Quantity must be above 0.");
        }

        System.out.println("\n-------------------------------");
        System.out.println(" Product   : " + target_pId.p_name());
        System.out.println(" Old Stock : " + target_pId.stock_qty());
        System.out.println(" Adding    : +" + addStock);
        System.out.println(" New Stock : " + (target_pId.stock_qty() + addStock));
        System.out.println("-------------------------------");

        if(readYesNo(sc, "Confirm restock? ")){
            if(productDao.restockProduct(addStock, target_pId.product_id())){
                logDao.logs(admin.user_id(), "RESTOCK PRODUCT", "product_tbl", target_pId.product_id(), "Restocked product: " + target_pId.p_name() +
                        " | Old Stock: " + target_pId.stock_qty() + " | Added: " + addStock + " | New Stock: " + (target_pId.stock_qty() + addStock));
            }
        }else {
            System.out.println(" -> Cancelled.");
        }
    }

    private void updateStatus(User admin){
        Product target_id = selectProduct(null, "PRODUCT (AVAILABLE/ UNAVAILABLE)");
        if(target_id == null) return;

        String updateStatus;
        if(target_id.p_status().equalsIgnoreCase("AVAILABLE")){
            updateStatus = "UNAVAILABLE";
        }else {
            updateStatus = "AVAILABLE";
        }

        if(readYesNo(sc, "Change "+target_id.p_name()+" to " + updateStatus + "?")){
            if(productDao.updateStatus(target_id.product_id(), updateStatus)){
                logDao.logs(admin.user_id(), "UPDATE PRODUCT STATUS", "product_tbl", target_id.product_id(), "Changed product: " + target_id.p_name() +
                        " status from: " + target_id.p_status() + " to: " + updateStatus);
            }
        }else {
            System.out.println(" -> Cancelled.");
        }
    }

    private void deleteProduct(User admin){
        Product target_id = selectProduct(null, "PRODUCT DELETION");
        if(target_id == null) return;

        System.out.println("-------------------------------");
        System.out.println("Name: "+ target_id.p_name());
        System.out.println("Category: "+ target_id.p_category());
        System.out.println("Price: "+ target_id.p_price());
        System.out.println("Stock: "+ target_id.stock_qty());
        System.out.println("-------------------------------");
        if(readYesNo(sc, "Delete this Product?")){
            if(productDao.deleteProduct(target_id.product_id())){
                logDao.logs(admin.user_id(), "DELETE PRODUCT", "product_tbl", target_id.product_id(), "Deleted product: " + target_id.p_name() +
                        " | Category: " + target_id.p_category() + " | Price: " + target_id.p_price() + " | Stock: " + target_id.stock_qty());
            }
        }else {
            System.out.println("\n -> Cancelled.");
        }
    }
}
