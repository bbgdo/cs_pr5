package org.example.dao;


import org.example.entity.Product;

import java.util.List;

public interface ProductDao {
    void createProduct(String name, double price);
    Product readProduct(int id);
    void updateProduct(int id, String name, double price);
    void deleteProduct(int id);
    List<Product> listProductsByPrice(double maxPrice);
}
