package com.codefathers.service;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateProductDTO;
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

    public void updateProduct(String sku, UpdateProductDTO dto) {
        Product product = productRepository.update(sku);

        if (product == null) {
            throw new RuntimeException("Produto com SKU '" + sku + "' não encontrado.");
        }
        product.setDescription(dto.getDescription());
        product.setBuyPrice(dto.getBuyPrice());
        product.setSellPrice(dto.getSellPrice());
        try {
            productRepository.save(product);
        }
        catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }
}
