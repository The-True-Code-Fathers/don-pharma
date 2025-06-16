package com.codefathers.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateProductDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.repository.dto.MostSoldProductDTO;
import com.codefathers.repository.interfaces.ProductRepository;

import com.codefathers.util.JsonUtil;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final Validator validator;

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
                    .measurementUnit(createProductDTO.getMeasurementUnit())
                    .createdAt(LocalDateTime.now())
                    .build();
        try {
            productRepository.save(product);
        }
        catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateProduct(@Valid String productSku, UpdateProductDTO updateProductDTO) {

        Optional<Product> maybeProduct = productRepository.findBySKU(productSku);
        if (maybeProduct.isEmpty()) {
            throw new RuntimeException("Produto com SKU '" + productSku + "' não encontrado.");
        }
        Product product = maybeProduct.get();

        product.setDescription(updateProductDTO.getDescription());
        product.setBuyPrice(updateProductDTO.getBuyPrice());
        product.setSellPrice(updateProductDTO.getSellPrice());
        product.setActive(updateProductDTO.isActive());
        product.setMeasurementUnit(updateProductDTO.getMeasurementUnit());
        try {
            productRepository.update(product);
        } catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }

    public Product findProductBySKU(String productSku) {
        return productRepository.findBySKU(productSku).get();
    }

    public List<Product> findAllProducts() {
        return productRepository.listAll();
    }

    public List<MostSoldProductDTO> findMostSoldProducts(LocalDate from, LocalDate to, int limit) {
        return productRepository.findMostSoldProducts(from, to, limit);
    }

}