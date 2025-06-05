package com.codefathers.service;

import com.codefathers.model.entity.Product;

import java.util.List;

public interface ProductRepository {
    Product findBySKU(String  sku);
    List<Product> listAllProducts();
    void save(Product product);
}


