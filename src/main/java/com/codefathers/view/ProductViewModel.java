package com.codefathers.view;

import java.math.BigDecimal;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.repository.ProductRepository;
import com.codefathers.repository.ProductRepositoryImpl;
import com.codefathers.service.ProductService;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class ProductViewModel {

    private final ProductService productService;

    public ProductViewModel() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        ProductRepository repository = new ProductRepositoryImpl(); // sua implementação
        this.productService = new ProductService(repository, validator);
    }

    public void createSampleProduct() {
        CreateProductDTO dto = new CreateProductDTO();
        dto.setSku("DIPIROCA123");
        dto.setBuyPrice(new BigDecimal("10.00"));
        dto.setSellPrice(new BigDecimal("15.00"));
        dto.setName("Produto de Test");
        dto.setDescription("Descrição do produto de");

        productService.createProduct(dto);
        System.out.println("Produto criado com sucesso!");
    }

    public static void main(String[] args) {
        ProductViewModel viewModel = new ProductViewModel();
        viewModel.createSampleProduct();
    }

}