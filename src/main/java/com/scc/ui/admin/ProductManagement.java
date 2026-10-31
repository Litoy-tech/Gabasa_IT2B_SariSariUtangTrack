package com.scc.ui.admin;

import com.scc.dao.ProductDao;
import com.scc.model.Product;

import static com.scc.ui.InputHelper.*;

import java.util.List;
import java.util.Scanner;

public class ProductManagement {
    private final ProductDao productDao;
    private final Scanner sc;

    public ProductManagement(ProductDao productDao, Scanner sc){
        this.productDao = productDao;
        this.sc = sc;
    }

    public void show(){
        while (true){
            System.out.println("\n===============================");
            System.out.println("        MANAGE PRODUCT");
            System.out.println("===============================");
            System.out.println("[1] Add Product\n[2] View Products\n[3] Edit Product\n[4] Restock\n[5] Deactivate / Activate Product\n[6] Delete Product\n[0] Back");
            String choice = readString(sc, "Enter Choice: ");

            switch (choice){
                case "1" -> addProduct();
                case "2" -> viewProducts();
                case "3" -> System.out.println("Edit Product (soon)");
                case "4" -> System.out.println("Restock (soon)");
                case "5" -> System.out.println("Deactivate / Activate (soon)");
                case "6" -> System.out.println("Delete Product (soon)");
                case "0" -> { return; }
                default -> System.out.println("Invalid choice! 0 to 6 only.");
            }
        }
    }

    private void addProduct(){
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
            productDao.addProduct(name, category, price, stock);
        }else {
            System.out.println(" -> Cancelled.");
        }
    }


    private  void displayProductTable(List<Product> productList, String title){
        System.out.println("\n==================================================================================================");
        System.out.println("                                         " +title+ " " );
        if(productList.isEmpty()){
            System.out.println("\n-> No Records.");
            System.out.println("--------------------------------------------------------------------------------------------------");
            return;
        }

        System.out.println("+------+--------------------------------+----------------------+----------+-------+----------+");
        System.out.printf ("| %-4s | %-30s | %-20s | %8s | %5s | %-8s |\n", "ID", "Name", "Category", "Price", "Stock", "Status");
        System.out.println("+------+--------------------------------+----------------------+----------+-------+----------+");
        for(Product p: productList){
            System.out.printf("| %-4d | %-30s | %-20s | %8.2f | %5d | %-8s |\n", p.product_id(), p.p_name(), p.p_category(), p.p_price(),
                    p.stock_qty(), p.p_status());
        }
        System.out.println("+------+--------------------------------+----------------------+----------+-------+----------+");
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

            String choice = readString(sc, "Filter ([1] ALL | [2] ACTIVE | [3] INACTIVE | [4] Search | [0] Back): ").trim();

            switch (choice){
                case "1" -> {status = null; keyword = null; }
                case "2" -> status = "ACTIVE";
                case "3" -> status = "INACTIVE";
                case "4" -> keyword = readString(sc, "Enter product name to search: ");
                case "0" -> {return;}
                default ->  System.out.println(" -> [ERROR] Invalid choice. 0 to 4 only.");
            }
        }
    }

}
