package com.codefathers.service;

import java.util.List;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateProductDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.repository.ProductRepository;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;


public class ProductService {

    private final ProductRepository productRepository;
    private final Validator validator;

    public ProductService(ProductRepository productRepository, Validator validator) {
        this.validator = validator;
        this.productRepository = productRepository;
    }

    public void createProduct(@Valid CreateProductDTO products) {

        var violations = validator.validate(products);

        if (!violations.isEmpty()) {
            throw new jakarta.validation.ConstraintViolationException(violations);
        }

        Product product = Product.builder()
                    .sku(products.getSku())
                    .buyPrice(products.getBuyPrice())
                    .sellPrice(products.getSellPrice())
                    .name(products.getName())
                    .description(products.getDescription())
                    .active(true)
                    .build();
        try {
            productRepository.save(product);
        }
        catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateProduct(@Valid String sku, UpdateProductDTO dto) {

        Product product = productRepository.update(sku);

        var violations = validator.validate(dto);

        if (!violations.isEmpty()) {
            throw new jakarta.validation.ConstraintViolationException(violations);
        }

        if (product == null) {
            throw new RuntimeException("Produto com SKU '" + sku + "' não encontrado.");
        }
        product.setDescription(dto.getDescription());
        product.setBuyPrice(dto.getBuyPrice());
        product.setSellPrice(dto.getSellPrice());
        product.setActive(dto.isActive());
        try {
            productRepository.save(product);
        }
        catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }

    public Product findProductBySKU(String sku) {
        return productRepository.findBySKU(sku);
    }

    public List<Product> findAllProducts() {
        return productRepository.listAllProducts();
    }

}
