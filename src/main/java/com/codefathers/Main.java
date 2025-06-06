package com.codefathers;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateProductDTO;
import com.codefathers.repository.ProductRepositoryImpl;
import com.codefathers.service.ProductService;
import com.codefathers.util.ValidationUtil;

import java.math.BigDecimal;


public class Main {


    public static void main(String[] args) {
        ProductService productService = new ProductService(new ProductRepositoryImpl(), ValidationUtil.getValidator());

//        var qualquerCOisa = CreateProductDTO.builder()
//                .sku("A-12345")
//                .description("QUalquer coisa")
//                .name("Paracetamol")
//                .sellPrice(BigDecimal.valueOf(5))
//                .buyPrice(BigDecimal.valueOf(10))
//                .build();
//
//        productService.createProduct(qualquerCOisa);

//        var updateProduto = UpdateProductDTO.builder()
//                .description("QUalquer coisa")
//                .sellPrice(BigDecimal.valueOf(25))
//                .buyPrice(BigDecimal.valueOf(15))
//                .build();

    }
}