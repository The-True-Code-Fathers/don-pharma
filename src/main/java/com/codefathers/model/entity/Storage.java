package com.codefathers.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Entity(name = "storage")
public class Storage {
    
    @Id
    @Column(name = "product_sku")
    private String productSku;

    @OneToOne
    @JoinColumn(name = "product_sku", referencedColumnName = "sku")
    private Product product;

    @Column(name = "product_quantity", nullable = false)
    private int productQuantity;

}
