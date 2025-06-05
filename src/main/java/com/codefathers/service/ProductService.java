package com.codefathers.service;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.entity.Product;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;


public class ProductService {

    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void createProduct(CreateProductDTO products) {
        Product product = Product.builder()
                .sku(products.getSku())
                .buyPrice(products.getBuyPrice())
                .sellPrice(products.getSellPrice())
                .name(products.getName())
                .build();
        try {
            productRepository.save(product);
        }
        catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }

    }




}
