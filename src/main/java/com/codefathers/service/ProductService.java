package com.codefathers.service;

import java.util.List;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateProductDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.repository.interfaces.ProductRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

@ApplicationScoped
public class ProductService {

    private final ProductRepository productRepository;
    private final Validator validator;

    @Inject
    public ProductService(ProductRepository productRepository, Validator validator) {
        this.validator = validator;
        this.productRepository = productRepository;
    }

    public void createProduct(@Valid CreateProductDTO createProductDTO) {

        var violations = validator.validate(createProductDTO);

        if (!violations.isEmpty()) {
            throw new jakarta.validation.ConstraintViolationException(violations);
        }

        Product product = Product.builder()
                    .sku(createProductDTO.getSku())
                    .buyPrice(createProductDTO.getBuyPrice())
                    .sellPrice(createProductDTO.getSellPrice())
                    .name(createProductDTO.getName())
                    .description(createProductDTO.getDescription())
                    .active(true)
                    .build();
        try {
            productRepository.save(product);
        }
        catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateProduct(@Valid String productSku, UpdateProductDTO updateProductDTO) {

        //var violations = validator.validate(updateProductDTO);

        Product product = productRepository.findBySKU(productSku);

        // if (!violations.isEmpty()) {
        //     throw new jakarta.validation.ConstraintViolationException(violations);
        // }

        if (product == null) {
            throw new RuntimeException("Produto com SKU '" + productSku + "' não encontrado.");
        }
        product.setDescription(updateProductDTO.getDescription());
        product.setBuyPrice(updateProductDTO.getBuyPrice());
        product.setSellPrice(updateProductDTO.getSellPrice());
        product.setActive(updateProductDTO.isActive());
        try {
            productRepository.update(product);
        } catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }

    public Product findProductBySKU(String productSku) {
        return productRepository.findBySKU(productSku);
    }

    public List<Product> findAllProducts() {
        return productRepository.listAllProducts();
    }

}