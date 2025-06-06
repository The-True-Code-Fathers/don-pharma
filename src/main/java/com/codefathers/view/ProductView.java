package com.codefathers.view;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.repository.ProductRepositoryImpl;
import com.codefathers.service.ProductService;
import com.codefathers.util.ValidationUtil;

import java.util.Scanner;

public class ProductView {

    private ProductService productService;

    public ProductView(ProductService productService) {
        this.productService = productService;
    }

    public void render() {
        System.out.println("Hello, World!");


    }

    public static void main(String[] args) {
        new ProductView(new ProductService(new ProductRepositoryImpl(), ValidationUtil.getValidator())).render();
    }

}
