package com.codefathers.view;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.service.ProductRepositoryImpl;
import com.codefathers.service.ProductService;

import java.math.BigDecimal;

public class ProductView {

    ProductService productService = new ProductService(new ProductRepositoryImpl());

    var qualquerCOisa = CreateProductDTO.builder()
            .sku("A-12345")
            .description("QUalquer coisa")
            .name("Paracetamol")
            .sellPrice(BigDecimal.valueOf(5))
            .buyPrice(BigDecimal.valueOf(10))
            .build();

        productService.createProduct(qualquerCOisa);

}
