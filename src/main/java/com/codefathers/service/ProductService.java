package com.codefathers.service;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.repository.ProductRepository;
import org.hibernate.exception.ConstraintViolationException;


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
                    .description(products.getDescription())
                    .build();
        try {
            productRepository.save(product);
        }
        catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }

}
