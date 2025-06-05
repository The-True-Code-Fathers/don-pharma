package com.codefathers.service;

import com.codefathers.model.entity.Product;

public interface ProductRepository {
    Product findBySKU(String  sku);
    void save(Product product);
}


