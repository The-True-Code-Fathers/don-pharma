package com.codefathers.model.entity;

import java.math.BigDecimal;

import com.codefathers.model.enums.UmSelect;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "product")
public class Product {
    
    @Id
    @Column(length = 50)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UmSelect umSelect;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "buy_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal buyPrice;

    @Column(name = "sell_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal sellPrice;

    @Column(name = "active", nullable = false)
    private boolean active;


}
