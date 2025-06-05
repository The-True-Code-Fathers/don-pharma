package com.codefathers;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Product;
import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.service.ProductRepository;
import com.codefathers.service.ProductRepositoryImpl;
import com.codefathers.service.ProductService;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;


public class Main {


    public static void main(String[] args) {
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
}