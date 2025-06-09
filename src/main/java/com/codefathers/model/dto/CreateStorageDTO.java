package com.codefathers.model.dto;

import com.codefathers.model.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStorageDTO {
    
    private String productSku;
    
    private Product product;

    private int productQuantity;
    
}
